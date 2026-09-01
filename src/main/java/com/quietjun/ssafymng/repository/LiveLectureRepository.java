package com.quietjun.ssafymng.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.quietjun.ssafymng.entity.LiveLecture;

public interface LiveLectureRepository extends JpaRepository<LiveLecture, Long> {

    @Query("SELECT MAX(l.lectureDate) FROM LiveLecture l")
    LocalDate findMaxLectureDate();

    @Query("SELECT MIN(l.lectureDate) FROM LiveLecture l")
    LocalDate findMinLectureDate();
}
