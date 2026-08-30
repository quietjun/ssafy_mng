package com.quietjun.ssafymng.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.quietjun.ssafymng.entity.Problem;
import com.quietjun.ssafymng.entity.Student;
import com.quietjun.ssafymng.entity.Submission;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    List<Submission> findByStudentAndProblemOrderBySubmittedAtDesc(Student student, Problem problem);

    List<Submission> findByStudent_SnoAndProblemOrderBySubmittedAtDesc(String sno, Problem problem);

    List<Submission> findByProblemOrderBySubmittedAtDesc(Problem problem);

    List<Submission> findByStudentOrderBySubmittedAtDesc(Student student);

    @Query("SELECT s FROM Submission s JOIN FETCH s.student JOIN FETCH s.problem p WHERE p.problemDate = :problemDate ORDER BY s.submittedAt DESC")
    List<Submission> findByProblemDateWithStudentAndProblem(@Param("problemDate") LocalDate problemDate);

    long countByProblem(Problem problem);

    long countByStudent_SnoAndResultStatus(String sno, String resultStatus);

    void deleteByProblem(Problem problem);
}
