package com.quietjun.ssafymng.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.quietjun.ssafymng.entity.PlatformSite;

@Repository
public interface PlatformSiteRepository extends JpaRepository<PlatformSite, Long> {
}
