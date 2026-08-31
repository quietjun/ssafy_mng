package com.quietjun.ssafymng.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PairHistoryDto {
    private Long id;
    private String domain;
    private String title;
    private String student1Sno;
    private String student1Name;
    private String student2Sno;
    private String student2Name;
    private LocalDateTime createdAt;
}
