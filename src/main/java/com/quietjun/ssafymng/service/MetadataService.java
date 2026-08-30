package com.quietjun.ssafymng.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quietjun.ssafymng.dto.ConfigMetaDataDto;
import com.quietjun.ssafymng.entity.ConfigMetaData;
import com.quietjun.ssafymng.repository.ConfigMetaDataRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MetadataService {

    private final ConfigMetaDataRepository metadataRepository;

    @Transactional(readOnly = true)
    public ConfigMetaDataDto find(String keyword) {
        return metadataRepository.findById(keyword)
                .map(ConfigMetaData::toDto)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<ConfigMetaDataDto> findAll() {
        return metadataRepository.findAll()
                .stream()
                .map(ConfigMetaData::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ConfigMetaDataDto save(ConfigMetaDataDto dto) {
        ConfigMetaData saved = metadataRepository.save(dto.toEntity());
        return saved.toDto();
    }
}
