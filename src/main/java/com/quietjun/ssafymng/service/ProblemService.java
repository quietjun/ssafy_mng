package com.quietjun.ssafymng.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quietjun.ssafymng.dto.ProblemCreateRequest;
import com.quietjun.ssafymng.dto.ProblemDto;
import com.quietjun.ssafymng.dto.ProblemMetadataDto;
import com.quietjun.ssafymng.entity.Problem;
import com.quietjun.ssafymng.repository.ProblemRepository;
import com.quietjun.ssafymng.repository.SubmissionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProblemService {

    private final ProblemRepository problemRepository;
    private final SubmissionRepository submissionRepository;

    @Transactional(readOnly = true)
    public List<ProblemDto> getProblemsByDate(LocalDate date) {
        return problemRepository.findByProblemDateOrderByCreatedAtAsc(date)
                .stream()
                .map(p -> {
                    ProblemDto dto = p.toDto();
                    dto.setSubmissionCount(submissionRepository.countByProblem(p));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProblemDto> getWeeklyProblems(String currentSno, LocalDate startDate, LocalDate endDate) {
        LocalDate start = (startDate != null) ? startDate : LocalDate.now().minusDays(6);
        LocalDate end = (endDate != null) ? endDate : LocalDate.now();

        List<Problem> problems = problemRepository.findByProblemDateBetweenOrderByProblemDateDescCreatedAtDesc(start, end);

        return problems.stream()
                .map(p -> {
                    ProblemDto dto = p.toDto();
                    dto.setSubmissionCount(submissionRepository.countByProblem(p));
                    if (currentSno != null && !currentSno.isBlank()) {
                        var mySubs = submissionRepository.findByStudent_SnoAndProblemOrderBySubmittedAtDesc(currentSno, p);
                        if (!mySubs.isEmpty()) {
                            dto.setSubmittedByMe(true);
                            dto.setMyResultStatus(mySubs.get(0).getResultStatus());
                        } else {
                            dto.setSubmittedByMe(false);
                        }
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProblemDto> getAllProblems() {
        return problemRepository.findAllByOrderByProblemDateDescCreatedAtDesc()
                .stream()
                .map(p -> {
                    ProblemDto dto = p.toDto();
                    dto.setSubmissionCount(submissionRepository.countByProblem(p));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProblemDto getProblem(Long id) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문제 ID입니다: " + id));
        ProblemDto dto = problem.toDto();
        dto.setSubmissionCount(submissionRepository.countByProblem(problem));
        return dto;
    }

    @Transactional
    public ProblemDto createProblem(ProblemCreateRequest req) {
        LocalDate date = req.getProblemDate() != null ? req.getProblemDate() : LocalDate.now();
        String type = (req.getProblemType() != null && !req.getProblemType().isBlank()) ? req.getProblemType().trim() : "과제";
        Problem problem = Problem.builder()
                .problemDate(date)
                .title(req.getTitle())
                .problemType(type)
                .platformName(req.getPlatformName())
                .platformUrl(req.getPlatformUrl())
                .description(req.getDescription())
                .build();
        return problemRepository.save(problem).toDto();
    }

    @Transactional
    public ProblemDto updateProblem(Long id, ProblemCreateRequest req) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문제 ID입니다: " + id));

        if (req.getProblemDate() != null) problem.setProblemDate(req.getProblemDate());
        if (req.getTitle() != null) problem.setTitle(req.getTitle());
        if (req.getProblemType() != null && !req.getProblemType().isBlank()) problem.setProblemType(req.getProblemType().trim());
        if (req.getPlatformName() != null) problem.setPlatformName(req.getPlatformName());
        if (req.getPlatformUrl() != null) problem.setPlatformUrl(req.getPlatformUrl());
        if (req.getDescription() != null) problem.setDescription(req.getDescription());

        return problemRepository.save(problem).toDto();
    }

    @Transactional
    public void deleteProblem(Long id) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문제 ID입니다: " + id));
        submissionRepository.deleteByProblem(problem);
        problemRepository.delete(problem);
    }

    private final org.springframework.ai.chat.model.ChatModel chatModel;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    /**
     * 알고리즘 문제 URL을 분석하여 문제 제목, 번호 및 설명을 자동으로 추출
     */
    public ProblemMetadataDto fetchProblemMetadata(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("URL이 유효하지 않습니다.");
        }

        String rawHtmlText = "";
        String ogTitle = "";
        try {
            org.jsoup.nodes.Document doc = org.jsoup.Jsoup.connect(url.trim())
                    .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .timeout(5000)
                    .get();

            org.jsoup.nodes.Element ogTitleEl = doc.selectFirst("meta[property=og:title]");
            if (ogTitleEl != null) {
                ogTitle = ogTitleEl.attr("content");
            }
            if (ogTitle.isBlank()) {
                ogTitle = doc.title();
            }

            // 본문 텍스트 일부 추출
            rawHtmlText = doc.body() != null ? doc.body().text() : "";
            if (rawHtmlText.length() > 2000) {
                rawHtmlText = rawHtmlText.substring(0, 2000);
            }
        } catch (Exception e) {
            log.warn("HTML 크롤링 실패 (AI 지식 기반으로 폴백 진행): {}", e.getMessage());
        }

        String prompt = String.format("""
            You are an expert algorithm problem metadata extractor.
            Given the algorithm problem URL, extracted title, and HTML snippet, determine the official Problem Name, Number, Platform (e.g. SWEA, Baekjoon/BOJ, Programmers, LeetCode), and Difficulty/Tier (if known).

            URL: %s
            Extracted Web Title: %s
            HTML Snippet: %s

            Instructions:
            - If it is SW Expert Academy (swexpertacademy.com):
              Identify the exact SWEA problem number and title matching the contestProbId or URL (e.g. 'SWEA 2072. 홀수만 더하기 (D1)' or 'SWEA 1204. 최빈수 구하기 (D2)').
            - If it is Baekjoon (acmicpc.net):
              Format title like '백준 12865. 평범한 배낭 (골드5)' or 'BOJ 1000. A+B'.
            - If it is Programmers:
              Format title like '프로그래머스: 타겟 넘버 (Lv.2)'.
            - In the description field, ONLY provide the original problem URL link as '문제 링크: %s' without any problem summary or text explanation.

            Output ONLY valid JSON in the following schema without markdown backticks:
            {
              "title": "SWEA 2072. 홀수만 더하기 (D1)",
              "description": "문제 링크: %s"
            }
            """, url.trim(), ogTitle, rawHtmlText, url.trim(), url.trim());

        try {
            String response = chatModel.call(prompt);
            String cleanedJson = response.trim();
            if (cleanedJson.startsWith("```json")) {
                cleanedJson = cleanedJson.substring(7);
            } else if (cleanedJson.startsWith("```")) {
                cleanedJson = cleanedJson.substring(3);
            }
            if (cleanedJson.endsWith("```")) {
                cleanedJson = cleanedJson.substring(0, cleanedJson.length() - 3);
            }
            cleanedJson = cleanedJson.trim();

            return objectMapper.readValue(cleanedJson, ProblemMetadataDto.class);
        } catch (Exception e) {
            log.error("AI 메타데이터 추출 실패: ", e);
            // 폴백 기본값 반환
            String fallbackTitle = (ogTitle != null && !ogTitle.isBlank() && !ogTitle.contains("SW Expert Academy"))
                    ? ogTitle
                    : "알고리즘 문제 (" + url + ")";
            return new ProblemMetadataDto(fallbackTitle, "문제 링크: " + url);
        }
    }
}
