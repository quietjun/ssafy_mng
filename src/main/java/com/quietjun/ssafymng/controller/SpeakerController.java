package com.quietjun.ssafymng.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.quietjun.ssafymng.dto.SeatSwapRequest;
import com.quietjun.ssafymng.dto.StudentDto;
import com.quietjun.ssafymng.dto.TodaySpeakerDto;
import com.quietjun.ssafymng.service.SpeakerService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/speaker")
@RequiredArgsConstructor
public class SpeakerController {

    private final SpeakerService speakerService;

    @GetMapping("/layout")
    public ResponseEntity<TodaySpeakerDto> getLayout() {
        return ResponseEntity.ok(speakerService.getSeatLayout());
    }

    @PostMapping("/layout")
    public ResponseEntity<?> saveLayout(@RequestBody List<StudentDto> students) {
        boolean success = speakerService.saveSeatLayout(students);
        return ResponseEntity.ok(Map.of("success", success));
    }

    @PostMapping("/swap")
    public ResponseEntity<?> swapSeats(@RequestBody SeatSwapRequest req) {
        boolean success = speakerService.swapSeats(req);
        return ResponseEntity.ok(Map.of("success", success));
    }

    @PostMapping("/draw")
    public ResponseEntity<?> drawSpeaker(@RequestBody(required = false) List<String> candidateSnos) {
        try {
            StudentDto winner = speakerService.drawSpeaker(candidateSnos);
            return ResponseEntity.ok(winner);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/point")
    public ResponseEntity<?> updatePoint(
            @RequestParam("sno") String sno,
            @RequestParam("point") int point) {
        try {
            StudentDto updated = speakerService.updatePresentationPoint(sno, point);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
