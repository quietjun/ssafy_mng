package com.quietjun.ssafymng.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PairSaveRequest {
    private String domain;
    private String title;
    private List<PairItem> pairs;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class PairItem {
        private String student1Sno;
        private String student1Name;
        private String student2Sno;
        private String student2Name;
    }
}
