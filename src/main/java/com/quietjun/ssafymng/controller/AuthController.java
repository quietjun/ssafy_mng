package com.quietjun.ssafymng.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.quietjun.ssafymng.dto.PasswordChangeRequest;
import com.quietjun.ssafymng.dto.StudentDto;
import com.quietjun.ssafymng.service.StudentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final StudentService studentService;

    @GetMapping("/current")
    public ResponseEntity<?> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("authenticated", false);
            return ResponseEntity.ok(resp);
        }

        try {
            StudentDto student = studentService.getStudent(auth.getName());
            Map<String, Object> resp = new HashMap<>();
            resp.put("authenticated", true);
            resp.put("sno", student.getSno());
            resp.put("name", student.getName());
            resp.put("role", student.getRole().name());
            resp.put("passwordChanged", student.isPasswordChanged());
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("authenticated", false);
            return ResponseEntity.ok(resp);
        }
    }

    @GetMapping("/users")
    public ResponseEntity<?> getUsersForLogin() {
        var list = studentService.getAllActiveMembers().stream()
            .map(s -> {
                Map<String, Object> map = new HashMap<>();
                map.put("sno", s.getSno());
                map.put("name", s.getName());
                map.put("role", s.getRole().name());
                return map;
            })
            .sorted((a, b) -> {
                if ("ROLE_ADMIN".equals(a.get("role")) && !"ROLE_ADMIN".equals(b.get("role"))) return -1;
                if (!"ROLE_ADMIN".equals(a.get("role")) && "ROLE_ADMIN".equals(b.get("role"))) return 1;
                return ((String) a.get("name")).compareTo((String) b.get("name"));
            })
            .toList();
        return ResponseEntity.ok(list);
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody PasswordChangeRequest req) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "로그인이 필요합니다."));
        }

        try {
            studentService.changePassword(auth.getName(), req);
            return ResponseEntity.ok(Map.of("success", true, "message", "비밀번호가 성공적으로 변경되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
