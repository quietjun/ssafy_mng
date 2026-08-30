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
public class FinalSubmissionRequest {
    private Long problemId;
    private String sourceCode;
    private String originalFileName;
    private String resultStatus;
    private String memoryUsage;
    private String executionTime;
    private String codeLength;
    private String submissionDateText;
    private String aiTimeComplexity;
    private String aiSpaceComplexity;
    private String aiKeyIdea;
    private String aiFeedback;
    private List<String> aiKeywords;
}
