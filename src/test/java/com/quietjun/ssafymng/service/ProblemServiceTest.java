package com.quietjun.ssafymng.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.quietjun.ssafymng.dto.ProblemCreateRequest;
import com.quietjun.ssafymng.dto.ProblemDto;
import com.quietjun.ssafymng.entity.Problem;
import com.quietjun.ssafymng.repository.ProblemRepository;
import com.quietjun.ssafymng.repository.SubmissionRepository;

@ExtendWith(MockitoExtension.class)
class ProblemServiceTest {

    @Mock
    private ProblemRepository problemRepository;

    @Mock
    private SubmissionRepository submissionRepository;

    @InjectMocks
    private ProblemService problemService;

    @Nested
    @DisplayName("문제 등록 테스트")
    class CreateProblem {

        @Test
        @DisplayName("새로운 문제를 생성하면 문제 DTO를 반환한다")
        void createProblem_Success() {
            // given
            LocalDate now = LocalDate.now();
            ProblemCreateRequest req = ProblemCreateRequest.builder()
                    .title("2072. 홀수만 더하기")
                    .problemType("과제")
                    .platformName("SWEA")
                    .problemDate(now)
                    .build();

            Problem savedProblem = Problem.builder()
                    .id(1L)
                    .title("2072. 홀수만 더하기")
                    .problemType("과제")
                    .platformName("SWEA")
                    .problemDate(now)
                    .build();

            given(problemRepository.save(any(Problem.class))).willReturn(savedProblem);

            // when
            ProblemDto result = problemService.createProblem(req);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getTitle()).isEqualTo("2072. 홀수만 더하기");
            assertThat(result.getProblemType()).isEqualTo("과제");
        }
    }

    @Nested
    @DisplayName("문제 조회 및 삭제 테스트")
    class GetAndDeleteProblem {

        @Test
        @DisplayName("존재하는 문제 ID로 조회 시 DTO와 제출 수를 반환한다")
        void getProblem_Success() {
            // given
            Problem problem = Problem.builder()
                    .id(1L)
                    .title("2072. 홀수만 더하기")
                    .problemType("과제")
                    .build();

            given(problemRepository.findById(1L)).willReturn(Optional.of(problem));
            given(submissionRepository.countByProblem(problem)).willReturn(5);

            // when
            ProblemDto result = problemService.getProblem(1L);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getSubmissionCount()).isEqualTo(5);
        }

        @Test
        @DisplayName("존재하지 않는 문제 ID 조회 시 예외가 발생한다")
        void getProblem_NotFound_ThrowsException() {
            // given
            given(problemRepository.findById(99L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> problemService.getProblem(99L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("존재하지 않는 문제 ID입니다");
        }

        @Test
        @DisplayName("문제 삭제 시 연관된 제출 데이터와 문제가 삭제된다")
        void deleteProblem_Success() {
            // given
            Problem problem = Problem.builder().id(1L).build();
            given(problemRepository.findById(1L)).willReturn(Optional.of(problem));

            // when
            problemService.deleteProblem(1L);

            // then
            verify(submissionRepository).deleteByProblem(problem);
            verify(problemRepository).delete(problem);
        }
    }
}
