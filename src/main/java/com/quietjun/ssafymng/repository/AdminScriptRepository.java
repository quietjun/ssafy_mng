package com.quietjun.ssafymng.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.quietjun.ssafymng.entity.AdminScript;

public interface AdminScriptRepository extends JpaRepository<AdminScript, Long> {
    List<AdminScript> findAllByOrderByOrderIndexAscIdAsc();
}
