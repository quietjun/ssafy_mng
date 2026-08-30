package com.quietjun.ssafymng.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.quietjun.ssafymng.entity.Problem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProblemDto {
    private Long id;
    private LocalDate problemDate;
    private String title;
    private String problemType; // '과제' or '워크샵'
    private String platformName;
    private String platformUrl;
    private String description;
    private LocalDateTime createdAt;
    private long submissionCount;
    @com.fasterxml.jackson.annotation.JsonProperty("isSubmittedByMe")
    private boolean isSubmittedByMe;
    private String myResultStatus;

    public Problem toEntity() {
        return Problem.builder()
                .id(id)
                .problemDate(problemDate)
                .title(title)
                .problemType(problemType != null ? problemType : "과제")
                .platformName(platformName)
                .platformUrl(platformUrl)
                .description(description)
                .build();
    }
}
