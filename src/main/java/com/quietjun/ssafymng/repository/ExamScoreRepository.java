package com.quietjun.ssafymng.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.quietjun.ssafymng.entity.Exam;
import com.quietjun.ssafymng.entity.ExamScore;
import com.quietjun.ssafymng.entity.Student;

@Repository
public interface ExamScoreRepository extends JpaRepository<ExamScore, Long> {
    List<ExamScore> findByExam(Exam exam);
    List<ExamScore> findByExamId(Long examId);
    List<ExamScore> findByStudent_Sno(String sno);
    Optional<ExamScore> findByExamAndStudent(Exam exam, Student student);
    void deleteByExam(Exam exam);
    int countByExam(Exam exam);
}
