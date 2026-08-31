package com.quietjun.ssafymng.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.quietjun.ssafymng.dto.AdminScriptDto;

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
@Table(name = "admin_scripts")
public class AdminScript {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 255)
    private String description;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String scriptContent;

    @Column(nullable = false)
    @Builder.Default
    private int orderIndex = 0;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public AdminScriptDto toDto() {
        return AdminScriptDto.builder()
                .id(id)
                .title(title)
                .description(description)
                .scriptContent(scriptContent)
                .orderIndex(orderIndex)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
