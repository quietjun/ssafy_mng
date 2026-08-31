package com.quietjun.ssafymng.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.quietjun.ssafymng.entity.Exam;
import com.quietjun.ssafymng.entity.ExamCategory;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {
    List<Exam> findByCategoryOrderByCreatedAtDesc(ExamCategory category);
    List<Exam> findAllByOrderByCreatedAtDesc();
}
