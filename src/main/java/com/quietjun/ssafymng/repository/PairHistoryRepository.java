package com.quietjun.ssafymng.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.quietjun.ssafymng.entity.PairHistory;

public interface PairHistoryRepository extends JpaRepository<PairHistory, Long> {

    List<PairHistory> findByDomainOrderByCreatedAtDesc(String domain);

    List<PairHistory> findAllByOrderByCreatedAtDesc();

    void deleteByDomainAndTitle(String domain, String title);

    @Query("SELECT p FROM PairHistory p WHERE " +
           "(p.student1Sno = :sno1 AND p.student2Sno = :sno2) OR " +
           "(p.student1Sno = :sno2 AND p.student2Sno = :sno1)")
    List<PairHistory> findPastPairsBetweenStudents(@Param("sno1") String sno1, @Param("sno2") String sno2);
}
