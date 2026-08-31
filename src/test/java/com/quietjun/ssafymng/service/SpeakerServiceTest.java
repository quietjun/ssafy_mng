package com.quietjun.ssafymng.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.quietjun.ssafymng.dto.SeatSwapRequest;
import com.quietjun.ssafymng.dto.StudentDto;
import com.quietjun.ssafymng.entity.Role;
import com.quietjun.ssafymng.entity.Student;
import com.quietjun.ssafymng.repository.StudentRepository;

@ExtendWith(MockitoExtension.class)
class SpeakerServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private MetadataService metadataService;

    @InjectMocks
    private SpeakerService speakerService;

    @Nested
    @DisplayName("좌석 맞교환 테스트")
    class SwapSeats {

        @Test
        @DisplayName("두 좌석 위치의 학생 정보가 정상 교환된다")
        void swapSeats_Success() {
            // given
            Student s1 = Student.builder().sno("20240101").name("김싸피").srow(0).scol(0).build();
            Student s2 = Student.builder().sno("20240102").name("이싸피").srow(1).scol(1).build();

            SeatSwapRequest req = SeatSwapRequest.builder()
                    .srcRow(0).srcCol(0)
                    .targetRow(1).targetCol(1)
                    .build();

            given(studentRepository.findByRoleAndEscapeFalse(Role.ROLE_STUDENT))
                    .willReturn(List.of(s1, s2));

            // when
            boolean result = speakerService.swapSeats(req);

            // then
            assertThat(result).isTrue();
            assertThat(s1.getSrow()).isEqualTo(1);
            assertThat(s1.getScol()).isEqualTo(1);
            assertThat(s2.getSrow()).isEqualTo(0);
            assertThat(s2.getScol()).isEqualTo(0);
        }

        @Test
        @DisplayName("동일한 좌석 위치 교환 시 false를 반환한다")
        void swapSeats_SamePosition_ReturnsFalse() {
            // given
            SeatSwapRequest req = SeatSwapRequest.builder()
                    .srcRow(0).srcCol(0)
                    .targetRow(0).targetCol(0)
                    .build();

            // when
            boolean result = speakerService.swapSeats(req);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("발표자 추첨 테스트")
    class DrawSpeaker {

        @Test
        @DisplayName("후보 학번 목록이 주어지면 후보 중에서 발표자를 추첨한다")
        void drawSpeaker_Success() {
            // given
            Student s1 = Student.builder().sno("20240101").name("김싸피").presentationPoint(1).build();
            Student s2 = Student.builder().sno("20240102").name("이싸피").presentationPoint(1).build();

            given(studentRepository.findByRoleAndEscapeFalse(Role.ROLE_STUDENT))
                    .willReturn(List.of(s1, s2));

            // when
            StudentDto winner = speakerService.drawSpeaker(List.of("20240101"));

            // then
            assertThat(winner).isNotNull();
            assertThat(winner.getSno()).isEqualTo("20240101");
            assertThat(winner.getName()).isEqualTo("김싸피");
        }

        @Test
        @DisplayName("추첨 후보가 없으면 예외가 발생한다")
        void drawSpeaker_EmptyCandidates_ThrowsException() {
            // given
            given(studentRepository.findByRoleAndEscapeFalse(Role.ROLE_STUDENT))
                    .willReturn(List.of());

            // when & then
            assertThatThrownBy(() -> speakerService.drawSpeaker(List.of("20240101")))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("추첨 대상 학생이 없습니다");
        }
    }
}
