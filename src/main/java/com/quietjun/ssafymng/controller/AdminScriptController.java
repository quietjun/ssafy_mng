package com.quietjun.ssafymng.controller;

import java.util.List;

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

import com.quietjun.ssafymng.dto.AdminScriptDto;
import com.quietjun.ssafymng.service.AdminScriptService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/scripts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminScriptController {

    private final AdminScriptService adminScriptService;

    @GetMapping
    public ResponseEntity<List<AdminScriptDto>> getAllScripts() {
        return ResponseEntity.ok(adminScriptService.getAllScripts());
    }

    @PostMapping
    public ResponseEntity<AdminScriptDto> createScript(@RequestBody AdminScriptDto dto) {
        return ResponseEntity.ok(adminScriptService.saveScript(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdminScriptDto> updateScript(@PathVariable Long id, @RequestBody AdminScriptDto dto) {
        dto.setId(id);
        return ResponseEntity.ok(adminScriptService.saveScript(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteScript(@PathVariable Long id) {
        adminScriptService.deleteScript(id);
        return ResponseEntity.noContent().build();
    }
}
