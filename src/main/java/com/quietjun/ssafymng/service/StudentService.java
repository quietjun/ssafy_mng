package com.quietjun.ssafymng.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quietjun.ssafymng.dto.PasswordChangeRequest;
import com.quietjun.ssafymng.dto.StudentBulkImportRequest;
import com.quietjun.ssafymng.dto.StudentCreateRequest;
import com.quietjun.ssafymng.dto.StudentDto;
import com.quietjun.ssafymng.entity.Role;
import com.quietjun.ssafymng.entity.Student;
import com.quietjun.ssafymng.repository.StudentRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void initDefaultAdmin() {
        if (studentRepository.countByRole(Role.ROLE_ADMIN) == 0) {
            log.info("기본 관리자 계정(admin)을 생성합니다.");
            Student admin = Student.builder()
                    .sno("admin")
                    .name("관리자")
                    .password(passwordEncoder.encode("1234"))
                    .role(Role.ROLE_ADMIN)
                    .passwordChanged(true)
                    .presentationPoint(1)
                    .build();
            studentRepository.save(admin);
        }
    }

    @Transactional(readOnly = true)
    public List<StudentDto> getStudentList() {
        return studentRepository.findByRoleAndEscapeFalse(Role.ROLE_STUDENT)
                .stream()
                .map(Student::toDto)
                .sorted()
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StudentDto> getAllActiveMembers() {
        return studentRepository.findByEscapeFalseOrderBySrowAscScolAsc()
                .stream()
                .map(Student::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StudentDto getStudent(String sno) {
        return studentRepository.findById(sno)
                .map(Student::toDto)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학생입니다: " + sno));
    }

    @Transactional
    public StudentDto createStudent(StudentCreateRequest req) {
        if (studentRepository.existsById(req.getSno())) {
            throw new IllegalArgumentException("이미 존재하는 학번입니다: " + req.getSno());
        }

        // 초기 비밀번호는 입력된 비밀번호가 없으면 학번(sno)으로 설정
        String rawPassword = (req.getPassword() != null && !req.getPassword().isBlank())
                ? req.getPassword()
                : req.getSno();

        Student student = Student.builder()
                .sno(req.getSno())
                .name(req.getName())
                .password(passwordEncoder.encode(rawPassword))
                .role(req.getRole() != null ? req.getRole() : Role.ROLE_STUDENT)
                .srow(req.getSrow())
                .scol(req.getScol())
                .presentationPoint(req.getPresentationPoint() > 0 ? req.getPresentationPoint() : 1)
                .passwordChanged(false)
                .escape(false)
                .build();

        return studentRepository.save(student).toDto();
    }

    @Transactional
    public StudentDto updateStudent(String sno, StudentCreateRequest req) {
        Student student = studentRepository.findById(sno)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학생입니다: " + sno));

        student.setName(req.getName());
        if (req.getSrow() != null) student.setSrow(req.getSrow());
        if (req.getScol() != null) student.setScol(req.getScol());
        if (req.getPresentationPoint() > 0) student.setPresentationPoint(req.getPresentationPoint());
        if (req.getRole() != null) student.setRole(req.getRole());

        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            student.setPassword(passwordEncoder.encode(req.getPassword()));
            student.setPasswordChanged(true);
        }

        return studentRepository.save(student).toDto();
    }

    @Transactional
    public void deleteStudent(String sno) {
        Student student = studentRepository.findById(sno)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학생입니다: " + sno));
        student.setEscape(true); // 소프트 삭제
        studentRepository.save(student);
    }

    @Transactional
    public void changePassword(String sno, PasswordChangeRequest req) {
        Student student = studentRepository.findById(sno)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + sno));

        if (!passwordEncoder.matches(req.getCurrentPassword(), student.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        if (req.getNewPassword() == null || req.getNewPassword().length() < 4) {
            throw new IllegalArgumentException("새 비밀번호는 4자리 이상이어야 합니다.");
        }

        student.setPassword(passwordEncoder.encode(req.getNewPassword()));
        student.setPasswordChanged(true);
        studentRepository.save(student);
    }

    @Transactional
    public int bulkImport(StudentBulkImportRequest req) {
        if (req.getCsvText() == null || req.getCsvText().isBlank()) {
            return 0;
        }

        String[] lines = req.getCsvText().split("\\r?\\n");
        int count = 0;

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            String[] parts = line.split("[,\\t]");
            if (parts.length >= 2) {
                String sno = parts[0].trim();
                String name = parts[1].trim();
                Integer srow = parts.length > 2 ? parseIntegerSafely(parts[2].trim()) : null;
                Integer scol = parts.length > 3 ? parseIntegerSafely(parts[3].trim()) : null;

                Optional<Student> existingOpt = studentRepository.findById(sno);
                if (existingOpt.isPresent()) {
                    Student existing = existingOpt.get();
                    existing.setName(name);
                    if (srow != null) existing.setSrow(srow);
                    if (scol != null) existing.setScol(scol);
                    existing.setEscape(false);
                    studentRepository.save(existing);
                } else {
                    Student newStudent = Student.builder()
                            .sno(sno)
                            .name(name)
                            .password(passwordEncoder.encode(sno)) // 초기 비번 = 학번
                            .role(Role.ROLE_STUDENT)
                            .srow(srow)
                            .scol(scol)
                            .presentationPoint(1)
                            .passwordChanged(false)
                            .escape(false)
                            .build();
                    studentRepository.save(newStudent);
                }
                count++;
            }
        }
        return count;
    }

    @Transactional
    public void save(StudentDto dto) {
        Student student = studentRepository.findById(dto.getSno()).orElse(null);
        if (student != null) {
            student.setName(dto.getName());
            student.setSrow(dto.getSrow());
            student.setScol(dto.getScol());
            student.setPresentationPoint(dto.getPresentationPoint());
            student.setSolved(dto.getSolved());
            studentRepository.save(student);
        }
    }

    private Integer parseIntegerSafely(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return null;
        }
    }
}
