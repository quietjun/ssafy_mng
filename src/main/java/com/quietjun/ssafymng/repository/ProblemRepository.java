package com.quietjun.ssafymng.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.quietjun.ssafymng.entity.Problem;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long> {

    List<Problem> findByProblemDateOrderByCreatedAtAsc(LocalDate problemDate);

    List<Problem> findByProblemDateBetweenOrderByProblemDateDescCreatedAtDesc(LocalDate startDate, LocalDate endDate);

    List<Problem> findAllByOrderByProblemDateDescCreatedAtDesc();
}
