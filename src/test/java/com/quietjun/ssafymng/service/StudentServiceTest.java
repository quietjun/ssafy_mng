package com.quietjun.ssafymng.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.quietjun.ssafymng.dto.PasswordChangeRequest;
import com.quietjun.ssafymng.dto.StudentBulkImportRequest;
import com.quietjun.ssafymng.dto.StudentCreateRequest;
import com.quietjun.ssafymng.dto.StudentDto;
import com.quietjun.ssafymng.entity.Role;
import com.quietjun.ssafymng.entity.Student;
import com.quietjun.ssafymng.repository.StudentRepository;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private com.quietjun.ssafymng.repository.ExamScoreRepository examScoreRepository;

    @InjectMocks
    private StudentService studentService;

    @Nested
    @DisplayName("학생 목록 조회 테스트")
    class GetStudents {

        @Test
        @DisplayName("탈퇴하지 않은 일반 학생 목록만 정상 반환한다")
        void getStudentList_Success() {
            // given
            Student student1 = Student.builder().sno("20240101").name("김싸피").role(Role.ROLE_STUDENT).build();
            Student student2 = Student.builder().sno("20240102").name("이싸피").role(Role.ROLE_STUDENT).build();

            given(studentRepository.findByRoleAndEscapeFalse(Role.ROLE_STUDENT))
                    .willReturn(List.of(student1, student2));

            // when
            List<StudentDto> result = studentService.getStudentList();

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getSno()).isEqualTo("20240101");
            assertThat(result.get(1).getSno()).isEqualTo("20240102");
        }
    }

    @Nested
    @DisplayName("학생 생성 테스트")
    class CreateStudent {

        @Test
        @DisplayName("신규 학생 정보를 입력하면 정상 생성된다")
        void createStudent_Success() {
            // given
            StudentCreateRequest req = StudentCreateRequest.builder()
                    .sno("20240101")
                    .name("홍길동")
                    .role(Role.ROLE_STUDENT)
                    .build();

            given(studentRepository.existsById("20240101")).willReturn(false);
            given(passwordEncoder.encode("20240101")).willReturn("encoded_pw");

            Student savedStudent = Student.builder()
                    .sno("20240101")
                    .name("홍길동")
                    .password("encoded_pw")
                    .role(Role.ROLE_STUDENT)
                    .presentationPoint(1)
                    .build();

            given(studentRepository.save(any(Student.class))).willReturn(savedStudent);

            // when
            StudentDto result = studentService.createStudent(req);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getSno()).isEqualTo("20240101");
            assertThat(result.getName()).isEqualTo("홍길동");
            assertThat(result.getDomain()).isEqualTo("여행");
            assertThat(result.isCert()).isTrue();
        }

        @Test
        @DisplayName("이미 존재하는 학번으로 생성 시 예외가 발생한다")
        void createStudent_DuplicateSno_ThrowsException() {
            // given
            StudentCreateRequest req = StudentCreateRequest.builder()
                    .sno("20240101")
                    .name("홍길동")
                    .build();

            given(studentRepository.existsById("20240101")).willReturn(true);

            // when & then
            assertThatThrownBy(() -> studentService.createStudent(req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("이미 존재하는 학번입니다");
        }
    }

    @Nested
    @DisplayName("학생 삭제 테스트")
    class DeleteStudent {

        @Test
        @DisplayName("학생 삭제 요청 시 escape 상태가 true로 변경(소프트 삭제)된다")
        void deleteStudent_Success() {
            // given
            Student student = Student.builder()
                    .sno("20240101")
                    .name("홍길동")
                    .escape(false)
                    .build();

            given(studentRepository.findById("20240101")).willReturn(Optional.of(student));

            // when
            studentService.deleteStudent("20240101");

            // then
            assertThat(student.isEscape()).isTrue();
            verify(studentRepository).save(student);
        }
    }

    @Nested
    @DisplayName("비밀번호 변경 테스트")
    class ChangePassword {

        @Test
        @DisplayName("현재 비밀번호가 일치하면 새 비밀번호로 정상 변경된다")
        void changePassword_Success() {
            // given
            Student student = Student.builder()
                    .sno("20240101")
                    .password("encoded_old_pw")
                    .passwordChanged(false)
                    .build();

            PasswordChangeRequest req = PasswordChangeRequest.builder()
                    .currentPassword("old_pw")
                    .newPassword("new_pw_1234")
                    .build();

            given(studentRepository.findById("20240101")).willReturn(Optional.of(student));
            given(passwordEncoder.matches("old_pw", "encoded_old_pw")).willReturn(true);
            given(passwordEncoder.encode("new_pw_1234")).willReturn("encoded_new_pw");

            // when
            studentService.changePassword("20240101", req);

            // then
            assertThat(student.getPassword()).isEqualTo("encoded_new_pw");
            assertThat(student.isPasswordChanged()).isTrue();
        }

        @Test
        @DisplayName("현재 비밀번호가 불일치하면 예외가 발생한다")
        void changePassword_WrongCurrentPassword_ThrowsException() {
            // given
            Student student = Student.builder()
                    .sno("20240101")
                    .password("encoded_old_pw")
                    .build();

            PasswordChangeRequest req = PasswordChangeRequest.builder()
                    .currentPassword("wrong_pw")
                    .newPassword("new_pw_1234")
                    .build();

            given(studentRepository.findById("20240101")).willReturn(Optional.of(student));
            given(passwordEncoder.matches("wrong_pw", "encoded_old_pw")).willReturn(false);

            // when & then
            assertThatThrownBy(() -> studentService.changePassword("20240101", req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("현재 비밀번호가 일치하지 않습니다");
        }
    }

    @Nested
    @DisplayName("CSV 일괄 등록 테스트")
    class BulkImport {

        @Test
        @DisplayName("CSV 텍스트를 파싱하여 신규 및 기존 학생 정보를 등록 및 업데이트한다")
        void bulkImport_Success() {
            // given
            String csvData = "20240101, 강감찬, 1, 2\n20240102, 이순신, 2, 3";
            StudentBulkImportRequest req = new StudentBulkImportRequest(csvData);

            given(studentRepository.findById("20240101")).willReturn(Optional.empty());
            given(studentRepository.findById("20240102")).willReturn(Optional.empty());
            given(passwordEncoder.encode(any())).willReturn("encoded_sno");

            // when
            int importedCount = studentService.bulkImport(req);

            // then
            assertThat(importedCount).isEqualTo(2);
        }
    }
}
