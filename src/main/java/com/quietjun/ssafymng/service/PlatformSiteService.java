package com.quietjun.ssafymng.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quietjun.ssafymng.entity.PlatformSite;
import com.quietjun.ssafymng.repository.PlatformSiteRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlatformSiteService {

    private final PlatformSiteRepository platformSiteRepository;

    @PostConstruct
    public void initDefaultPlatforms() {
        if (platformSiteRepository.count() == 0) {
            log.info("기본 문제 출처 사이트 초기화 진행");
            platformSiteRepository.saveAll(List.of(
                PlatformSite.builder().name("SWEA").url("https://swexpertacademy.com").build(),
                PlatformSite.builder().name("백준 (BOJ)").url("https://www.acmicpc.net").build(),
                PlatformSite.builder().name("프로그래머스").url("https://school.programmers.co.kr").build(),
                PlatformSite.builder().name("정올 (Jungol)").url("http://www.jungol.co.kr").build(),
                PlatformSite.builder().name("기타 / 자체 과제").url("").build()
            ));
        }
    }

    public List<PlatformSite> getAllPlatforms() {
        return platformSiteRepository.findAll();
    }

    @Transactional
    public PlatformSite createPlatform(String name, String url) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("사이트 이름을 입력해 주세요.");
        }
        PlatformSite site = PlatformSite.builder()
                .name(name.trim())
                .url(url != null ? url.trim() : "")
                .build();
        return platformSiteRepository.save(site);
    }

    @Transactional
    public PlatformSite updatePlatform(Long id, String name, String url) {
        PlatformSite site = platformSiteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 사이트를 찾을 수 없습니다. (ID: " + id + ")"));
        if (name != null && !name.isBlank()) {
            site.setName(name.trim());
        }
        if (url != null) {
            site.setUrl(url.trim());
        }
        return platformSiteRepository.save(site);
    }

    @Transactional
    public void deletePlatform(Long id) {
        platformSiteRepository.deleteById(id);
    }
}
