package com.kitchenplus.kitchenplus.dtos;

import com.kitchenplus.kitchenplus.data.models.Node;

import java.util.List;

public record ContourDataDto(
        List<Node> nodes
) {
}
