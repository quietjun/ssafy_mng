package com.quietjun.ssafymng.dto;

import com.quietjun.ssafymng.entity.Role;
import com.quietjun.ssafymng.entity.Student;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StudentDto implements Comparable<StudentDto> {
    private String sno;
    private String name;
    private Role role;
    @Builder.Default
    private int presentationPoint = 1;
    private Integer srow;
    private Integer scol;
    private Integer solved;
    @Builder.Default
    private boolean escape = false;
    @Builder.Default
    private boolean passwordChanged = false;
    @Builder.Default
    private boolean isCandidate = true;
    private Double totalExamScore;
    @Builder.Default
    private String domain = "여행";
    @Builder.Default
    private boolean cert = true;
    @Builder.Default
    private int points = 0;
    @Builder.Default
    private int totalPointsEarned = 0;
    @Builder.Default
    private String equippedAvatar = "robot";
    @Builder.Default
    private String equippedFrame = "none";
    @Builder.Default
    private String equippedTheme = "default";
    @Builder.Default
    private String equippedTitle = "새싹 개발자";
    @Builder.Default
    private String equippedBanner = "banner-default";
    @Builder.Default
    private String equippedCursor = "cursor-default";
    @Builder.Default
    private String unlockedItems = "robot,none,default,새싹 개발자,banner-default,cursor-default";

    @Override
    public int compareTo(StudentDto o) {
        if (this.name == null && o.name == null) return 0;
        if (this.name == null) return 1;
        if (o.name == null) return -1;
        return this.name.compareTo(o.name);
    }

    public Student toEntity(String encodedPassword) {
        return Student.builder()
                .sno(sno)
                .password(encodedPassword)
                .name(name)
                .role(role != null ? role : Role.ROLE_STUDENT)
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
                .equippedCursor(equippedCursor != null ? equippedCursor : "cursor-default")
                .unlockedItems(unlockedItems != null ? unlockedItems : "robot,none,default,새싹 개발자,banner-default,cursor-default")
                .build();
    }
}
