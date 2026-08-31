package com.quietjun.ssafymng.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExamScoreBulkRequest {
    private Long examId;
    private String csvText;
}
