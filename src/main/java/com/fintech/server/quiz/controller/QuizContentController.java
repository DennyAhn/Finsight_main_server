package com.fintech.server.quiz.controller;

import com.fintech.server.quiz.dto.SectorResponseDto;
import com.fintech.server.quiz.dto.SubsectorDetailResponseDto;
import com.fintech.server.quiz.dto.LevelDetailResponseDto;
import com.fintech.server.quiz.service.QuizContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class QuizContentController {

    private final QuizContentService quizContentService;

    // 첫 번째 화면용 API
    @GetMapping("/sectors")
    public ResponseEntity<List<SectorResponseDto>> getAllSectors() {
        return ResponseEntity.ok(quizContentService.findAllSectors());
    }

    // 두 번째 화면용 API
    @GetMapping("/subsectors/{id}")
    public ResponseEntity<SubsectorDetailResponseDto> getSubsectorDetails(@PathVariable("id") Long subsectorId) {
        return ResponseEntity.ok(quizContentService.findSubsectorById(subsectorId));
    }

    // 레벨별 퀴즈 상태 조회 API
    @GetMapping("/levels/{levelId}/quizzes")
    public ResponseEntity<LevelDetailResponseDto> getLevelQuizzes(
            @PathVariable("levelId") Long levelId,
            @RequestParam("userId") Long userId) {
        return ResponseEntity.ok(quizContentService.getLevelDetail(levelId, userId));
    }
}

