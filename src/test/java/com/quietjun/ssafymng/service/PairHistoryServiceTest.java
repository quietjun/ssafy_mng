package com.quietjun.ssafymng.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.quietjun.ssafymng.dto.PairHistoryDto;
import com.quietjun.ssafymng.dto.PairSaveRequest;
import com.quietjun.ssafymng.entity.PairHistory;
import com.quietjun.ssafymng.repository.PairHistoryRepository;

@ExtendWith(MockitoExtension.class)
class PairHistoryServiceTest {

    @Mock
    private PairHistoryRepository pairHistoryRepository;

    @InjectMocks
    private PairHistoryService pairHistoryService;

    @Test
    @DisplayName("페어 이력을 정상적으로 저장하고 DTO 목록을 반환한다")
    void savePairs_Success() {
        // given
        PairSaveRequest req = PairSaveRequest.builder()
                .domain("여행")
                .title("2026년 3월 페어")
                .pairs(List.of(
                        new PairSaveRequest.PairItem("20240101", "김싸피", "20240102", "이싸피")
                ))
                .build();

        PairHistory savedHistory = PairHistory.builder()
                .id(1L)
                .domain("여행")
                .title("2026년 3월 페어")
                .student1Sno("20240101")
                .student1Name("김싸피")
                .student2Sno("20240102")
                .student2Name("이싸피")
                .build();

        given(pairHistoryRepository.save(any(PairHistory.class))).willReturn(savedHistory);

        // when
        List<PairHistoryDto> result = pairHistoryService.savePairs(req);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("2026년 3월 페어");
        assertThat(result.get(0).getStudent1Sno()).isEqualTo("20240101");
        assertThat(result.get(0).getStudent2Sno()).isEqualTo("20240102");
        verify(pairHistoryRepository, times(1)).save(any(PairHistory.class));
    }

    @Test
    @DisplayName("특정 도메인의 과거 페어 이력을 최신순으로 조회한다")
    void getHistoryByDomain_Success() {
        // given
        PairHistory history1 = PairHistory.builder()
                .id(1L)
                .domain("여행")
                .title("2026년 3월 1차 페어")
                .student1Sno("20240101")
                .student2Sno("20240102")
                .build();

        given(pairHistoryRepository.findByDomainOrderByCreatedAtDesc("여행"))
                .willReturn(List.of(history1));

        // when
        List<PairHistoryDto> result = pairHistoryService.getHistoryByDomain("여행");

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDomain()).isEqualTo("여행");
    }
}
