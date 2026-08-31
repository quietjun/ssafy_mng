package com.quietjun.ssafymng.dto;

import java.time.LocalDate;

import com.quietjun.ssafymng.entity.ExamCategory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExamCreateRequest {
    private String title;
    private ExamCategory category;
    private LocalDate examDate;
    private int perfectScore;
    private String description;
}
