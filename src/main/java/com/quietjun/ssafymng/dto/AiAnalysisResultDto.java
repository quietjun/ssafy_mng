package com.quietjun.ssafymng.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AiAnalysisResultDto {
    // 캡처 이미지 파싱 결과
    private String resultStatus;       // e.g. Pass, Fail, 맞았습니다!!
    private String memoryUsage;        // e.g. 396 kb
    private String executionTime;      // e.g. 4 ms
    private String codeLength;         // e.g. 204 B
    private String submissionDateText; // e.g. 2026-08-28 10:51

    // Java 코드 AI 분석 결과
    private String timeComplexity;     // e.g. O(N log N)
    private String spaceComplexity;    // e.g. O(N)
    private String keyIdea;            // 핵심 알고리즘 아이디어 요약 (2~3줄)
    private String feedback;           // 코드 리뷰 및 개선 제안
    @Builder.Default
    private java.util.List<String> keywords = new java.util.ArrayList<>(); // 핵심 키워드 10개

    // 캡처 검증 결과
    @Builder.Default
    private Boolean isValidCapture = true; // 채점 화면(결과/메모리/시간) 유효 여부
    private String errorMessage;
}
