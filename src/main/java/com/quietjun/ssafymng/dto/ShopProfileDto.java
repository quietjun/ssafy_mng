package com.quietjun.ssafymng.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShopProfileDto {
    private String sno;
    private String name;
    private int points;
    private int totalPointsEarned;
    private String equippedAvatar;
    private String equippedFrame;
    private String equippedTheme;
    private String equippedTitle;
    private String equippedBanner;
    private String equippedCursor;
    private List<String> unlockedItemIds;
}
