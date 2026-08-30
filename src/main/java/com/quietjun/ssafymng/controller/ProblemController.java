package com.quietjun.ssafymng.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.quietjun.ssafymng.dto.ProblemCreateRequest;
import com.quietjun.ssafymng.dto.ProblemDto;
import com.quietjun.ssafymng.service.ProblemService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/problems")
@RequiredArgsConstructor
public class ProblemController {

    private final ProblemService problemService;

    @GetMapping
    public ResponseEntity<List<ProblemDto>> getProblemsByDate(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            org.springframework.security.core.Authentication authentication) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now();

        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // 학생은 미래 날짜의 문제를 조회할 수 없음
        if (!isAdmin && targetDate.isAfter(LocalDate.now())) {
            return ResponseEntity.ok(List.of());
        }

        return ResponseEntity.ok(problemService.getProblemsByDate(targetDate));
    }

    @GetMapping("/weekly")
    public ResponseEntity<List<ProblemDto>> getWeeklyProblems(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            org.springframework.security.core.Authentication authentication) {
        String currentSno = (authentication != null) ? authentication.getName() : null;
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        LocalDate today = LocalDate.now();
        LocalDate end = (endDate != null) ? endDate : today;
        if (!isAdmin && end.isAfter(today)) {
            end = today;
        }
        LocalDate start = (startDate != null) ? startDate : end.minusDays(6);

        return ResponseEntity.ok(problemService.getWeeklyProblems(currentSno, start, end));
    }

    @GetMapping("/all")
    public ResponseEntity<List<ProblemDto>> getAllProblems(
            org.springframework.security.core.Authentication authentication) {
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        List<ProblemDto> list = problemService.getAllProblems();
        if (!isAdmin) {
            LocalDate today = LocalDate.now();
            list = list.stream()
                    .filter(p -> p.getProblemDate() == null || !p.getProblemDate().isAfter(today))
                    .toList();
        }
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProblemDto> getProblem(
            @PathVariable Long id,
            org.springframework.security.core.Authentication authentication) {
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        ProblemDto problem = problemService.getProblem(id);
        if (!isAdmin && problem.getProblemDate() != null && problem.getProblemDate().isAfter(LocalDate.now())) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(problem);
    }

    @PostMapping
    public ResponseEntity<?> createProblem(@RequestBody ProblemCreateRequest req) {
        try {
            ProblemDto created = problemService.createProblem(req);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProblem(@PathVariable Long id, @RequestBody ProblemCreateRequest req) {
        try {
            ProblemDto updated = problemService.updateProblem(id, req);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProblem(@PathVariable Long id) {
        try {
            problemService.deleteProblem(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "문제가 삭제되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/fetch-metadata")
    public ResponseEntity<?> fetchMetadata(@RequestBody Map<String, String> body) {
        String url = body.get("url");
        if (url == null || url.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "URL을 입력해 주세요."));
        }
        try {
            var metadata = problemService.fetchProblemMetadata(url);
            return ResponseEntity.ok(metadata);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
