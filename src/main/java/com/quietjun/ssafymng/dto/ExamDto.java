package com.quietjun.ssafymng.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.quietjun.ssafymng.entity.ExamCategory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExamDto {
    private Long id;
    private String title;
    private ExamCategory category;
    private String categoryName;
    private LocalDate examDate;
    private int perfectScore;
    private String description;
    private LocalDateTime createdAt;

    // Aggregated stats
    private int scoreCount;
    private double averageScore;
    private double maxScore;
    private double minScore;
}
