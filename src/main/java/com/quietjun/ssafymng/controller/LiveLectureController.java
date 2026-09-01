package com.quietjun.ssafymng.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.quietjun.ssafymng.dto.LiveLectureBulkRequest;
import com.quietjun.ssafymng.dto.LiveLectureDto;
import com.quietjun.ssafymng.dto.LiveLectureSummaryResponse;
import com.quietjun.ssafymng.service.LiveLectureService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/lectures")
@RequiredArgsConstructor
public class LiveLectureController {

    private final LiveLectureService lectureService;

    @GetMapping
    public ResponseEntity<List<LiveLectureDto>> getAllLectures() {
        return ResponseEntity.ok(lectureService.getAllLectures());
    }

    @GetMapping("/summary")
    public ResponseEntity<LiveLectureSummaryResponse> getSummary() {
        return ResponseEntity.ok(lectureService.getSummary());
    }

    @PostMapping("/bulk")
    public ResponseEntity<?> processBulkText(@RequestBody LiveLectureBulkRequest req) {
        try {
            LiveLectureSummaryResponse summary = lectureService.processBulkText(req.getRawText(), req.getAppend());
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping
    public ResponseEntity<?> deleteAllLectures() {
        lectureService.deleteAllLectures();
        return ResponseEntity.ok(Map.of("success", true, "message", "전체 강의 데이터가 삭제되었습니다."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLecture(@PathVariable("id") Long id) {
        lectureService.deleteLecture(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "강의 데이터가 삭제되었습니다."));
    }
}
