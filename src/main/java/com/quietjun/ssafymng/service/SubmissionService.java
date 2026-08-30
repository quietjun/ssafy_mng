package com.quietjun.ssafymng.service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.quietjun.ssafymng.dto.AiAnalysisResultDto;
import com.quietjun.ssafymng.dto.DailyMonitoringDto;
import com.quietjun.ssafymng.dto.ProblemDto;
import com.quietjun.ssafymng.dto.SubmissionDto;
import com.quietjun.ssafymng.entity.Problem;
import com.quietjun.ssafymng.entity.Role;
import com.quietjun.ssafymng.entity.Student;
import com.quietjun.ssafymng.entity.Submission;
import com.quietjun.ssafymng.repository.ProblemRepository;
import com.quietjun.ssafymng.repository.StudentRepository;
import com.quietjun.ssafymng.repository.SubmissionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final StudentRepository studentRepository;
    private final ProblemRepository problemRepository;
    private final AiCodeReviewService aiCodeReviewService;

    @Value("${app.upload.dir:./data/uploads}")
    private String uploadDir;

    /**
     * 1단계: 소스코드와 캡처 이미지(또는 수동 입력 채점 정보)를 AI로 검수/분석 (DB 저장 X, 이미지 임시 분석 후 즉시 삭제)
     */
    public AiAnalysisResultDto inspectWithAi(
            MultipartFile sourceFile, 
            MultipartFile imageFile, 
            String sourceCodeText,
            String manualExecutionTime,
            String manualMemoryUsage,
            String manualCodeLength,
            String manualResultStatus) {

        String finalSourceCode = "";
        if (sourceFile != null && !sourceFile.isEmpty()) {
            try {
                finalSourceCode = new String(sourceFile.getBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException("소스 파일 읽기에 실패했습니다.");
            }
        } else if (sourceCodeText != null && !sourceCodeText.isBlank()) {
            finalSourceCode = sourceCodeText;
        } else {
            throw new IllegalArgumentException("제출할 Java 소스코드 또는 파일이 필요합니다.");
        }

        Path tempImgPath = null;
        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                String ext = ".png";
                if (imageFile.getOriginalFilename() != null && imageFile.getOriginalFilename().contains(".")) {
                    ext = imageFile.getOriginalFilename().substring(imageFile.getOriginalFilename().lastIndexOf("."));
                }
                tempImgPath = Files.createTempFile("ai_inspect_", ext);
                imageFile.transferTo(tempImgPath.toFile());
            }

            AiAnalysisResultDto result = aiCodeReviewService.analyzeSubmission(
                    finalSourceCode,
                    tempImgPath != null ? tempImgPath.toAbsolutePath().toString() : null
            );

            // 수동 입력값이 있으면 우선 적용
            if (manualResultStatus != null && !manualResultStatus.isBlank()) {
                result.setResultStatus(manualResultStatus.trim());
            }
            if (manualExecutionTime != null && !manualExecutionTime.isBlank()) {
                result.setExecutionTime(manualExecutionTime.trim());
            }
            if (manualMemoryUsage != null && !manualMemoryUsage.isBlank()) {
                result.setMemoryUsage(manualMemoryUsage.trim());
            }
            if (manualCodeLength != null && !manualCodeLength.isBlank()) {
                result.setCodeLength(manualCodeLength.trim());
            } else if (result.getCodeLength() == null || result.getCodeLength().isBlank()) {
                result.setCodeLength(finalSourceCode.getBytes(StandardCharsets.UTF_8).length + " B");
            }

            return result;
        } catch (IOException e) {
            log.error("AI 검수 중 오류 발생", e);
            throw new RuntimeException("AI 이미지 검수 처리 실패: " + e.getMessage());
        } finally {
            if (tempImgPath != null) {
                try {
                    Files.deleteIfExists(tempImgPath);
                } catch (IOException ignored) {}
            }
        }
    }

    /**
     * 2단계: 학생이 확인/수정한 최종 텍스트 데이터 DB 저장 (이미지 저장 X, 텍스트만 영구 보관)
     */
    @Transactional
    public SubmissionDto saveFinalSubmission(String sno, com.quietjun.ssafymng.dto.FinalSubmissionRequest req) {
        Student student = studentRepository.findById(sno)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학생입니다: " + sno));

        Problem problem = problemRepository.findById(req.getProblemId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문제 ID입니다: " + req.getProblemId()));

        String keywordsStr = (req.getAiKeywords() != null && !req.getAiKeywords().isEmpty())
                ? String.join(",", req.getAiKeywords())
                : "";

        Submission submission = Submission.builder()
                .student(student)
                .problem(problem)
                .sourceCode(req.getSourceCode())
                .originalFileName(req.getOriginalFileName() != null ? req.getOriginalFileName() : "Solution.java")
                .resultImagePath(null) // 이미지 파일은 보관하지 않음
                .resultStatus(req.getResultStatus() != null ? req.getResultStatus() : "Pass")
                .memoryUsage(req.getMemoryUsage())
                .executionTime(req.getExecutionTime())
                .codeLength(req.getCodeLength())
                .submissionDateText(req.getSubmissionDateText())
                .aiAnalyzed(true)
                .aiTimeComplexity(req.getAiTimeComplexity())
                .aiSpaceComplexity(req.getAiSpaceComplexity())
                .aiKeyIdea(req.getAiKeyIdea())
                .aiFeedback(req.getAiFeedback())
                .aiKeywords(keywordsStr)
                .build();

        Submission saved = submissionRepository.save(submission);

        // 학생 해결 카운트 갱신
        long solvedCount = submissionRepository.countByStudent_SnoAndResultStatus(sno, "Pass");
        student.setSolved((int) solvedCount);
        studentRepository.save(student);

        return saved.toDto();
    }

    @Transactional(readOnly = true)
    public SubmissionDto getSubmission(Long id) {
        return submissionRepository.findById(id)
                .map(Submission::toDto)
                .orElseThrow(() -> new IllegalArgumentException("제출 내역을 찾을 수 없습니다: " + id));
    }

    /**
     * 해당 문제에 제출된 모든 풀이 이력 목록 (최신순 전체)
     */
    @Transactional(readOnly = true)
    public List<SubmissionDto> getSubmissionsByProblem(Long problemId) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문제 ID입니다: " + problemId));

        List<Submission> allSubs = submissionRepository.findByProblemOrderBySubmittedAtDesc(problem);

        return allSubs.stream()
                .map(Submission::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 특정 학생의 특정 문제에 대한 전체 제출 이력 (1차, 2차...)
     */
    @Transactional(readOnly = true)
    public List<SubmissionDto> getStudentSubmissionsForProblem(String sno, Long problemId) {
        Student student = studentRepository.findById(sno)
                .orElseThrow(() -> new IllegalArgumentException("학생을 찾을 수 없습니다: " + sno));
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new IllegalArgumentException("문제를 찾을 수 없습니다: " + problemId));

        return submissionRepository.findByStudentAndProblemOrderBySubmittedAtDesc(student, problem)
                .stream()
                .map(Submission::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SubmissionDto> getSubmissionsByStudent(String sno) {
        Student student = studentRepository.findById(sno)
                .orElseThrow(() -> new IllegalArgumentException("학생을 찾을 수 없습니다: " + sno));
        return submissionRepository.findByStudentOrderBySubmittedAtDesc(student)
                .stream()
                .map(Submission::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 일별 전체 학생들의 과제 제출 현황 모니터링
     */
    @Transactional(readOnly = true)
    public DailyMonitoringDto getDailyMonitoring(LocalDate date) {
        List<Problem> problems = problemRepository.findByProblemDateOrderByCreatedAtAsc(date);
        List<ProblemDto> problemDtos = problems.stream().map(Problem::toDto).collect(Collectors.toList());

        List<Student> students = studentRepository.findByRoleAndEscapeFalse(Role.ROLE_STUDENT);
        List<Submission> submissions = submissionRepository.findByProblemDateWithStudentAndProblem(date);

        // 학생별 전체 제출 목록 매핑
        Map<String, List<Submission>> studentSubmissionMap = submissions.stream()
                .collect(Collectors.groupingBy(s -> s.getStudent().getSno()));

        int submittedCount = 0;
        List<DailyMonitoringDto.StudentSubmissionStatusDto> statusList = new ArrayList<>();

        for (Student student : students) {
            List<Submission> subList = studentSubmissionMap.getOrDefault(student.getSno(), Collections.emptyList());
            boolean isSubmitted = !subList.isEmpty();
            if (isSubmitted) {
                submittedCount++;
            }

            // 최신순으로 정렬된 제출 요약 목록
            List<DailyMonitoringDto.SubmissionSummaryDto> summaryList = subList.stream()
                    .map(sub -> DailyMonitoringDto.SubmissionSummaryDto.builder()
                            .submissionId(sub.getId())
                            .problemId(sub.getProblem().getId())
                            .problemTitle(sub.getProblem().getTitle())
                            .resultStatus(sub.getResultStatus())
                            .executionTime(sub.getExecutionTime())
                            .memoryUsage(sub.getMemoryUsage())
                            .aiTimeComplexity(sub.getAiTimeComplexity())
                            .aiKeyIdea(sub.getAiKeyIdea())
                            .aiAnalyzed(sub.isAiAnalyzed())
                            .build())
                    .collect(Collectors.toList());

            statusList.add(DailyMonitoringDto.StudentSubmissionStatusDto.builder()
                    .sno(student.getSno())
                    .name(student.getName())
                    .srow(student.getSrow())
                    .scol(student.getScol())
                    .submitted(isSubmitted)
                    .submissions(summaryList)
                    .build());
        }

        statusList.sort((a, b) -> a.getName().compareTo(b.getName()));

        return DailyMonitoringDto.builder()
                .targetDate(date)
                .problems(problemDtos)
                .studentStatuses(statusList)
                .totalStudents(students.size())
                .submittedStudentsCount(submittedCount)
                .build();
    }
}
