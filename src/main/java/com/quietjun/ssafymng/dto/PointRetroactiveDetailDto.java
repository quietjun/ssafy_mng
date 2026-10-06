package com.quietjun.ssafymng.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointRetroactiveDetailDto {
    private String sno;
    private String name;
    private int solvedTotal;
    private int workshopSolvedCount;
    private int assignmentSolvedCount;
    private int spentPoints;

    private int previousPoints;
    private int newPoints;
    private int pointDelta;

    private int previousTotalEarned;
    private int newTotalEarned;
    private int totalEarnedDelta;

    private boolean changed;
}
