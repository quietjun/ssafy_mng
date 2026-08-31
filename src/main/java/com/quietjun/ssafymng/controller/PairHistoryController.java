package com.quietjun.ssafymng.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.quietjun.ssafymng.dto.PairHistoryDto;
import com.quietjun.ssafymng.dto.PairSaveRequest;
import com.quietjun.ssafymng.service.PairHistoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/pairs/history")
@RequiredArgsConstructor
public class PairHistoryController {

    private final PairHistoryService pairHistoryService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> savePairs(@RequestBody PairSaveRequest req) {
        try {
            List<PairHistoryDto> saved = pairHistoryService.savePairs(req);
            return ResponseEntity.ok(Map.of("success", true, "count", saved.size(), "message", saved.size() + "개의 페어 이력이 저장되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<PairHistoryDto>> getHistory(@RequestParam(required = false) String domain) {
        if (domain != null && !domain.isBlank()) {
            return ResponseEntity.ok(pairHistoryService.getHistoryByDomain(domain));
        }
        return ResponseEntity.ok(pairHistoryService.getAllHistory());
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteHistoryItem(@org.springframework.web.bind.annotation.PathVariable Long id) {
        pairHistoryService.deleteHistory(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "페어 이력 항목이 삭제되었습니다."));
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/title")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteHistoryByTitle(@RequestParam String domain, @RequestParam String title) {
        pairHistoryService.deleteHistoryByTitle(domain, title);
        return ResponseEntity.ok(Map.of("success", true, "message", "'" + title + "' 페어 회차 이력이 전체 삭제되었습니다."));
    }
}
