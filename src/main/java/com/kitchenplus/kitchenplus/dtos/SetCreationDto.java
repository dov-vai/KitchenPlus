package com.kitchenplus.kitchenplus.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SetCreationDto(
        @NotBlank(message = "Set title is required")
        @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
        String title,

        @NotEmpty(message = "Please select at least one item for your set")
        List<Long> selectedItems
) {
}
