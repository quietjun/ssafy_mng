package com.quietjun.ssafymng.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.quietjun.ssafymng.dto.LiveLectureDto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "live_lectures")
public class LiveLecture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50)
    private String term; // 구분 (e.g. 1학기)

    @Column(length = 100)
    private String location; // 장소 (e.g. 구미, 온택트룸4(17층))

    @Column(nullable = false, length = 150)
    private String subject; // 주제/트랙 (e.g. 코딩 Live강의 Mobile 트랙)

    @Column(columnDefinition = "TEXT")
    private String content; // 내용 (e.g. WEB : HTML5)

    @Column(length = 100)
    private String instructor; // 강사명 (e.g. 허태식 강사)

    private LocalDate lectureDate; // 날짜 (e.g. 2026-07-20)

    @Column(length = 20)
    private String dayOfWeek; // 요일 (e.g. 월)

    @Column(length = 30)
    private String startTime; // 방영시간 (e.g. 9:00)

    @Column(length = 30)
    private String endTime; // 종료시간 (e.g. 11:00)

    @Column(length = 30)
    private String duration; // 길이 (e.g. 2:00)

    @CreationTimestamp
    private LocalDateTime createdAt;

    public LiveLectureDto toDto() {
        return LiveLectureDto.builder()
                .id(id)
                .term(term)
                .location(location)
                .subject(subject)
                .content(content)
                .instructor(instructor)
                .lectureDate(lectureDate)
                .dayOfWeek(dayOfWeek)
                .startTime(startTime)
                .endTime(endTime)
                .duration(duration)
                .createdAt(createdAt)
                .build();
    }
}
