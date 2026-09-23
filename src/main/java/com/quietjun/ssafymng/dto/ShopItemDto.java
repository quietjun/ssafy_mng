package com.quietjun.ssafymng.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShopItemDto {
    private String id;
    private String name;
    private String category; // AVATAR, FRAME, THEME, TITLE
    private String icon;
    private String description;
    private int price;
    private boolean defaultOwned;
    private String previewColor;
    private String previewClass;
}
