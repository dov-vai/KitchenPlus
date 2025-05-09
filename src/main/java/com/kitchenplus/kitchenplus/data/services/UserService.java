package com.kitchenplus.kitchenplus.data.services;

import com.kitchenplus.kitchenplus.AppProperties;
import com.kitchenplus.kitchenplus.data.enums.LinkType;
import com.kitchenplus.kitchenplus.data.models.Client;
import com.kitchenplus.kitchenplus.data.models.ConfirmationLink;
import com.kitchenplus.kitchenplus.data.models.Session;
import com.kitchenplus.kitchenplus.data.models.User;
import com.kitchenplus.kitchenplus.data.repositories.ConfirmationLinkRepository;
import com.kitchenplus.kitchenplus.data.repositories.SessionRepository;
import com.kitchenplus.kitchenplus.data.repositories.UserRepository;
import com.kitchenplus.kitchenplus.exceptions.UserFriendlyException;
import com.kitchenplus.kitchenplus.utils.GeneratorUtils;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

@Service
public class UserService {

    @Autowired private AppProperties appProperties;

    @Autowired private PasswordEncoder passwordEncoder;

    @Autowired private UserRepository userRepository;
    @Autowired private SessionRepository sessionRepository;
    @Autowired private ConfirmationLinkRepository confirmationLinkRepository;

    private final int SESSION_LENGTH = 7; // in days
    private final int SESSION_LENGTH_IN_SECONDS = SESSION_LENGTH * 24 * 60 * 60; // in seconds

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Creates a new user with the given email and password.
     * The password is hashed using BCrypt before being saved to the database.
     * @param email Email address of the user
     * @param password Password of the user
     * @return The created User object
     */
    public Client createUser(String email, String password) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(); 
        Client user = new Client();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        return user;
    }

    /**
     * Logs in a user with the given email and password.
     * If the credentials are valid, a session token is created and stored in a cookie.
     * @param email
     * @param password
     * @param response
     * @throws MessagingException 
     * @throws UserFriendlyException 
     */
    public User login(String email, String password, HttpServletResponse response) throws MessagingException, UserFriendlyException {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UserFriendlyException("Invalid credentials"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new UserFriendlyException("Invalid credentials");
        }

        // If email is not confirmed, create a new confirmation link
        if (!user.isEmailConfirmed()) {
            var confirmationLink = createConfirmationLink(LinkType.EMAIL_CONFIRMATION, user);
            sendConfirmationEmail(user.getEmail(), confirmationLink.getFullAddress());
            throw new UserFriendlyException("Email is not confirmed, a new confirmation link has been sent to your email");
        }

        String token = UUID.randomUUID().toString();

        Session authToken = new Session();
        authToken.setToken(token);
        authToken.setExpiresAt(LocalDateTime.now().plusDays(SESSION_LENGTH));
        authToken.setUser(user);
        sessionRepository.save(authToken);

        Cookie cookie = new Cookie("auth_token", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(SESSION_LENGTH_IN_SECONDS);
        response.addCookie(cookie);

        return user;
    }

    /**
     * Logs out the user by invalidating the session token.
     * The session token is removed from the database and the cookie is cleared.
     * @param token
     * @param response
     */
    public void logout(String token, HttpServletResponse response) {
        response.addCookie(new Cookie("auth_token", null));
        sessionRepository.findByToken(token).ifPresent(sessionRepository::delete);
    }

    /**
     * Returns jakarta.mail.Session object for sending emails.
     * @return jakarta.mail.Session object
     */
    private jakarta.mail.Session getEmailSession() {
        Properties prop = new Properties();
        prop.put("mail.smtp.host", appProperties.getMail().getHost());
        prop.put("mail.smtp.port", appProperties.getMail().getPort());
        prop.put("mail.smtp.auth", true);
        prop.put("mail.smtp.ssl.enable", true);

        var session = jakarta.mail.Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                    appProperties.getMail().getUsername(),
                    appProperties.getMail().getPassword()
                );
            }
        });
        return session;
    }

    public void sendConfirmationEmail(String email, String url) throws MessagingException {
        StringBuilder content = new StringBuilder();
        content.append("<h1>Confirm your email address</h1>");
        content.append("<p>Click on this link to confirm your email address:</p>");
        content.append("<a href=\"").append(url).append("\">Confirm</a>");
        content.append("<p>If the link does not work, copy this link to your browser's address bar:</p>");
        content.append("<p>").append(url).append("</p>");
        content.append("<p>If you did not request this, please ignore this email.</p>");
        sendMail(email, "Confirm your email address", content.toString());
    }

    /**
     * Sends an email to the specified address with the given subject and content.
     * The email is sent using the SMTP server configured in the getEmailSession() method.
     * @param destinationAddress The email address to send the email to
     * @param subject The subject of the email
     * @param content The content of the email
     * @throws MessagingException If there is an error sending the email
     */
    private void sendMail(String destinationAddress, String subject, String content) throws MessagingException {
        var session = getEmailSession();

        var message = new MimeMessage(session);
        message.setFrom(new InternetAddress(appProperties.getMail().getUsername()));
        message.setRecipients(
            Message.RecipientType.TO, InternetAddress.parse(destinationAddress)
        );
        message.setSubject(subject);

        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(content, "text/html; charset=utf-8");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(mimeBodyPart);

        message.setContent(multipart);

        Transport.send(message);
    }

    public ConfirmationLink createConfirmationLink(LinkType type, User user) {
        var link = new ConfirmationLink(
            type,
            GeneratorUtils.generateRandomString(32),
            LocalDateTime.now().plusDays(7),
            user
        );
        confirmationLinkRepository.save(link);
        return link;
    }

    /**
     * Finds a confirmation link by its address fragment.
     * The address fragment is the part of the link that comes after the base URL.
     * @param addressFragment
     * @return
     * @throws UserFriendlyException
     */
    public ConfirmationLink getConfirmationLink(String addressFragment) throws UserFriendlyException {
        return confirmationLinkRepository.findByAddressFragment(addressFragment)
            .orElseThrow(() -> new UserFriendlyException("Invalid or expired confirmation link"));
    }

    @Transactional
    public void confirmEmailAddress(ConfirmationLink link) throws UserFriendlyException {
        if (link.getType() != LinkType.EMAIL_CONFIRMATION) {
            throw new UnsupportedOperationException("Link type is not EMAIL_CONFIRMATION");
        }

        // If the link is expired, create a new one and send a new email
        if (link.getValidUntil().isBefore(LocalDateTime.now())) {
            var newLink = createConfirmationLink(LinkType.EMAIL_CONFIRMATION, link.getUser());
            try {
                sendConfirmationEmail(link.getUser().getEmail(), newLink.getFullAddress());
            } catch (MessagingException e) {
                throw new UserFriendlyException("Failed to send a new confirmation email, try again later");
            }
            throw new UserFriendlyException("Expired confirmation link, a new link has been sent to your email");
        }

        var user = link.getUser();
        user.setEmailConfirmed(true);
        userRepository.save(user);

        // Delete all email confirmation links for this user
        confirmationLinkRepository.deleteByUserIdAndType(user.getId(), link.getType());
    }
}
