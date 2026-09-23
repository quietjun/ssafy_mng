package com.quietjun.ssafymng.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.quietjun.ssafymng.dto.ShopItemDto;
import com.quietjun.ssafymng.dto.ShopProfileDto;
import com.quietjun.ssafymng.service.PointShopService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/shop")
@RequiredArgsConstructor
public class PointShopController {

    private final PointShopService pointShopService;

    @GetMapping("/catalog")
    public ResponseEntity<List<ShopItemDto>> getCatalog() {
        return ResponseEntity.ok(pointShopService.getCatalog());
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyProfile(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        }
        try {
            ShopProfileDto profile = pointShopService.getProfile(authentication.getName());
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/buy")
    public ResponseEntity<?> buyItem(@RequestBody Map<String, String> body, Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        }
        String itemId = body.get("itemId");
        if (itemId == null || itemId.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "아이템 ID가 누락되었습니다."));
        }
        try {
            ShopProfileDto updated = pointShopService.buyItem(authentication.getName(), itemId.trim());
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/equip")
    public ResponseEntity<?> equipItem(@RequestBody Map<String, String> body, Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        }
        String category = body.get("category");
        String itemId = body.get("itemId");
        if (category == null || itemId == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "카테고리와 아이템 ID가 필요합니다."));
        }
        try {
            ShopProfileDto updated = pointShopService.equipItem(authentication.getName(), category.trim(), itemId.trim());
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/ranking")
    public ResponseEntity<List<ShopProfileDto>> getRanking() {
        return ResponseEntity.ok(pointShopService.getRanking());
    }
}
