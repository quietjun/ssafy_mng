package com.quietjun.ssafymng.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExamScoreDto {
    private Long id;
    private Long examId;
    private String examTitle;
    private String examCategory;
    private String examCategoryName;
    private double perfectScore;
    private String studentSno;
    private String studentName;
    private double score;
    private String note;
    private LocalDateTime updatedAt;
}
