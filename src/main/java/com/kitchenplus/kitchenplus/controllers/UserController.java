package com.kitchenplus.kitchenplus.controllers;

import com.kitchenplus.kitchenplus.data.enums.LinkType;
import com.kitchenplus.kitchenplus.data.models.ConfirmationLink;
import com.kitchenplus.kitchenplus.data.services.UserService;
import com.kitchenplus.kitchenplus.dtos.LoginDto;
import com.kitchenplus.kitchenplus.dtos.RegisterDto;
import com.kitchenplus.kitchenplus.exceptions.UserFriendlyException;
import com.kitchenplus.kitchenplus.utils.AuthUtils;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String login(@ModelAttribute("loginForm") LoginDto dto) {
        // Redirect if already authenticated
        if (AuthUtils.isAuthenticated()) {
            return "redirect:/";
        }
        return "user/loginPage";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("loginForm") LoginDto dto, BindingResult result, HttpServletResponse response) {
        // Redirect if already authenticated
        if (AuthUtils.isAuthenticated()) {
            return "redirect:/";
        }
        
        // Check for validation errors
        if (result.hasErrors()) {
            return "user/loginPage";
        }

        // Attempt to log in
        try {
            userService.login(dto.email().strip().toLowerCase(), dto.password(), response);
        } catch (UserFriendlyException e) {
            result.reject("login", e.getMessage());
            return "user/loginPage";
        } catch (Exception e) {
            result.reject("exception", "Unexpected error occurred");
            e.printStackTrace();
            return "user/loginPage";
        }

        // Redirect to the home page
        return "redirect:/";
    }

    @GetMapping("/register")
    public String register(@ModelAttribute("registerForm") RegisterDto dto) {
        // Redirect if already authenticated
        if (AuthUtils.isAuthenticated()) {
            return "redirect:/";
        }
        return "user/registerPage";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerForm") RegisterDto dto, BindingResult result, RedirectAttributes redirectAttributes) {
        // Redirect if already authenticated
        if (AuthUtils.isAuthenticated()) {
            return "redirect:/";
        }
        
        // Check for validation errors
        if (result.hasErrors()) {
            return "user/registerPage";
        }

        // Check if the email already exists
        var user = userService.getUserByEmail(dto.email().toLowerCase());
        if (user.isPresent()) {
            result.rejectValue("email", "error.email", "Email already exists");
            return "user/registerPage";
        }

        // Create the user
        var createdUser = userService.createUser(dto.email(), dto.password());

        // Generate a confirmation link
        var confirmationLink = userService.createConfirmationLink(LinkType.EMAIL_CONFIRMATION, createdUser);

        // Send a confirmation email
        try {
            userService.sendConfirmationEmail(dto.email(), confirmationLink.getFullAddress());
        } catch (Exception e) {
            System.out.println("Failed to send confirmation email");
            e.printStackTrace();

            result.reject("email", "Failed to send confirmation email");
            return "user/registerPage";
        }

        redirectAttributes.addFlashAttribute("success", "User registered successfully");
        return "redirect:login";
    }

    @GetMapping("/logout")
    public String logout(@CookieValue("auth_token") String token, HttpServletResponse response) {
        userService.logout(token, response);
        return "redirect:/login";
    }


    /**
     * This endpoint is responsible for handling the confirmation links.
     * It checks if the link is valid and not expired.
     * If the link is valid, but expired, it will create a new link and send a new email.
     * If the link is valid and not expired, it will perform the confirmation actions.
     * Otherwise, it will show an error message.
     */
    @GetMapping("/confirm/{addressFragment}")
    public String confirmEmail(@PathVariable String addressFragment, RedirectAttributes redirectAttributes, Model model) {
        // Query the confirmation link
        ConfirmationLink confirmationLink;
        try {
            confirmationLink = userService.getConfirmationLink(addressFragment);
        } catch (UserFriendlyException e) {
            model.addAttribute("error", e.getMessage());
            return "/user/emailConfirmationPage";
        }

        // Assert the link is EMAIL_CONFIRMATION
        // this is only because we won't be implementing other link types
        if (confirmationLink.getType() != LinkType.EMAIL_CONFIRMATION) {
            throw new UnsupportedOperationException("Invalid link type");
        }

        // Perform the confirmation actions
        try {
            userService.confirmEmailAddress(confirmationLink);
        } catch (UserFriendlyException e) {
            model.addAttribute("error", e.getMessage());
            return "/user/emailConfirmationPage";
        }

        // If successful, redirect to the login page
        redirectAttributes.addFlashAttribute("success", "Email confirmed successfully");
        return "redirect:/login";
    }
}
