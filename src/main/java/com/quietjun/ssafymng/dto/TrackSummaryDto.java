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
public class TrackSummaryDto {
    private String trackName;
    private long lectureCount;
    private double percentage;
    private double totalHours;
    private double hoursPercentage; // 전체 강의 시간 중 이 트랙의 시간 점유 비율 (%)
    private List<String> instructors;
    private List<String> locations;
    private LocalDate minDate;
    private LocalDate maxDate;
}
