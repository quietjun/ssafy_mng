package com.quietjun.ssafymng.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProblemCreateRequest {
    private LocalDate problemDate;
    private String title;
    private String problemType; // '과제' or '워크샵'
    private String platformName;
    private String platformUrl;
    private String description;
}
