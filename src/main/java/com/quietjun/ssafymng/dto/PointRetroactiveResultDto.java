package com.quietjun.ssafymng.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointRetroactiveResultDto {
    private boolean dryRun;
    private String mode;
    private int workshopPoints;
    private int assignmentPoints;
    private int totalStudents;
    private int affectedStudents;
    private int totalPointsAwarded;
    private List<PointRetroactiveDetailDto> details;
}
