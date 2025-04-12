package com.kitchenplus.kitchenplus.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginDto(
        @NotBlank @Email
        String email,
        @NotBlank @Size(min = 8, max = 64, message = "must be between 8 and 64 characters")
        String password
) {}
