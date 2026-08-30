package com.quietjun.ssafymng.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubmissionDto {
    private Long id;
    private String sno;
    private String studentSno;
    private String studentName;
    private Long problemId;
    private String problemTitle;
    private LocalDate problemDate;
    private String sourceCode;
    private String originalFileName;
    private String resultImagePath;
    private String resultStatus;
    private String memoryUsage;
    private String executionTime;
    private String codeLength;
    private String submissionDateText;
    private LocalDateTime submittedAt;

    private boolean aiAnalyzed;
    private String aiTimeComplexity;
    private String aiSpaceComplexity;
    private String aiKeyIdea;
    private String aiFeedback;
    private String aiKeywords;
}
