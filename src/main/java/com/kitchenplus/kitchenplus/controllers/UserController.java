package com.kitchenplus.kitchenplus.controllers;

import com.kitchenplus.kitchenplus.dtos.LoginDto;
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

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("loginForm") LoginDto dto, BindingResult result) {

        System.out.println("Validation OK");
        System.out.println(dto);

        result.reject("custom-err", "Rejecting just because it's not yet implemented");

        if (result.hasErrors()) {
            return "user/login";
        }

        return "user/login";
    }
}
