package com.quietjun.ssafymng.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.quietjun.ssafymng.dto.StudentBulkImportRequest;
import com.quietjun.ssafymng.dto.StudentCreateRequest;
import com.quietjun.ssafymng.dto.StudentDto;
import com.quietjun.ssafymng.service.StudentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @GetMapping
    public ResponseEntity<List<StudentDto>> getStudentList() {
        return ResponseEntity.ok(studentService.getStudentList());
    }

    @GetMapping("/{sno}")
    public ResponseEntity<StudentDto> getStudent(@PathVariable String sno) {
        return ResponseEntity.ok(studentService.getStudent(sno));
    }

    @PostMapping
    public ResponseEntity<?> createStudent(@RequestBody StudentCreateRequest req) {
        try {
            StudentDto created = studentService.createStudent(req);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PutMapping("/{sno}")
    public ResponseEntity<?> updateStudent(@PathVariable String sno, @RequestBody StudentCreateRequest req) {
        try {
            StudentDto updated = studentService.updateStudent(sno, req);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/{sno}")
    public ResponseEntity<?> deleteStudent(@PathVariable String sno) {
        try {
            studentService.deleteStudent(sno);
            return ResponseEntity.ok(Map.of("success", true, "message", "학생이 삭제되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/bulk")
    public ResponseEntity<?> bulkImport(@RequestBody StudentBulkImportRequest req) {
        int count = studentService.bulkImport(req);
        return ResponseEntity.ok(Map.of("success", true, "count", count, "message", count + "명의 학생이 등록/갱신되었습니다."));
    }
}
