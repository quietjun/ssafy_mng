package com.quietjun.ssafymng.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.quietjun.ssafymng.dto.PointRetroactiveDetailDto;
import com.quietjun.ssafymng.dto.PointRetroactiveRequest;
import com.quietjun.ssafymng.dto.PointRetroactiveResultDto;
import com.quietjun.ssafymng.entity.Problem;
import com.quietjun.ssafymng.entity.Role;
import com.quietjun.ssafymng.entity.Student;
import com.quietjun.ssafymng.entity.Submission;
import com.quietjun.ssafymng.repository.StudentRepository;
import com.quietjun.ssafymng.repository.SubmissionRepository;

@ExtendWith(MockitoExtension.class)
class PointRetroactiveTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private SubmissionRepository submissionRepository;

    @InjectMocks
    private PointShopService pointShopService;

    @Test
    @DisplayName("포인트 소급 적용: 미반영분 차액(INCREMENTAL) 정상 가산 및 중복 제출 1회만 인정")
    void testIncrementalRetroactiveApply() {
        // Given
        Student s1 = Student.builder()
                .sno("1001")
                .name("김싸피")
                .role(Role.ROLE_STUDENT)
                .points(0)
                .totalPointsEarned(0)
                .build();

        Problem prob1 = Problem.builder().id(1L).title("과제1").problemType("과제").build();
        Problem prob2 = Problem.builder().id(2L).title("워크샵1").problemType("워크샵").build();

        // 김싸피는 과제1을 2번 Pass, 워크샵1을 1번 Pass
        Submission sub1 = Submission.builder().id(101L).student(s1).problem(prob1).resultStatus("Pass").build();
        Submission sub2 = Submission.builder().id(102L).student(s1).problem(prob1).resultStatus("Pass").build();
        Submission sub3 = Submission.builder().id(103L).student(s1).problem(prob2).resultStatus("Pass").build();

        given(studentRepository.findByRoleAndEscapeFalse(Role.ROLE_STUDENT)).willReturn(List.of(s1));
        given(submissionRepository.findAllPassedSubmissions()).willReturn(List.of(sub1, sub2, sub3));

        PointRetroactiveRequest req = PointRetroactiveRequest.builder()
                .workshopPoints(20)
                .assignmentPoints(10)
                .mode("INCREMENTAL")
                .dryRun(false)
                .build();

        // When
        PointRetroactiveResultDto result = pointShopService.applyRetroactivePoints(req);

        // Then
        assertThat(result.getTotalStudents()).isEqualTo(1);
        assertThat(result.getAffectedStudents()).isEqualTo(1);
        assertThat(result.getTotalPointsAwarded()).isEqualTo(30); // 10P (과제1) + 20P (워크샵1)

        PointRetroactiveDetailDto detail = result.getDetails().get(0);
        assertThat(detail.getSno()).isEqualTo("1001");
        assertThat(detail.getSolvedTotal()).isEqualTo(2); // 고유 문제 2개
        assertThat(detail.getWorkshopSolvedCount()).isEqualTo(1);
        assertThat(detail.getAssignmentSolvedCount()).isEqualTo(1);
        assertThat(detail.getNewPoints()).isEqualTo(30);
        assertThat(detail.getNewTotalEarned()).isEqualTo(30);
        assertThat(detail.getPointDelta()).isEqualTo(30);

        verify(studentRepository, times(1)).save(s1);
        assertThat(s1.getPoints()).isEqualTo(30);
        assertThat(s1.getTotalPointsEarned()).isEqualTo(30);
        assertThat(s1.getSolved()).isEqualTo(2);
    }

    @Test
    @DisplayName("포인트 소급 적용: DryRun(시뮬레이션) 시 DB save가 호출되지 않음")
    void testDryRunRetroactiveApply() {
        // Given
        Student s1 = Student.builder()
                .sno("1001")
                .name("김싸피")
                .role(Role.ROLE_STUDENT)
                .points(0)
                .totalPointsEarned(0)
                .build();

        Problem prob = Problem.builder().id(1L).title("과제1").problemType("과제").build();
        Submission sub = Submission.builder().id(101L).student(s1).problem(prob).resultStatus("Pass").build();

        given(studentRepository.findByRoleAndEscapeFalse(Role.ROLE_STUDENT)).willReturn(List.of(s1));
        given(submissionRepository.findAllPassedSubmissions()).willReturn(List.of(sub));

        PointRetroactiveRequest req = PointRetroactiveRequest.builder()
                .workshopPoints(20)
                .assignmentPoints(10)
                .dryRun(true)
                .build();

        // When
        PointRetroactiveResultDto result = pointShopService.applyRetroactivePoints(req);

        // Then
        assertThat(result.isDryRun()).isTrue();
        assertThat(result.getTotalPointsAwarded()).isEqualTo(10);
        assertThat(s1.getPoints()).isEqualTo(0); // 원본 객체 변경 안 됨 (save 호출 X)
        verify(studentRepository, never()).save(any());
    }

    @Test
    @DisplayName("포인트 소급 적용: RECALCULATE 모드 시 상점 구매 차감 정상 반영")
    void testRecalculateModeWithShopPurchases() {
        // Given: s1이 cat(가격 30)을 구매하여 unlockedItems에 포함됨
        Student s1 = Student.builder()
                .sno("1001")
                .name("이싸피")
                .role(Role.ROLE_STUDENT)
                .points(10)
                .totalPointsEarned(40)
                .unlockedItems("robot,none,default,새싹 개발자,banner-default,cat")
                .build();

        // 3문제 해결: 워크샵 1개 (20P) + 과제 2개 (20P) = 총 40P 획득
        Problem prob1 = Problem.builder().id(1L).title("과제1").problemType("과제").build();
        Problem prob2 = Problem.builder().id(2L).title("과제2").problemType("과제").build();
        Problem prob3 = Problem.builder().id(3L).title("워크샵1").problemType("워크샵").build();

        Submission sub1 = Submission.builder().id(101L).student(s1).problem(prob1).resultStatus("Pass").build();
        Submission sub2 = Submission.builder().id(102L).student(s1).problem(prob2).resultStatus("Pass").build();
        Submission sub3 = Submission.builder().id(103L).student(s1).problem(prob3).resultStatus("Pass").build();

        given(studentRepository.findByRoleAndEscapeFalse(Role.ROLE_STUDENT)).willReturn(List.of(s1));
        given(submissionRepository.findAllPassedSubmissions()).willReturn(List.of(sub1, sub2, sub3));

        PointRetroactiveRequest req = PointRetroactiveRequest.builder()
                .mode("RECALCULATE")
                .dryRun(false)
                .build();

        // When
        PointRetroactiveResultDto result = pointShopService.applyRetroactivePoints(req);

        // Then: 총 획득 40P - 고양이 구매 30P = 잔여 10P
        PointRetroactiveDetailDto detail = result.getDetails().get(0);
        assertThat(detail.getSpentPoints()).isEqualTo(30);
        assertThat(detail.getNewTotalEarned()).isEqualTo(40);
        assertThat(detail.getNewPoints()).isEqualTo(10);
    }
}
