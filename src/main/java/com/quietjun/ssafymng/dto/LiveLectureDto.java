package com.quietjun.ssafymng.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LiveLectureDto {
    private Long id;
    private String term;
    private String location;
    private String subject;
    private String content;
    private String instructor;
    private LocalDate lectureDate;
    private String dayOfWeek;
    private String startTime;
    private String endTime;
    private String duration;
    private LocalDateTime createdAt;
}
