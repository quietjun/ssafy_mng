package com.quietjun.ssafymng.entity;

import com.quietjun.ssafymng.dto.ConfigMetaDataDto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "config_metadata")
public class ConfigMetaData {

    @Id
    @Column(length = 50)
    private String keyword;

    @Column(length = 100)
    private String label;

    @Column(length = 255)
    private String value;

    public ConfigMetaDataDto toDto() {
        return ConfigMetaDataDto.builder()
                .keyword(keyword)
                .label(label)
                .value(value)
                .build();
    }
}
