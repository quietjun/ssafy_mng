package com.quietjun.ssafymng.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.quietjun.ssafymng.dto.LiveLectureSummaryResponse;
import com.quietjun.ssafymng.dto.TrackSummaryDto;
import com.quietjun.ssafymng.entity.LiveLecture;
import com.quietjun.ssafymng.repository.ConfigMetaDataRepository;
import com.quietjun.ssafymng.repository.LiveLectureRepository;

@ExtendWith(MockitoExtension.class)
class LiveLectureServiceTest {

    @Mock
    private LiveLectureRepository lectureRepository;

    @Mock
    private ConfigMetaDataRepository metadataRepository;

    @InjectMocks
    private LiveLectureService liveLectureService;

    private LiveLecture sampleJava;
    private LiveLecture samplePython;
    private LiveLecture sampleMobile;

    @BeforeEach
    void setUp() {
        sampleJava = LiveLecture.builder()
                .id(1L)
                .term("1학기")
                .location("온택트룸4")
                .subject("코딩 Live강의 Java 전공 트랙")
                .content("Java : 기본문법")
                .instructor("조용준 강사")
                .lectureDate(LocalDate.of(2026, 7, 20))
                .dayOfWeek("월")
                .startTime("9:00")
                .endTime("11:00")
                .duration("2:00")
                .build();

        samplePython = LiveLecture.builder()
                .id(2L)
                .term("1학기")
                .location("17층2호")
                .subject("코딩 Live강의 Python 트랙")
                .content("Python : Basic syntax 1")
                .instructor("김준호 강사")
                .lectureDate(LocalDate.of(2026, 7, 20))
                .dayOfWeek("월")
                .startTime("9:00")
                .endTime("11:00")
                .duration("2:00")
                .build();

        sampleMobile = LiveLecture.builder()
                .id(3L)
                .term("1학기")
                .location("구미")
                .subject("코딩 Live강의 Mobile 트랙")
                .content("WEB : HTML5")
                .instructor("허태식 강사")
                .lectureDate(LocalDate.of(2026, 7, 21))
                .dayOfWeek("화")
                .startTime("9:00")
                .endTime("11:00")
                .duration("2:00")
                .build();
    }

    @Test
    @DisplayName("트랙명이 정규화되어 그룹화되고 강의 횟수순으로 정렬된다")
    void testGetSummaryTrackNormalizationAndSorting() {
        when(lectureRepository.findAll()).thenReturn(List.of(sampleJava, samplePython, sampleMobile));

        LiveLectureSummaryResponse summary = liveLectureService.getSummary(null);

        assertThat(summary.getTotalLectures()).isEqualTo(3);
        assertThat(summary.getTotalTracks()).isEqualTo(3);
        
        List<TrackSummaryDto> tracks = summary.getTrackSummaries();
        assertThat(tracks).hasSize(3);

        List<String> trackNames = tracks.stream().map(TrackSummaryDto::getTrackName).toList();
        assertThat(trackNames).containsExactlyInAnyOrder("Java 전공 트랙", "Python 트랙", "Mobile 트랙");
    }

    @Test
    @DisplayName("bulkText 추가 시 동일한 강의 중복 저장을 방지하면서 타 트랙 누적은 허용한다")
    void testProcessBulkTextDeduplication() {
        when(lectureRepository.findAll()).thenReturn(List.of(sampleJava));

        String rawTextToAppend = "1학기\t온택트룸4\t코딩 Live강의 Java 전공 트랙\tJava : 기본문법\t조용준 강사\t2026-07-20\t월\t9:00\t11:00\t2:00\n" +
                "1학기\t17층2호\t코딩 Live강의 Python 트랙\tPython : Basic syntax 1\t김준호 강사\t2026-07-20\t월\t9:00\t11:00\t2:00";

        liveLectureService.processBulkText(rawTextToAppend, true);

        verify(lectureRepository).saveAll(any());
    }
}
