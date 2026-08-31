package com.quietjun.ssafymng.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quietjun.ssafymng.dto.PairHistoryDto;
import com.quietjun.ssafymng.dto.PairSaveRequest;
import com.quietjun.ssafymng.entity.PairHistory;
import com.quietjun.ssafymng.repository.PairHistoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PairHistoryService {

    private final PairHistoryRepository pairHistoryRepository;

    @Transactional
    public List<PairHistoryDto> savePairs(PairSaveRequest req) {
        if (req.getDomain() == null || req.getDomain().isBlank()) {
            throw new IllegalArgumentException("도메인 정보가 필요합니다.");
        }
        if (req.getTitle() == null || req.getTitle().isBlank()) {
            throw new IllegalArgumentException("회차 명칭(제목)을 입력해주세요.");
        }
        if (req.getPairs() == null || req.getPairs().isEmpty()) {
            throw new IllegalArgumentException("저장할 페어 정보가 없습니다.");
        }

        List<PairHistory> savedList = new ArrayList<>();
        for (PairSaveRequest.PairItem item : req.getPairs()) {
            if (item.getStudent1Sno() == null || item.getStudent2Sno() == null) {
                continue;
            }
            PairHistory ph = PairHistory.builder()
                    .domain(req.getDomain())
                    .title(req.getTitle())
                    .student1Sno(item.getStudent1Sno())
                    .student1Name(item.getStudent1Name())
                    .student2Sno(item.getStudent2Sno())
                    .student2Name(item.getStudent2Name())
                    .build();
            savedList.add(pairHistoryRepository.save(ph));
        }

        return savedList.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PairHistoryDto> getHistoryByDomain(String domain) {
        return pairHistoryRepository.findByDomainOrderByCreatedAtDesc(domain)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PairHistoryDto> getAllHistory() {
        return pairHistoryRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteHistory(Long id) {
        pairHistoryRepository.deleteById(id);
    }

    @Transactional
    public void deleteHistoryByTitle(String domain, String title) {
        pairHistoryRepository.deleteByDomainAndTitle(domain, title);
    }

    private PairHistoryDto toDto(PairHistory entity) {
        return PairHistoryDto.builder()
                .id(entity.getId())
                .domain(entity.getDomain())
                .title(entity.getTitle())
                .student1Sno(entity.getStudent1Sno())
                .student1Name(entity.getStudent1Name())
                .student2Sno(entity.getStudent2Sno())
                .student2Name(entity.getStudent2Name())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
