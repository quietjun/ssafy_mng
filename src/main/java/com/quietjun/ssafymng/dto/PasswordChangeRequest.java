package com.quietjun.ssafymng.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PasswordChangeRequest {
    @JsonAlias({"oldPassword", "currentPassword"})
    private String currentPassword;
    private String newPassword;
}

