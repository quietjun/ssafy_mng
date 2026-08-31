package com.quietjun.ssafymng.service;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quietjun.ssafymng.dto.AiAnalysisResultDto;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AiCodeReviewService {

    @Autowired(required = false)
    private ChatModel chatModel;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiCodeReviewService() {
    }

    /**
     * Java 소스코드와 채점 결과 캡처 이미지를 Gemini AI로 분석합니다.
     */
    public AiAnalysisResultDto analyzeSubmission(String sourceCode, String imagePath) {
        if (chatModel == null) {
            log.warn("ChatModel이 구성되지 않아 기본 AI 분석 결과를 반환합니다.");
            return createFallbackResult(sourceCode, "Gemini API Key가 설정되지 않았거나 ChatModel 빈이 없습니다.");
        }

        try {
            String promptText = "당신은 알고리즘 및 Java 코드 리뷰 전문가이자 채점 결과 엄격 검수관입니다.\n" +
                "제공된 학생의 Java 소스코드와 첨부된 [채점 결과 캡처 이미지]를 분석하여 JSON으로 응답해주세요.\n\n" +
                "[중요 검수 규칙]\n" +
                "1. 캡처 이미지에서 다음 4가지 핵심 채점 정보를 반드시 판별하세요:\n" +
                "   - resultStatus: 채점 결과 (예: Pass, 맞았습니다!!, 100점, Fail, 오답, 시간초과 등)\n" +
                "   - memoryUsage: 메모리 사용량 (예: 396 kb, 15,200 KB, 54MB 등)\n" +
                "   - executionTime: 실행 시간 (예: 4 ms, 120 ms, 0.45s 등)\n" +
                "   - codeLength: 코드 길이 (예: 204 B, 1,234 B 등 - 캡처에 없으면 소스코드 바이트 길이 자동 계산)\n" +
                "   - submissionDateText: 제출 일시 (캡처에 보이는 일시 혹은 현재 일시)\n\n" +
                "2. 채점 결과 이미지 유효성 판정:\n" +
                "   - 만약 첨부된 이미지가 알고리즘 사이트(SWEA, 백준, 프로그래머스 등)의 채점 결과 화면이 아니거나,\n" +
                "   - 결과(Pass/Fail/맞았습니다), 실행시간, 메모리 등의 채점 정보를 전혀 식별할 수 없는 엉뚱한 이미지인 경우:\n" +
                "     \"isValidCapture\": false,\n" +
                "     \"errorMessage\": \"채점 결과 화면(결과, 메모리, 실행시간 등)을 인식할 수 없습니다.\"\n" +
                "   - 올바른 채점 결과 화면이거나 이미지가 없는 경우:\n" +
                "     \"isValidCapture\": true,\n" +
                "     \"errorMessage\": \"\"\n\n" +
                "3. Java 소스 코드 분석:\n" +
                "   - timeComplexity: 시간 복잡도 (빅오 표기법, 예: O(N log N), O(N), O(V + E) 등)\n" +
                "   - spaceComplexity: 공간 복잡도 (빅오 표기법, 예: O(N), O(1) 등)\n" +
                "   - keyIdea: 알고리즘의 핵심 해결 아이디어 및 자료구조 요약 (한국어로 2~3문장 요약)\n" +
                "   - feedback: 코드의 장점 또는 가독성/시간/메모리 최적화 관점의 짧은 피드백 (한국어 1~2문장)\n" +
                "   - keywords: 해당 알고리즘 문제 해결 및 코드에 실제로 사용된 가장 중요한 핵심 키워드를 배열로 추출\n\n" +
                "[반드시 아래 JSON 포맷으로만 출력하세요]:\n" +
                "{\n" +
                "  \"resultStatus\": \"Pass\",\n" +
                "  \"memoryUsage\": \"396 kb\",\n" +
                "  \"executionTime\": \"4 ms\",\n" +
                "  \"codeLength\": \"204 B\",\n" +
                "  \"submissionDateText\": \"2026-08-28 10:51\",\n" +
                "  \"timeComplexity\": \"O(N log N)\",\n" +
                "  \"spaceComplexity\": \"O(N)\",\n" +
                "  \"keyIdea\": \"...\",\n" +
                "  \"feedback\": \"...\",\n" +
                "  \"keywords\": [\"BFS\", \"큐\"],\n" +
                "  \"isValidCapture\": true,\n" +
                "  \"errorMessage\": \"\"\n" +
                "}\n\n" +
                "[학생의 Java 소스코드]:\n" + sourceCode;

            ChatClient chatClient = ChatClient.create(chatModel);
            String aiResponseText = null;

            if (imagePath != null && !imagePath.isBlank()) {
                File imgFile = new File(imagePath);
                if (imgFile.exists()) {
                    String mimeType = imgFile.getName().toLowerCase().endsWith(".png") ? "image/png" : "image/jpeg";
                    aiResponseText = chatClient.prompt()
                            .user(u -> u.text(promptText)
                                    .media(MimeTypeUtils.parseMimeType(mimeType), new FileSystemResource(imgFile)))
                            .call()
                            .content();
                }
            }

            if (aiResponseText == null) {
                aiResponseText = chatClient.prompt()
                        .user(promptText)
                        .call()
                        .content();
            }

            log.info("Gemini AI 분석 응답 원문: {}", aiResponseText);
            return parseAiResponse(aiResponseText, sourceCode);

        } catch (Exception e) {
            log.error("Gemini AI 코드 분석 중 오류 발생: {}", e.getMessage(), e);
            return createFallbackResult(sourceCode, "AI 분석 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private AiAnalysisResultDto parseAiResponse(String responseText, String sourceCode) {
        if (responseText == null || responseText.isBlank()) {
            return createFallbackResult(sourceCode, "AI 응답이 비어 있습니다.");
        }

        try {
            String cleaned = responseText.trim();
            if (cleaned.startsWith("```json")) {
                cleaned = cleaned.substring(7);
            } else if (cleaned.startsWith("```")) {
                cleaned = cleaned.substring(3);
            }
            if (cleaned.endsWith("```")) {
                cleaned = cleaned.substring(0, cleaned.length() - 3);
            }
            cleaned = cleaned.trim();

            return objectMapper.readValue(cleaned, AiAnalysisResultDto.class);
        } catch (Exception e) {
            log.warn("JSON 파싱 실패, 정규식 추출 시도: {}", e.getMessage());
            return extractByRegex(responseText, sourceCode);
        }
    }

    private AiAnalysisResultDto extractByRegex(String text, String sourceCode) {
        AiAnalysisResultDto dto = createFallbackResult(sourceCode, "AI 응답 형식 변환 완료");
        
        dto.setTimeComplexity(extractRegex(text, "\"timeComplexity\"\\s*:\\s*\"([^\"]+)\"", "O(N)"));
        dto.setSpaceComplexity(extractRegex(text, "\"spaceComplexity\"\\s*:\\s*\"([^\"]+)\"", "O(N)"));
        dto.setKeyIdea(extractRegex(text, "\"keyIdea\"\\s*:\\s*\"([^\"]+)\"", "제출된 소스코드 기반 알고리즘 구현"));
        dto.setFeedback(extractRegex(text, "\"feedback\"\\s*:\\s*\"([^\"]+)\"", "정상 제출되었습니다."));
        dto.setResultStatus(extractRegex(text, "\"resultStatus\"\\s*:\\s*\"([^\"]+)\"", "Pass"));
        dto.setExecutionTime(extractRegex(text, "\"executionTime\"\\s*:\\s*\"([^\"]+)\"", "-"));
        dto.setMemoryUsage(extractRegex(text, "\"memoryUsage\"\\s*:\\s*\"([^\"]+)\"", "-"));
        
        return dto;
    }

    private String extractRegex(String source, String regex, String defaultValue) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(source);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return defaultValue;
    }

    private AiAnalysisResultDto createFallbackResult(String sourceCode, String feedbackMsg) {
        return AiAnalysisResultDto.builder()
                .resultStatus("Pass")
                .executionTime("-")
                .memoryUsage("-")
                .codeLength(sourceCode != null ? sourceCode.length() + " B" : "-")
                .submissionDateText("-")
                .timeComplexity("O(N)")
                .spaceComplexity("O(1)")
                .keyIdea("Java 소스코드 과제 제출 완료")
                .feedback(feedbackMsg)
                .build();
    }
}
