package com.quietjun.ssafymng.dto;

import com.quietjun.ssafymng.entity.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StudentCreateRequest {
    private String sno;
    private String name;
    private String password;
    private Role role;
    private Integer srow;
    private Integer scol;
    @Builder.Default
    private int presentationPoint = 1;
    @Builder.Default
    private String domain = "여행";
    @Builder.Default
    private Boolean cert = true;
}
