package com.quietjun.ssafymng.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.quietjun.ssafymng.dto.PointRetroactiveRequest;
import com.quietjun.ssafymng.dto.PointRetroactiveResultDto;
import com.quietjun.ssafymng.service.PointShopService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/points")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPointController {

    private final PointShopService pointShopService;

    /**
     * 포인트 소급 적용 및 시뮬레이션(Dry Run) 실행
     */
    @PostMapping("/retroactive-apply")
    public ResponseEntity<PointRetroactiveResultDto> applyRetroactivePoints(
            @RequestBody(required = false) PointRetroactiveRequest request) {
        if (request == null) {
            request = new PointRetroactiveRequest();
        }
        PointRetroactiveResultDto result = pointShopService.applyRetroactivePoints(request);
        return ResponseEntity.ok(result);
    }

    /**
     * 전체 학생 포인트 보유 및 순환 현황 요약
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getPointSummary() {
        return ResponseEntity.ok(pointShopService.getPointSummary());
    }
}
