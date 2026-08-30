package com.quietjun.ssafymng.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quietjun.ssafymng.dto.ConfigMetaDataDto;
import com.quietjun.ssafymng.dto.SeatSwapRequest;
import com.quietjun.ssafymng.dto.StudentDto;
import com.quietjun.ssafymng.dto.TodaySpeakerDto;
import com.quietjun.ssafymng.entity.Role;
import com.quietjun.ssafymng.entity.Student;
import com.quietjun.ssafymng.repository.StudentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpeakerService {

    private final StudentRepository studentRepository;
    private final MetadataService metadataService;
    private final Random random = new Random();

    public int getRowsCount() {
        ConfigMetaDataDto rMeta = metadataService.find("classrow");
        if (rMeta != null && rMeta.getValue() != null && !rMeta.getValue().isBlank()) {
            try {
                return Integer.parseInt(rMeta.getValue());
            } catch (Exception ignored) {}
        }
        return 6;
    }

    public String getColPattern() {
        ConfigMetaDataDto pMeta = metadataService.find("classcol_pattern");
        if (pMeta != null && pMeta.getValue() != null && !pMeta.getValue().isBlank()) {
            return pMeta.getValue().trim();
        }
        ConfigMetaDataDto cMeta = metadataService.find("classcol");
        if (cMeta != null && cMeta.getValue() != null && !cMeta.getValue().isBlank()) {
            return cMeta.getValue().trim();
        }
        return "2,3"; // 기본 2분단 (2열, 3열 = 총 5열)
    }

    public List<Integer> getColGroups() {
        String pattern = getColPattern();
        List<Integer> groups = new ArrayList<>();

        // 콜론(:) 또는 쉼표(,) 구분
        String[] parts = pattern.split("[,:]");
        for (String part : parts) {
            try {
                int val = Integer.parseInt(part.trim());
                if (val > 0) groups.add(val);
            } catch (Exception ignored) {}
        }

        if (groups.isEmpty()) {
            groups.add(2);
            groups.add(3);
        } else if (groups.size() == 1) {
            int total = groups.get(0);
            groups.clear();
            if (total == 5) {
                groups.add(2);
                groups.add(3);
            } else if (total == 6) {
                groups.add(3);
                groups.add(3);
            } else if (total == 4) {
                groups.add(2);
                groups.add(2);
            } else {
                int half = total / 2;
                groups.add(half);
                groups.add(total - half);
            }
        }
        return groups;
    }

    public int getColsCount() {
        return getColGroups().stream().mapToInt(Integer::intValue).sum();
    }

    @Transactional
    public TodaySpeakerDto getSeatLayout() {
        int rows = getRowsCount();
        List<Integer> colGroups = getColGroups();
        int cols = colGroups.stream().mapToInt(Integer::intValue).sum();
        String pattern = getColPattern();

        List<Student> students = studentRepository.findByRoleAndEscapeFalse(Role.ROLE_STUDENT);
        StudentDto[][] grid = new StudentDto[rows][cols];
        List<StudentDto> studentDtos = new ArrayList<>();
        List<Student> unassigned = new ArrayList<>();

        for (Student s : students) {
            StudentDto dto = s.toDto();
            dto.setCandidate(true);
            studentDtos.add(dto);

            if (s.getSrow() != null && s.getScol() != null) {
                int r = s.getSrow();
                int c = s.getScol();
                if (r >= 0 && r < rows && c >= 0 && c < cols && grid[r][c] == null) {
                    grid[r][c] = dto;
                } else {
                    unassigned.add(s);
                }
            } else {
                unassigned.add(s);
            }
        }

        // 미배정 학생 자동 빈자리 배치
        for (Student s : unassigned) {
            boolean placed = false;
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    if (grid[r][c] == null) {
                        grid[r][c] = s.toDto();
                        s.setSrow(r);
                        s.setScol(c);
                        studentRepository.save(s);
                        placed = true;
                        break;
                    }
                }
                if (placed) break;
            }
        }

        return TodaySpeakerDto.builder()
                .rows(rows)
                .cols(cols)
                .colPattern(pattern)
                .colGroups(colGroups)
                .students(studentDtos)
                .grid(grid)
                .build();
    }

    @Transactional
    public boolean swapSeats(SeatSwapRequest req) {
        if (req.getSrcRow() == req.getTargetRow() && req.getSrcCol() == req.getTargetCol()) {
            return false;
        }

        List<Student> students = studentRepository.findByRoleAndEscapeFalse(Role.ROLE_STUDENT);
        Student s1 = students.stream()
                .filter(s -> s.getSrow() != null && s.getSrow() == req.getSrcRow()
                          && s.getScol() != null && s.getScol() == req.getSrcCol())
                .findFirst().orElse(null);

        Student s2 = students.stream()
                .filter(s -> s.getSrow() != null && s.getSrow() == req.getTargetRow()
                          && s.getScol() != null && s.getScol() == req.getTargetCol())
                .findFirst().orElse(null);

        if (s1 != null) {
            s1.setSrow(req.getTargetRow());
            s1.setScol(req.getTargetCol());
            studentRepository.save(s1);
        }
        if (s2 != null) {
            s2.setSrow(req.getSrcRow());
            s2.setScol(req.getSrcCol());
            studentRepository.save(s2);
        }

        return true;
    }

    @Transactional
    public boolean saveSeatLayout(List<StudentDto> studentList) {
        if (studentList == null) return false;
        for (StudentDto dto : studentList) {
            if (dto.getSno() == null) continue;
            studentRepository.findById(dto.getSno()).ifPresent(s -> {
                s.setSrow(dto.getSrow());
                s.setScol(dto.getScol());
                studentRepository.save(s);
            });
        }
        return true;
    }

    @Transactional(readOnly = true)
    public StudentDto drawSpeaker(List<String> candidateSnos) {
        List<Student> students = studentRepository.findByRoleAndEscapeFalse(Role.ROLE_STUDENT);
        List<Student> candidates;

        if (candidateSnos != null && !candidateSnos.isEmpty()) {
            candidates = students.stream()
                    .filter(s -> candidateSnos.contains(s.getSno()))
                    .collect(Collectors.toList());
        } else {
            candidates = students;
        }

        if (candidates.isEmpty()) {
            throw new IllegalStateException("추첨 대상 학생이 없습니다.");
        }

        List<Student> pool = new ArrayList<>();
        for (Student s : candidates) {
            int weight = s.getPresentationPoint() > 0 ? s.getPresentationPoint() : 1;
            for (int i = 0; i < weight; i++) {
                pool.add(s);
            }
        }

        if (pool.isEmpty()) {
            pool.addAll(candidates);
        }

        Student winner = pool.get(random.nextInt(pool.size()));
        return winner.toDto();
    }

    @Transactional
    public StudentDto updatePresentationPoint(String sno, int newPoint) {
        Student student = studentRepository.findById(sno)
                .orElseThrow(() -> new IllegalArgumentException("학생을 찾을 수 없습니다: " + sno));

        boolean couponEvent = false;
        if (newPoint >= 11) {
            newPoint = 1;
            couponEvent = true;
        } else if (newPoint < 1) {
            newPoint = 1;
        }

        student.setPresentationPoint(newPoint);
        Student saved = studentRepository.save(student);
        StudentDto dto = saved.toDto();
        if (couponEvent) {
            dto.setName(dto.getName() + " (☕ 커피 쿠폰 획득!)");
        }
        return dto;
    }
}
