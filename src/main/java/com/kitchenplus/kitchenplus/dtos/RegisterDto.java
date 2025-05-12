package com.kitchenplus.kitchenplus.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public record RegisterDto(
    @NotBlank @Email
    @Pattern(regexp = "^\\S.*\\S$|^\\S$|^$", message = "cannot have leading or trailing whitespace")
    String email,

    @NotBlank @Size(min = 8, max = 64, message = "must be between 8 and 64 characters")
    @Pattern(regexp = "^\\S.*\\S$|^\\S$|^$", message = "cannot have leading or trailing whitespace")
    String password
) {}
