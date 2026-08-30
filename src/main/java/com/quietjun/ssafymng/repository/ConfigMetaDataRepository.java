package com.quietjun.ssafymng.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.quietjun.ssafymng.entity.ConfigMetaData;

@Repository
public interface ConfigMetaDataRepository extends JpaRepository<ConfigMetaData, String> {
}
