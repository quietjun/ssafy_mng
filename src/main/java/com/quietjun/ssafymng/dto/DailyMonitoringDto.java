package com.quietjun.ssafymng.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DailyMonitoringDto {
    private LocalDate targetDate;
    private List<ProblemDto> problems;
    private List<StudentSubmissionStatusDto> studentStatuses;
    private int totalStudents;
    private int submittedStudentsCount;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class StudentSubmissionStatusDto {
        private String sno;
        private String name;
        private Integer srow;
        private Integer scol;
        private boolean submitted;
        private List<SubmissionSummaryDto> submissions;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class SubmissionSummaryDto {
        private Long submissionId;
        private Long problemId;
        private String problemTitle;
        private String resultStatus;
        private String executionTime;
        private String memoryUsage;
        private String aiTimeComplexity;
        private String aiKeyIdea;
        private boolean aiAnalyzed;
    }
}
