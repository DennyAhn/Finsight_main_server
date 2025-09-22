package com.fintech.server.quiz.dto;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class LevelCompletionDto {
    private Long levelId;
    private String levelTitle;
    private boolean isCompleted;
    private boolean isPassed;
    private Integer correctAnswers; // 맞힌 문제 수
    private Integer totalQuestions; // 전체 문제 수 (4)
    private Integer score; // 점수 (0-4)
    private Integer timeSpent; // 소요 시간 (초)
    private LocalDateTime completedAt;
    
    // 다음 레벨 정보
    private Long nextLevelId;
    private String nextLevelTitle;
    private boolean nextLevelUnlocked;
    
    // 보상 정보
    private Integer pointsEarned;
    private String badgeEarned;
    private String achievementMessage;
}
