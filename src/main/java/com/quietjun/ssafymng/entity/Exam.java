package com.quietjun.ssafymng.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.quietjun.ssafymng.dto.ExamDto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "exams")
@DynamicInsert
@DynamicUpdate
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private ExamCategory category = ExamCategory.MONTHLY;

    @Column(nullable = true)
    private LocalDate examDate;

    @Column(nullable = false)
    @Builder.Default
    private int perfectScore = 100;

    @Column(columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public ExamDto toDto() {
        return ExamDto.builder()
                .id(id)
                .title(title)
                .category(category)
                .categoryName(category != null ? category.getDescription() : "기타평가")
                .examDate(examDate)
                .perfectScore(perfectScore)
                .description(description)
                .createdAt(createdAt)
                .build();
    }
}
