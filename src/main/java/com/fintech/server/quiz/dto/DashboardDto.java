package com.fintech.server.quiz.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class DashboardDto {
    // 사용자 기본 정보
    private UserInfoDto userInfo;
    
    // 학습 통계
    private LearningStatsDto learningStats;
    
    // 주간 진행 현황
    private List<WeeklyProgressDto> weeklyProgress;
    
    // 최근 활동
    private List<RecentActivityDto> recentActivities;
    
    // 다음 레벨 추천
    private NextLevelRecommendationDto nextLevelRecommendation;
    
    // 현재 진행 중인 레벨
    private CurrentLevelSessionDto currentLevelSession;
    
    @Getter
    @Builder
    public static class UserInfoDto {
        private Long userId;
        private String nickname;
        private String currentLevelTitle;
        private Integer currentLevelNumber;
        private Integer streak;
        private Integer totalScore;
    }
    
    @Getter
    @Builder
    public static class LearningStatsDto {
        private Integer totalLevelsCompleted;
        private Integer totalQuizzesCompleted;
        private Integer totalQuestionsAnswered;
        private Integer totalMinutesSpent;
        private Double averageScore;
    }
    
    @Getter
    @Builder
    public static class WeeklyProgressDto {
        private int dayOfMonth;
        private boolean completed;
        private Integer minutesSpent;
        private Integer quizzesCompleted;
    }
    
    @Getter
    @Builder
    public static class RecentActivityDto {
        private String type; // QUIZ_COMPLETED, LEVEL_COMPLETED
        private String title;
        private Integer score;
        private String completedAt;
        private java.time.LocalDateTime activityTime;
    }
    
    @Getter
    @Builder
    public static class NextLevelRecommendationDto {
        private Long levelId;
        private String levelTitle;
        private String subsectorName;
        private String reason;
        private Integer progressPercentage;
        private Integer remainingQuizzes;
        private String difficulty;
        private Integer estimatedTime;
        private String learningGoal;
    }
    
    @Getter
    @Builder
    public static class CurrentLevelSessionDto {
        private String sessionId;
        private Long levelId;
        private String levelTitle;
        private String subsectorName;
        private String startedAt;
        private Integer timeLimit;
        private Integer timeRemaining;
        private Integer currentQuizIndex;
        private Integer completedQuizzes;
        private Integer correctAnswers;
        private Integer remainingToPass;
        private String status; // IN_PROGRESS, PAUSED, COMPLETED, FAILED
    }
}
