package com.fintech.server.quiz.dto;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class LevelProgressDto {
    private Long levelId;
    private String levelTitle;
    private String subsectorName;
    private Integer levelNumber;
    private String learningGoal;
    
    // 레벨 진행 상황
    private LevelStatus status;
    private Integer completedQuizzes; // 완료된 퀴즈 수 (0-4)
    private Integer totalQuizzes; // 전체 퀴즈 수 (4)
    private Integer correctAnswers; // 현재까지 맞힌 문제 수
    private Integer remainingToPass; // 패스까지 필요한 문제 수 (3개 이상)
    
    // 시간 정보
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Integer timeSpent; // 소요 시간 (초)
    private Integer timeLimit; // 제한 시간 (초)
    
    // 퀴즈별 진행 상황
    private List<QuizProgressDto> quizProgress;
    
    // 다음 레벨 정보
    private Long nextLevelId;
    private String nextLevelTitle;
    private boolean nextLevelUnlocked;
    
    public enum LevelStatus {
        NOT_STARTED, IN_PROGRESS, COMPLETED, FAILED, EXPIRED
    }
    
    // 레벨 통과 여부 확인
    public boolean isLevelPassed() {
        return correctAnswers != null && correctAnswers >= 3; // 4문제 중 3문제 이상 맞춰야 통과
    }
    
    @Getter
    @Builder
    public static class QuizProgressDto {
        private Long quizId;
        private String quizTitle;
        private Integer quizNumber; // 1, 2, 3, 4
        private QuizStatus status;
        private Integer correctAnswers;
        private Integer totalQuestions;
        private LocalDateTime completedAt;
        private Integer timeSpent; // 소요 시간 (초)
        private boolean passed; // 통과 여부
    }
    
    public enum QuizStatus {
        NOT_STARTED, IN_PROGRESS, COMPLETED, FAILED
    }
}
