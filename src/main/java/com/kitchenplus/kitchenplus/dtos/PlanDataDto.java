package com.kitchenplus.kitchenplus.dtos;

import java.util.List;

public record PlanDataDto(
    List<SpacerNodeDto> spacers,
    List<ItemNodeDto> items
) {
}
