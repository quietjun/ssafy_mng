package com.quietjun.ssafymng.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.quietjun.ssafymng.dto.ProblemDto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "problems")
public class Problem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate problemDate;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 50)
    @Builder.Default
    private String problemType = "과제"; // '과제' or '워크샵'

    @Column(length = 100)
    private String platformName; // e.g. SWEA, 백준, 프로그래머스

    @Column(length = 500)
    private String platformUrl;  // e.g. https://swexpertacademy.com

    @Column(columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public ProblemDto toDto() {
        return ProblemDto.builder()
                .id(id)
                .problemDate(problemDate)
                .title(title)
                .problemType(problemType != null ? problemType : "과제")
                .platformName(platformName)
                .platformUrl(platformUrl)
                .description(description)
                .createdAt(createdAt)
                .build();
    }
}
