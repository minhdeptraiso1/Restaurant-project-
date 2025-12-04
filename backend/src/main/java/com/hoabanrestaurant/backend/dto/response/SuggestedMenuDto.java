package com.hoabanrestaurant.backend.dto.response;

import java.util.List;

public record SuggestedMenuDto(
        List<DishDto> dishes,
        List<ComboDto> combos
) {
}
