package com.quietjun.ssafymng.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LiveLectureBulkRequest {
    private String rawText;
    private Boolean append; // true: 기존 데이터에 추가, false: 기존 데이터 교체 (기본값: false)
}
