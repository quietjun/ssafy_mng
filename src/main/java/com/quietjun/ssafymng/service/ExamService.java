package com.quietjun.ssafymng.service;

import java.time.LocalDate;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quietjun.ssafymng.dto.ExamCreateRequest;
import com.quietjun.ssafymng.dto.ExamDto;
import com.quietjun.ssafymng.dto.ExamScoreBulkRequest;
import com.quietjun.ssafymng.dto.ExamScoreDto;
import com.quietjun.ssafymng.entity.Exam;
import com.quietjun.ssafymng.entity.ExamCategory;
import com.quietjun.ssafymng.entity.ExamScore;
import com.quietjun.ssafymng.entity.Student;
import com.quietjun.ssafymng.repository.ExamRepository;
import com.quietjun.ssafymng.repository.ExamScoreRepository;
import com.quietjun.ssafymng.repository.StudentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExamService {

    private final ExamRepository examRepository;
    private final ExamScoreRepository examScoreRepository;
    private final StudentRepository studentRepository;

    @Transactional(readOnly = true)
    public List<ExamDto> getExams(ExamCategory category) {
        List<Exam> exams = (category != null)
                ? examRepository.findByCategoryOrderByCreatedAtDesc(category)
                : examRepository.findAllByOrderByCreatedAtDesc();

        return exams.stream().map(this::toDtoWithStats).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ExamDto getExam(Long id) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시험 ID입니다: " + id));
        return toDtoWithStats(exam);
    }

    @Transactional
    public ExamDto createExam(ExamCreateRequest req) {
        ExamCategory category = req.getCategory() != null ? req.getCategory() : ExamCategory.MONTHLY;
        LocalDate examDate = req.getExamDate() != null ? req.getExamDate() : LocalDate.now();
        int perfectScore = req.getPerfectScore() > 0 ? req.getPerfectScore() : 100;

        Exam exam = Exam.builder()
                .title(req.getTitle())
                .category(category)
                .examDate(examDate)
                .perfectScore(perfectScore)
                .description(req.getDescription())
                .build();

        return toDtoWithStats(examRepository.save(exam));
    }

    @Transactional
    public ExamDto updateExam(Long id, ExamCreateRequest req) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시험 ID입니다: " + id));

        if (req.getTitle() != null && !req.getTitle().isBlank()) {
            exam.setTitle(req.getTitle().trim());
        }
        if (req.getCategory() != null) {
            exam.setCategory(req.getCategory());
        }
        if (req.getExamDate() != null) {
            exam.setExamDate(req.getExamDate());
        }
        if (req.getPerfectScore() > 0) {
            exam.setPerfectScore(req.getPerfectScore());
        }
        if (req.getDescription() != null) {
            exam.setDescription(req.getDescription());
        }

        return toDtoWithStats(examRepository.save(exam));
    }

    @Transactional
    public void deleteExam(Long id) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시험 ID입니다: " + id));

        examScoreRepository.deleteByExam(exam);
        examRepository.delete(exam);
    }

    @Transactional(readOnly = true)
    public List<ExamScoreDto> getScoresByExam(Long examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시험 ID입니다: " + examId));

        return examScoreRepository.findByExam(exam)
                .stream()
                .map(ExamScore::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExamScoreDto> getScoresByStudent(String sno) {
        return examScoreRepository.findByStudent_Sno(sno)
                .stream()
                .map(ExamScore::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ExamScoreDto saveOrUpdateScore(Long examId, String sno, double score, String note) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시험 ID입니다: " + examId));
        Student student = studentRepository.findById(sno)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학생입니다: " + sno));

        Optional<ExamScore> existingOpt = examScoreRepository.findByExamAndStudent(exam, student);
        ExamScore examScore;
        if (existingOpt.isPresent()) {
            examScore = existingOpt.get();
            examScore.setScore(score);
            if (note != null) examScore.setNote(note);
        } else {
            examScore = ExamScore.builder()
                    .exam(exam)
                    .student(student)
                    .score(score)
                    .note(note)
                    .build();
        }

        return examScoreRepository.save(examScore).toDto();
    }

    @Transactional
    public int bulkImportScores(ExamScoreBulkRequest req) {
        if (req.getExamId() == null) {
            throw new IllegalArgumentException("시험 ID가 지정되지 않았습니다.");
        }
        if (req.getCsvText() == null || req.getCsvText().isBlank()) {
            return 0;
        }

        Exam exam = examRepository.findById(req.getExamId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시험 ID입니다: " + req.getExamId()));

        String[] lines = req.getCsvText().split("\\r?\\n");
        int count = 0;

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            String[] parts = line.split("[,\\t]");
            if (parts.length >= 2) {
                String sno = parts[0].trim();
                Double score = null;

                // 학번, 점수  또는  학번, 이름, 점수 형태 지원
                if (parts.length == 2) {
                    score = parseDoubleSafely(parts[1].trim());
                } else {
                    score = parseDoubleSafely(parts[2].trim());
                    if (score == null) {
                        score = parseDoubleSafely(parts[1].trim());
                    }
                }

                if (score == null) continue;

                Optional<Student> studentOpt = studentRepository.findById(sno);
                if (studentOpt.isPresent()) {
                    Student student = studentOpt.get();
                    Optional<ExamScore> scoreOpt = examScoreRepository.findByExamAndStudent(exam, student);

                    if (scoreOpt.isPresent()) {
                        ExamScore existing = scoreOpt.get();
                        existing.setScore(score);
                        examScoreRepository.save(existing);
                    } else {
                        ExamScore newScore = ExamScore.builder()
                                .exam(exam)
                                .student(student)
                                .score(score)
                                .build();
                        examScoreRepository.save(newScore);
                    }
                    count++;
                }
            }
        }
        return count;
    }

    private ExamDto toDtoWithStats(Exam exam) {
        ExamDto dto = exam.toDto();
        List<ExamScore> scores = examScoreRepository.findByExam(exam);
        dto.setScoreCount(scores.size());

        if (!scores.isEmpty()) {
            DoubleSummaryStatistics stats = scores.stream()
                    .mapToDouble(ExamScore::getScore)
                    .summaryStatistics();
            dto.setAverageScore(Math.round(stats.getAverage() * 10.0) / 10.0);
            dto.setMaxScore(stats.getMax());
            dto.setMinScore(stats.getMin());
        } else {
            dto.setAverageScore(0.0);
            dto.setMaxScore(0.0);
            dto.setMinScore(0.0);
        }
        return dto;
    }

    private Double parseDoubleSafely(String s) {
        try {
            return Double.parseDouble(s);
        } catch (Exception e) {
            return null;
        }
    }
}
