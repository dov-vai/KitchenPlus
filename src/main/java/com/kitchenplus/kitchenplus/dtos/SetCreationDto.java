package com.kitchenplus.kitchenplus.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SetCreationDto(
        @NotBlank(message = "Set title is required")
        String title,

        @NotEmpty(message = "Please select at least one item for your set")
        List<Long> selectedItems
) {
}
