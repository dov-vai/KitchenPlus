package com.kitchenplus.kitchenplus.dtos;

public record SetItemDto(
        Long id,
        Long itemId,
        String name,
        String description,
        double price,
        int height,
        int width,
        String imageLink
) {
}
