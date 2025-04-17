package com.kitchenplus.kitchenplus.controllers;

import com.kitchenplus.kitchenplus.data.services.UserService;
import com.kitchenplus.kitchenplus.dtos.LoginDto;
import com.kitchenplus.kitchenplus.dtos.RegisterDto;
import com.kitchenplus.kitchenplus.utils.AuthUtils;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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
        return "user/login";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("loginForm") LoginDto dto, BindingResult result, HttpServletResponse response) {
        // Redirect if already authenticated
        if (AuthUtils.isAuthenticated()) {
            return "redirect:/";
        }
        
        // Check for validation errors
        if (result.hasErrors()) {
            return "user/login";
        }

        // Attempt to log in
        userService.login(dto.email().strip().toLowerCase(), dto.password(), response);

        // Redirect to the home page
        return "redirect:/";
    }

    @GetMapping("/register")
    public String register(@ModelAttribute("registerForm") RegisterDto dto) {
        // Redirect if already authenticated
        if (AuthUtils.isAuthenticated()) {
            return "redirect:/";
        }
        return "user/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerForm") RegisterDto dto, BindingResult result, RedirectAttributes redirectAttributes) {
        // Redirect if already authenticated
        if (AuthUtils.isAuthenticated()) {
            return "redirect:/";
        }
        
        // Check for validation errors
        if (result.hasErrors()) {
            return "user/register";
        }

        // Check if the email already exists
        var user = userService.getUserByEmail(dto.email().strip().toLowerCase());
        if (user.isPresent()) {
            result.rejectValue("email", "error.email", "Email already exists");
            return "user/register";
        }

        // Create the user
        userService.createUser(dto.email(), dto.password());

        // TODO: Add a success message
        redirectAttributes.addFlashAttribute("success", "User registered successfully");
        return "redirect:login";
    }

    @GetMapping("/logout")
    public String logout(@CookieValue("auth_token") String token, HttpServletResponse response) {
        System.out.println("parsing logout");
        userService.logout(token, response);
        return "redirect:/login";
    }
}
