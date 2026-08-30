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
import org.springframework.web.bind.annotation.RestController;

import com.quietjun.ssafymng.entity.PlatformSite;
import com.quietjun.ssafymng.service.PlatformSiteService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/platforms")
@RequiredArgsConstructor
public class PlatformSiteController {

    private final PlatformSiteService platformSiteService;

    @GetMapping
    public ResponseEntity<List<PlatformSite>> getAllPlatforms() {
        return ResponseEntity.ok(platformSiteService.getAllPlatforms());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createPlatform(@RequestBody Map<String, String> body) {
        try {
            String name = body.get("name");
            String url = body.get("url");
            PlatformSite site = platformSiteService.createPlatform(name, url);
            return ResponseEntity.ok(site);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updatePlatform(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String name = body.get("name");
            String url = body.get("url");
            PlatformSite site = platformSiteService.updatePlatform(id, name, url);
            return ResponseEntity.ok(site);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletePlatform(@PathVariable Long id) {
        try {
            platformSiteService.deletePlatform(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "문제 출처 사이트가 삭제되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
