package com.fintech.server.quiz.controller;

import com.fintech.server.quiz.dto.LevelCompletionDto;
import com.fintech.server.quiz.dto.LevelProgressDto;
import com.fintech.server.quiz.entity.Level;
import com.fintech.server.quiz.repository.LevelRepository;
import com.fintech.server.quiz.service.LevelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/levels")
@RequiredArgsConstructor
@Slf4j
public class LevelController {

    private final LevelService levelService;
    private final LevelRepository levelRepository;

    /**
     * 레벨 진행 상황 조회 API
     * 
     * 🌐 HTTP Method: GET
     * 🔗 URL: /api/levels/{id}/progress?userId={userId}
     * 
     * 📤 Response (200 OK):
     * {
     *   "levelId": 1,
     *   "levelTitle": "금융기초 1단계",
     *   "subsectorName": "은행업",
     *   "levelNumber": 1,
     *   "learningGoal": "은행 업무의 기본 원리 이해",
     *   "status": "IN_PROGRESS",
     *   "completedQuizzes": 2,
     *   "totalQuizzes": 4,
     *   "correctAnswers": 2,
     *   "remainingToPass": 1,
     *   "startedAt": "2024-01-15T14:30:00",
     *   "completedAt": null,
     *   "timeSpent": 600,
     *   "timeLimit": 3600,
     *   "quizProgress": [...],
     *   "nextLevelId": 2,
     *   "nextLevelTitle": "금융기초 2단계",
     *   "nextLevelUnlocked": false
     * }
     */
    @GetMapping("/{id}/progress")
    public ResponseEntity<?> getLevelProgress(
            @PathVariable("id") Long levelId,
            @RequestParam("userId") Long userId) {
        try {
            // 레벨과 서브섹터를 함께 조회 (JOIN FETCH 사용하여 LazyInitializationException 방지)
            Level level = levelRepository.findByIdWithSubsector(levelId)
                    .orElseThrow(() -> new RuntimeException("Level not found with id: " + levelId));
            LevelProgressDto progress = levelService.getLevelProgress(levelId, userId, level.getSubsector().getId());
            log.info("Level progress retrieved: levelId={}, userId={}, status={}", 
                    levelId, userId, progress.getStatus());
            return ResponseEntity.ok(progress);
        } catch (RuntimeException e) {
            log.warn("Failed to get level progress: levelId={}, userId={}, error={}", 
                    levelId, userId, e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error getting level progress: levelId={}, userId={}", 
                    levelId, userId, e);
            return ResponseEntity.internalServerError()
                    .body("Internal server error: " + e.getMessage());
        }
    }

    /**
     * 레벨 완료 처리 API
     * 
     * 🌐 HTTP Method: POST
     * 🔗 URL: /api/levels/{id}/complete?userId={userId}
     * 
     * 📤 Response (200 OK):
     * {
     *   "levelId": 1,
     *   "levelTitle": "금융기초 1단계",
     *   "isCompleted": true,
     *   "isPassed": true,
     *   "correctAnswers": 3,
     *   "totalQuestions": 4,
     *   "score": 3,
     *   "timeSpent": 1200,
     *   "completedAt": "2024-01-15T15:00:00",
     *   "nextLevelId": 2,
     *   "nextLevelTitle": "금융기초 2단계",
     *   "nextLevelUnlocked": true,
     *   "pointsEarned": 350,
     *   "badgeEarned": "Level 1 Master",
     *   "achievementMessage": "축하합니다! 레벨 1을 완료했습니다!"
     * }
     */
    @PostMapping("/{id}/complete")
    public ResponseEntity<?> completeLevel(
            @PathVariable("id") Long levelId,
            @RequestParam("userId") Long userId) {
        try {
            LevelCompletionDto completion = levelService.completeLevel(levelId, userId);
            log.info("Level completed: levelId={}, userId={}, passed={}, score={}/{}", 
                    levelId, userId, completion.isPassed(), 
                    completion.getCorrectAnswers(), completion.getTotalQuestions());
            return ResponseEntity.ok(completion);
        } catch (RuntimeException e) {
            log.warn("Failed to complete level: levelId={}, userId={}, error={}", 
                    levelId, userId, e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error completing level: levelId={}, userId={}", 
                    levelId, userId, e);
            return ResponseEntity.internalServerError()
                    .body("Internal server error: " + e.getMessage());
        }
    }

    /**
     * 레벨 시작 API (진행 상황 초기화)
     * 
     * 🌐 HTTP Method: POST
     * 🔗 URL: /api/levels/{id}/start?userId={userId}
     * 
     * 📤 Response (200 OK):
     * {
     *   "message": "Level started successfully",
     *   "levelId": 1,
     *   "levelTitle": "금융기초 1단계"
     * }
     */
    @PostMapping("/{id}/start")
    public ResponseEntity<?> startLevel(
            @PathVariable("id") Long levelId,
            @RequestParam("userId") Long userId) {
        try {
            // 레벨과 서브섹터를 함께 조회 (JOIN FETCH 사용하여 LazyInitializationException 방지)
            Level level = levelRepository.findByIdWithSubsector(levelId)
                    .orElseThrow(() -> new RuntimeException("Level not found with id: " + levelId));
            levelService.getLevelProgress(levelId, userId, level.getSubsector().getId());
            
            log.info("Level started: levelId={}, userId={}", levelId, userId);
            return ResponseEntity.ok().body("Level started successfully");
        } catch (RuntimeException e) {
            log.warn("Failed to start level: levelId={}, userId={}, error={}", 
                    levelId, userId, e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error starting level: levelId={}, userId={}", 
                    levelId, userId, e);
            return ResponseEntity.internalServerError()
                    .body("Internal server error: " + e.getMessage());
        }
    }
}
