package com.quietjun.ssafymng.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.quietjun.ssafymng.dto.AiAnalysisResultDto;
import com.quietjun.ssafymng.dto.DailyMonitoringDto;
import com.quietjun.ssafymng.dto.SubmissionDto;
import com.quietjun.ssafymng.service.SubmissionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    /**
     * 1단계: Java 소스코드 및 채점 캡처 이미지(또는 직접 입력한 채점결과) AI 검수 분석 (DB 저장 X)
     */
    @PostMapping(value = "/ai-inspect", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> inspectWithAi(
            @RequestPart(value = "sourceFile", required = false) MultipartFile sourceFile,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestParam(value = "sourceCodeText", required = false) String sourceCodeText,
            @RequestParam(value = "manualExecutionTime", required = false) String manualExecutionTime,
            @RequestParam(value = "manualMemoryUsage", required = false) String manualMemoryUsage,
            @RequestParam(value = "manualCodeLength", required = false) String manualCodeLength,
            @RequestParam(value = "manualResultStatus", required = false) String manualResultStatus) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "로그인이 필요합니다."));
        }

        try {
            AiAnalysisResultDto result = submissionService.inspectWithAi(
                    sourceFile, 
                    imageFile, 
                    sourceCodeText,
                    manualExecutionTime,
                    manualMemoryUsage,
                    manualCodeLength,
                    manualResultStatus
            );
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("AI 검수 실패", e);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * 2단계: 학생이 확인/수정한 최종 텍스트 데이터 제출 (DB 저장, 이미지 저장 X)
     */
    @PostMapping(consumes = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<?> submitFinalAssignment(@RequestBody com.quietjun.ssafymng.dto.FinalSubmissionRequest req) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "로그인이 필요합니다."));
        }

        try {
            SubmissionDto submission = submissionService.saveFinalSubmission(auth.getName(), req);
            return ResponseEntity.ok(submission);
        } catch (Exception e) {
            log.error("최종 과제 제출 실패", e);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubmissionDto> getSubmission(@PathVariable Long id) {
        return ResponseEntity.ok(submissionService.getSubmission(id));
    }

    @GetMapping("/problem/{problemId}")
    public ResponseEntity<List<SubmissionDto>> getSubmissionsByProblem(@PathVariable Long problemId) {
        return ResponseEntity.ok(submissionService.getSubmissionsByProblem(problemId));
    }

    @GetMapping("/problem/{problemId}/my-history")
    public ResponseEntity<?> getMySubmissionsForProblem(@PathVariable Long problemId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "로그인이 필요합니다."));
        }
        return ResponseEntity.ok(submissionService.getStudentSubmissionsForProblem(auth.getName(), problemId));
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMySubmissions() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "로그인이 필요합니다."));
        }
        return ResponseEntity.ok(submissionService.getSubmissionsByStudent(auth.getName()));
    }

    @GetMapping("/student/{sno}")
    public ResponseEntity<List<SubmissionDto>> getStudentSubmissions(@PathVariable String sno) {
        return ResponseEntity.ok(submissionService.getSubmissionsByStudent(sno));
    }

    @GetMapping("/monitoring")
    public ResponseEntity<DailyMonitoringDto> getDailyMonitoring(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        return ResponseEntity.ok(submissionService.getDailyMonitoring(targetDate));
    }
}
