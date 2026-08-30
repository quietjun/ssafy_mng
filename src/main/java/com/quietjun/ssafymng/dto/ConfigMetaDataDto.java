package com.quietjun.ssafymng.dto;

import com.quietjun.ssafymng.entity.ConfigMetaData;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConfigMetaDataDto {
    private String keyword;
    private String label;
    private String value;

    public ConfigMetaData toEntity() {
        return ConfigMetaData.builder()
                .keyword(keyword)
                .label(label)
                .value(value)
                .build();
    }
}
