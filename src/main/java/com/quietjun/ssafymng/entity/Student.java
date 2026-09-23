package com.quietjun.ssafymng.entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.quietjun.ssafymng.dto.StudentDto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "students")
@DynamicInsert
@DynamicUpdate
public class Student {

    @Id
    @Column(length = 50)
    private String sno;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Role role = Role.ROLE_STUDENT;

    @Column(columnDefinition = "int default 1")
    @Builder.Default
    private int presentationPoint = 1;

    @Column(nullable = true)
    private Integer srow;

    @Column(nullable = true)
    private Integer scol;

    @Column(nullable = true)
    private Integer solved;

    @Builder.Default
    private boolean escape = false;

    @Builder.Default
    private boolean passwordChanged = false;

    @Column(length = 50, columnDefinition = "varchar(50) default '여행'")
    @Builder.Default
    private String domain = "여행";

    @Column(columnDefinition = "boolean default true")
    @Builder.Default
    private boolean cert = true;

    @Column(columnDefinition = "int default 0")
    @Builder.Default
    private int points = 0;

    @Column(columnDefinition = "int default 0")
    @Builder.Default
    private int totalPointsEarned = 0;

    @Column(length = 50, columnDefinition = "varchar(50) default 'robot'")
    @Builder.Default
    private String equippedAvatar = "robot";

    @Column(length = 50, columnDefinition = "varchar(50) default 'none'")
    @Builder.Default
    private String equippedFrame = "none";

    @Column(length = 50, columnDefinition = "varchar(50) default 'default'")
    @Builder.Default
    private String equippedTheme = "default";

    @Column(length = 100, columnDefinition = "varchar(100) default '새싹 개발자'")
    @Builder.Default
    private String equippedTitle = "새싹 개발자";

    @Column(length = 50, columnDefinition = "varchar(50) default 'banner-default'")
    @Builder.Default
    private String equippedBanner = "banner-default";

    @Column(columnDefinition = "TEXT")
    @Builder.Default
    private String unlockedItems = "robot,none,default,새싹 개발자,banner-default";

    public StudentDto toDto() {
        return StudentDto.builder()
                .sno(sno)
                .name(name)
                .role(role)
                .presentationPoint(presentationPoint)
                .srow(srow)
                .scol(scol)
                .solved(solved)
                .escape(escape)
                .passwordChanged(passwordChanged)
                .domain(domain != null ? domain : "여행")
                .cert(cert)
                .points(points)
                .totalPointsEarned(totalPointsEarned)
                .equippedAvatar(equippedAvatar != null ? equippedAvatar : "robot")
                .equippedFrame(equippedFrame != null ? equippedFrame : "none")
                .equippedTheme(equippedTheme != null ? equippedTheme : "default")
                .equippedTitle(equippedTitle != null ? equippedTitle : "새싹 개발자")
                .equippedBanner(equippedBanner != null ? equippedBanner : "banner-default")
                .unlockedItems(unlockedItems != null ? unlockedItems : "robot,none,default,새싹 개발자,banner-default")
                .build();
    }
}
