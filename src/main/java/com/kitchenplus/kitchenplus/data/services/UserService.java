package com.kitchenplus.kitchenplus.data.services;

import com.kitchenplus.kitchenplus.data.models.Client;
import com.kitchenplus.kitchenplus.data.models.Session;
import com.kitchenplus.kitchenplus.data.models.User;
import com.kitchenplus.kitchenplus.data.repositories.SessionRepository;
import com.kitchenplus.kitchenplus.data.repositories.UserRepository;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired private PasswordEncoder passwordEncoder;

    @Autowired private UserRepository userRepository;
    @Autowired private SessionRepository sessionRepository;

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
     */
    public void login(String email, String password, HttpServletResponse response) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
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
}
