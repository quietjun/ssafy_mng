package com.quietjun.ssafymng.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.quietjun.ssafymng.entity.Role;
import com.quietjun.ssafymng.entity.Student;

@Repository
public interface StudentRepository extends JpaRepository<Student, String> {

    List<Student> findByEscapeFalseOrderBySrowAscScolAsc();

    List<Student> findByRoleAndEscapeFalse(Role role);

    Optional<Student> findBySnoAndEscapeFalse(String sno);

    long countByRole(Role role);
}
