package com.quietjun.ssafymng.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.quietjun.ssafymng.dto.ExamCreateRequest;
import com.quietjun.ssafymng.dto.ExamDto;
import com.quietjun.ssafymng.dto.ExamScoreBulkRequest;
import com.quietjun.ssafymng.dto.ExamScoreDto;
import com.quietjun.ssafymng.entity.ExamCategory;
import com.quietjun.ssafymng.service.ExamService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;

    @GetMapping
    public ResponseEntity<List<ExamDto>> getExams(@RequestParam(required = false) String category) {
        ExamCategory cat = null;
        if (category != null && !category.isBlank()) {
            cat = ExamCategory.fromString(category);
        }
        return ResponseEntity.ok(examService.getExams(cat));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExamDto> getExam(@PathVariable Long id) {
        return ResponseEntity.ok(examService.getExam(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ExamDto> createExam(@RequestBody ExamCreateRequest req) {
        return ResponseEntity.ok(examService.createExam(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ExamDto> updateExam(@PathVariable Long id, @RequestBody ExamCreateRequest req) {
        return ResponseEntity.ok(examService.updateExam(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteExam(@PathVariable Long id) {
        examService.deleteExam(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/scores")
    public ResponseEntity<List<ExamScoreDto>> getScoresByExam(@PathVariable Long id) {
        return ResponseEntity.ok(examService.getScoresByExam(id));
    }

    @GetMapping("/scores/student/{sno}")
    public ResponseEntity<List<ExamScoreDto>> getScoresByStudent(@PathVariable String sno) {
        return ResponseEntity.ok(examService.getScoresByStudent(sno));
    }

    @PostMapping("/{id}/scores")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ExamScoreDto> saveOrUpdateScore(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        String sno = (String) body.get("studentSno");
        double score = Double.parseDouble(String.valueOf(body.get("score")));
        String note = (String) body.get("note");
        return ResponseEntity.ok(examService.saveOrUpdateScore(id, sno, score, note));
    }

    @PostMapping("/scores/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> bulkImportScores(@RequestBody ExamScoreBulkRequest req) {
        int count = examService.bulkImportScores(req);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "count", count,
            "message", "총 " + count + "명의 시험 점수가 성공적으로 저장/업데이트되었습니다."
        ));
    }
}
