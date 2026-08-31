package com.quietjun.ssafymng.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.UpdateTimestamp;

import com.quietjun.ssafymng.dto.ExamScoreDto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "exam_scores", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"exam_id", "student_sno"})
})
public class ExamScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_sno", nullable = false)
    private Student student;

    @Column(nullable = false)
    private double score;

    @Column(length = 255)
    private String note;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public ExamScoreDto toDto() {
        return ExamScoreDto.builder()
                .id(id)
                .examId(exam != null ? exam.getId() : null)
                .examTitle(exam != null ? exam.getTitle() : null)
                .studentSno(student != null ? student.getSno() : null)
                .studentName(student != null ? student.getName() : null)
                .score(score)
                .note(note)
                .updatedAt(updatedAt)
                .build();
    }
}
