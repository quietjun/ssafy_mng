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

import com.quietjun.ssafymng.dto.ExamCreateRequest;
import com.quietjun.ssafymng.dto.ExamDto;
import com.quietjun.ssafymng.dto.ExamScoreBulkRequest;
import com.quietjun.ssafymng.entity.Exam;
import com.quietjun.ssafymng.entity.ExamCategory;
import com.quietjun.ssafymng.entity.ExamScore;
import com.quietjun.ssafymng.entity.Student;
import com.quietjun.ssafymng.repository.ExamRepository;
import com.quietjun.ssafymng.repository.ExamScoreRepository;
import com.quietjun.ssafymng.repository.StudentRepository;

@ExtendWith(MockitoExtension.class)
class ExamServiceTest {

    @Mock
    private ExamRepository examRepository;

    @Mock
    private ExamScoreRepository examScoreRepository;

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private ExamService examService;

    @Nested
    @DisplayName("시험 등록 및 수정 테스트")
    class CreateAndUpdateExam {

        @Test
        @DisplayName("신규 시험 항목을 정상적으로 생성하고 DTO를 반환한다")
        void createExam_Success() {
            // given
            ExamCreateRequest req = ExamCreateRequest.builder()
                    .title("1월 과목평가 (Java)")
                    .category(ExamCategory.SUBJECT)
                    .examDate(LocalDate.of(2026, 1, 15))
                    .perfectScore(100)
                    .description("자바 기초 평가")
                    .build();

            Exam savedExam = Exam.builder()
                    .id(1L)
                    .title("1월 과목평가 (Java)")
                    .category(ExamCategory.SUBJECT)
                    .examDate(LocalDate.of(2026, 1, 15))
                    .perfectScore(100)
                    .description("자바 기초 평가")
                    .build();

            given(examRepository.save(any(Exam.class))).willReturn(savedExam);
            given(examScoreRepository.findByExam(savedExam)).willReturn(List.of());

            // when
            ExamDto result = examService.createExam(req);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getTitle()).isEqualTo("1월 과목평가 (Java)");
            assertThat(result.getCategory()).isEqualTo(ExamCategory.SUBJECT);
            assertThat(result.getCategoryName()).isEqualTo("과목평가");
        }

        @Test
        @DisplayName("시험 이름을 정상적으로 수정한다")
        void updateExam_Success() {
            // given
            Exam exam = Exam.builder()
                    .id(1L)
                    .title("구버전 시험명")
                    .category(ExamCategory.MONTHLY)
                    .perfectScore(100)
                    .build();

            ExamCreateRequest req = ExamCreateRequest.builder()
                    .title("수정된 2월 월말평가")
                    .category(ExamCategory.MONTHLY)
                    .perfectScore(100)
                    .build();

            given(examRepository.findById(1L)).willReturn(Optional.of(exam));
            given(examRepository.save(exam)).willReturn(exam);
            given(examScoreRepository.findByExam(exam)).willReturn(List.of());

            // when
            ExamDto result = examService.updateExam(1L, req);

            // then
            assertThat(result.getTitle()).isEqualTo("수정된 2월 월말평가");
        }
    }

    @Nested
    @DisplayName("성적 일괄 등록(CSV) 테스트")
    class BulkImportScores {

        @Test
        @DisplayName("CSV 텍스트(학번, 점수)를 파싱하여 점수를 일괄 등록/수정한다")
        void bulkImportScores_Success() {
            // given
            Exam exam = Exam.builder().id(1L).title("1월 과목평가").build();
            Student student1 = Student.builder().sno("20240101").name("김싸피").build();
            Student student2 = Student.builder().sno("20240102").name("이싸피").build();

            String csvData = "20240101, 95.5\n20240102\t이싸피\t88.0";
            ExamScoreBulkRequest req = new ExamScoreBulkRequest(1L, csvData);

            given(examRepository.findById(1L)).willReturn(Optional.of(exam));
            given(studentRepository.findById("20240101")).willReturn(Optional.of(student1));
            given(studentRepository.findById("20240102")).willReturn(Optional.of(student2));
            given(examScoreRepository.findByExamAndStudent(exam, student1)).willReturn(Optional.empty());
            given(examScoreRepository.findByExamAndStudent(exam, student2)).willReturn(Optional.empty());

            // when
            int count = examService.bulkImportScores(req);

            // then
            assertThat(count).isEqualTo(2);
            verify(examScoreRepository, org.mockito.Mockito.times(2)).save(any(ExamScore.class));
        }
    }

    @Nested
    @DisplayName("학생별 성적 조회 테스트")
    class GetScoresByStudent {

        @Test
        @DisplayName("특정 학생의 학번으로 성적 이력 목록을 정상적으로 반환한다")
        void getScoresByStudent_Success() {
            // given
            String sno = "20240101";
            Student student = Student.builder().sno(sno).name("김싸피").build();
            Exam exam = Exam.builder().id(1L).title("1월 과목평가").build();

            ExamScore score = ExamScore.builder()
                    .id(10L)
                    .exam(exam)
                    .student(student)
                    .score(95.0)
                    .build();

            given(examScoreRepository.findByStudent_Sno(sno)).willReturn(List.of(score));

            // when
            var result = examService.getScoresByStudent(sno);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStudentSno()).isEqualTo(sno);
            assertThat(result.get(0).getScore()).isEqualTo(95.0);
            assertThat(result.get(0).getExamTitle()).isEqualTo("1월 과목평가");
        }
    }
}
