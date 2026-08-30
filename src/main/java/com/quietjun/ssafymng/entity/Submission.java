package com.quietjun.ssafymng.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.quietjun.ssafymng.dto.SubmissionDto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "submissions")
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sno", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(columnDefinition = "MEDIUMTEXT", nullable = false)
    private String sourceCode;

    @Column(length = 255)
    private String originalFileName;

    @Column(length = 500)
    private String resultImagePath;

    @Column(length = 50)
    private String resultStatus; // e.g. Pass, Fail

    @Column(length = 50)
    private String memoryUsage; // e.g. 396 kb

    @Column(length = 50)
    private String executionTime; // e.g. 4 ms

    @Column(length = 50)
    private String codeLength; // e.g. 204 B

    @Column(length = 50)
    private String submissionDateText; // e.g. 2026-08-28 10:51

    @CreationTimestamp
    private LocalDateTime submittedAt;

    // AI Analysis Result
    @Builder.Default
    private boolean aiAnalyzed = false;

    @Column(length = 100)
    private String aiTimeComplexity;

    @Column(length = 100)
    private String aiSpaceComplexity;

    @Column(columnDefinition = "TEXT")
    private String aiKeyIdea;

    @Column(columnDefinition = "TEXT")
    private String aiFeedback;

    @Column(length = 500)
    private String aiKeywords;

    public SubmissionDto toDto() {
        return SubmissionDto.builder()
                .id(id)
                .sno(student != null ? student.getSno() : null)
                .studentSno(student != null ? student.getSno() : null)
                .studentName(student != null ? student.getName() : null)
                .problemId(problem != null ? problem.getId() : null)
                .problemTitle(problem != null ? problem.getTitle() : null)
                .problemDate(problem != null ? problem.getProblemDate() : null)
                .sourceCode(sourceCode)
                .originalFileName(originalFileName)
                .resultImagePath(resultImagePath)
                .resultStatus(resultStatus)
                .memoryUsage(memoryUsage)
                .executionTime(executionTime)
                .codeLength(codeLength)
                .submissionDateText(submissionDateText)
                .submittedAt(submittedAt)
                .aiAnalyzed(aiAnalyzed)
                .aiTimeComplexity(aiTimeComplexity)
                .aiSpaceComplexity(aiSpaceComplexity)
                .aiKeyIdea(aiKeyIdea)
                .aiFeedback(aiFeedback)
                .aiKeywords(aiKeywords)
                .build();
    }
}
