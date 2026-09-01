package com.quietjun.ssafymng.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LiveLectureSummaryResponse {
    private long totalLectures;
    private long totalTracks;
    private double totalHours;
    private LocalDate minLectureDate;
    private LocalDate maxLectureDate; // 언제까지 처리되었는지 (최신 강의 날짜)
    private LocalDateTime lastProcessedAt; // 데이터가 집계/처리된 시각
    private List<TrackSummaryDto> trackSummaries;
    private Map<String, Long> instructorCounts;
    private Map<String, Long> locationCounts;
    private List<LiveLectureDto> lectures;
}
