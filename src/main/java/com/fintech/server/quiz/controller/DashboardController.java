package com.fintech.server.quiz.controller;

import com.fintech.server.quiz.dto.DashboardDto;
import com.fintech.server.quiz.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 사용자 대시보드 조회 API
     * 
     * 🌐 HTTP Method: GET
     * 🔗 URL: /api/dashboard?userId={userId}
     * 
     * 📤 Response (200 OK):
     * {
     *   "userInfo": {
     *     "nickname": "익명의 사용자",
     *     "currentLevel": "금융기초 1단계",
     *     "streak": 5,
     *     "totalScore": 1250,
     *     "level": 1
     *   },
     *   "learningStats": {
     *     "totalLevelsCompleted": 3,
     *     "totalQuizzesCompleted": 12,
     *     "totalQuestionsAnswered": 48,
     *     "totalMinutesSpent": 240,
     *     "averageScore": 85.5
     *   },
     *   "weeklyProgress": [...],
     *   "recentActivities": [...],
     *   "nextLevelRecommendation": {
     *     "levelId": 4,
     *     "levelTitle": "금융기초 4단계",
     *     "subsectorName": "은행업",
     *     "reason": "현재 레벨 완료",
     *     "progressPercentage": 100,
     *     "remainingQuizzes": 0,
     *     "difficulty": "MEDIUM",
     *     "estimatedTime": 60,
     *     "learningGoal": "은행 업무의 기본 원리 이해"
     *   },
     *   "currentLevelSession": {
     *     "sessionId": "level_1_123",
     *     "levelId": 1,
     *     "levelTitle": "금융기초 1단계",
     *     "subsectorName": "은행업",
     *     "startedAt": "2024-01-15T14:30:00",
     *     "timeLimit": 3600,
     *     "timeRemaining": 1800,
     *     "currentQuizIndex": 2,
     *     "completedQuizzes": 2,
     *     "correctAnswers": 2,
     *     "remainingToPass": 1,
     *     "status": "IN_PROGRESS"
     *   }
     * }
     */
    @GetMapping
    public ResponseEntity<?> getDashboard(@RequestParam("userId") Long userId) {
        try {
            DashboardDto dashboard = dashboardService.getUserDashboard(userId);
            log.info("Dashboard retrieved for user: {}", userId);
            return ResponseEntity.ok(dashboard);
        } catch (RuntimeException e) {
            log.warn("Failed to get dashboard for user: {}, error: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error getting dashboard for user: {}", userId, e);
            return ResponseEntity.internalServerError()
                    .body("Internal server error: " + e.getMessage());
        }
    }
}
