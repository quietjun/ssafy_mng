package com.quietjun.ssafymng.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.quietjun.ssafymng.dto.FinalSubmissionRequest;
import com.quietjun.ssafymng.dto.SubmissionDto;
import com.quietjun.ssafymng.entity.Problem;
import com.quietjun.ssafymng.entity.Student;
import com.quietjun.ssafymng.entity.Submission;
import com.quietjun.ssafymng.repository.ProblemRepository;
import com.quietjun.ssafymng.repository.StudentRepository;
import com.quietjun.ssafymng.repository.SubmissionRepository;

@ExtendWith(MockitoExtension.class)
class SubmissionServiceTest {

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private ProblemRepository problemRepository;

    @Mock
    private AiCodeReviewService aiCodeReviewService;

    @InjectMocks
    private SubmissionService submissionService;

    @Nested
    @DisplayName("최종 제출 저장 테스트")
    class SaveFinalSubmission {

        @Test
        @DisplayName("학생 및 문제 정보가 유효하면 최종 제출 데이터가 정상 저장된다")
        void saveFinalSubmission_Success() {
            // given
            String sno = "20240101";
            Long problemId = 10L;

            Student student = Student.builder().sno(sno).name("홍길동").build();
            Problem problem = Problem.builder().id(problemId).title("홀수만 더하기").build();

            FinalSubmissionRequest req = FinalSubmissionRequest.builder()
                    .problemId(problemId)
                    .sourceCode("public class Solution {}")
                    .originalFileName("Solution.java")
                    .resultStatus("Pass")
                    .executionTime("120 ms")
                    .memoryUsage("24 MB")
                    .aiTimeComplexity("O(N)")
                    .aiKeywords(List.of("배열", "반복문"))
                    .build();

            Submission savedSubmission = Submission.builder()
                    .id(100L)
                    .student(student)
                    .problem(problem)
                    .sourceCode("public class Solution {}")
                    .resultStatus("Pass")
                    .executionTime("120 ms")
                    .memoryUsage("24 MB")
                    .aiKeywords("배열,반복문")
                    .build();

            given(studentRepository.findById(sno)).willReturn(Optional.of(student));
            given(problemRepository.findById(problemId)).willReturn(Optional.of(problem));
            given(submissionRepository.save(any(Submission.class))).willReturn(savedSubmission);

            // when
            SubmissionDto result = submissionService.saveFinalSubmission(sno, req);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(100L);
            assertThat(result.getStudentSno()).isEqualTo(sno);
            assertThat(result.getResultStatus()).isEqualTo("Pass");
            assertThat(result.getExecutionTime()).isEqualTo("120 ms");
        }

        @Test
        @DisplayName("존재하지 않는 학생 학번으로 제출 시 예외가 발생한다")
        void saveFinalSubmission_InvalidStudent_ThrowsException() {
            // given
            FinalSubmissionRequest req = FinalSubmissionRequest.builder().problemId(10L).build();
            given(studentRepository.findById("invalid_sno")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> submissionService.saveFinalSubmission("invalid_sno", req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("존재하지 않는 학생입니다");
        }
    }

    @Nested
    @DisplayName("문제별 제출 이력 조회 테스트")
    class GetSubmissions {

        @Test
        @DisplayName("문제 ID로 제출 목록을 반환한다")
        void getSubmissionsByProblemId_Success() {
            // given
            Long problemId = 10L;
            Problem problem = Problem.builder().id(problemId).title("홀수만 더하기").build();
            Student student = Student.builder().sno("20240101").name("김싸피").build();

            Submission submission = Submission.builder()
                    .id(100L)
                    .problem(problem)
                    .student(student)
                    .resultStatus("Pass")
                    .build();

            given(problemRepository.findById(problemId)).willReturn(Optional.of(problem));
            given(submissionRepository.findByProblemOrderBySubmittedAtDesc(problem))
                    .willReturn(List.of(submission));

            // when
            List<SubmissionDto> result = submissionService.getSubmissionsByProblem(problemId);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(100L);
            assertThat(result.get(0).getStudentName()).isEqualTo("김싸피");
        }
    }
}
