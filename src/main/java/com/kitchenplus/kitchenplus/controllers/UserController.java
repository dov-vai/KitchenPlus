package com.kitchenplus.kitchenplus.controllers;

import com.kitchenplus.kitchenplus.dtos.LoginDto;
import com.kitchenplus.kitchenplus.dtos.RegisterDto;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;


@Controller
public class UserController {
    @GetMapping("/login")
    public String login(@ModelAttribute("loginForm") LoginDto dto) {
        return "user/login";
    }

    @GetMapping("/register")
    public String register(@ModelAttribute("registerForm") RegisterDto dto) {
        return "user/register";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("loginForm") LoginDto dto, BindingResult result) {
        if (result.hasErrors()) {
            return "user/login";
        }

        System.out.println("Validation OK");
        System.out.println(dto);

        if (true) {
            result.reject("custom-err", "Rejecting just because it's not yet implemented");
            return "user/login";
        }

        return "user/login";
    }
}
