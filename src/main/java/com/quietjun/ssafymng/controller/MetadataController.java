package com.quietjun.ssafymng.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.quietjun.ssafymng.dto.ConfigMetaDataDto;
import com.quietjun.ssafymng.service.MetadataService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/metadata")
@RequiredArgsConstructor
public class MetadataController {

    private final MetadataService metadataService;

    @GetMapping
    public ResponseEntity<List<ConfigMetaDataDto>> getAll() {
        return ResponseEntity.ok(metadataService.findAll());
    }

    @GetMapping("/grid-config")
    public ResponseEntity<Map<String, Object>> getGridConfig() {
        ConfigMetaDataDto rMeta = metadataService.find("classrow");
        ConfigMetaDataDto pMeta = metadataService.find("classcol_pattern");
        int rows = 6;
        if (rMeta != null && rMeta.getValue() != null && !rMeta.getValue().isBlank()) {
            try { rows = Integer.parseInt(rMeta.getValue()); } catch (Exception ignored) {}
        }
        String pattern = (pMeta != null && pMeta.getValue() != null && !pMeta.getValue().isBlank()) 
                ? pMeta.getValue().trim() : "2,3";
        return ResponseEntity.ok(Map.of("rows", rows, "colsPattern", pattern));
    }

    @PostMapping("/grid-config")
    public ResponseEntity<Map<String, Object>> saveGridConfig(@RequestBody Map<String, Object> body) {
        if (body.containsKey("rows")) {
            metadataService.save(ConfigMetaDataDto.builder()
                    .keyword("classrow")
                    .value(String.valueOf(body.get("rows")))
                    .build());
        }
        if (body.containsKey("colsPattern")) {
            String pattern = String.valueOf(body.get("colsPattern")).trim();
            metadataService.save(ConfigMetaDataDto.builder()
                    .keyword("classcol_pattern")
                    .value(pattern)
                    .build());
        }
        return ResponseEntity.ok(Map.of("success", true, "message", "좌석 그리드 설정이 저장되었습니다."));
    }

    @PostMapping
    public ResponseEntity<ConfigMetaDataDto> save(@RequestBody ConfigMetaDataDto dto) {
        return ResponseEntity.ok(metadataService.save(dto));
    }
}
