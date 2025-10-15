package com.fintech.server.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LevelProgressDto {
    private Long levelId;
    private Integer levelNumber;
    private String levelTitle;
    private Long subsectorId;
    private String subsectorName;
    private String learningGoal;
    private LevelStatus status;
    private Integer totalQuizzes; // 해당 레벨의 전체 퀴즈 수
    private Integer completedQuizzes; // 완료한 퀴즈 수
    private Integer passedQuizzes; // 통과한 퀴즈 수
    private Integer failedQuizzes; // 실패한 퀴즈 수
    private Integer correctAnswers; // 맞힌 문제 수
    private Integer remainingToPass; // 통과까지 남은 문제 수
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Integer timeSpent; // 소요 시간 (초)
    private Integer timeLimit; // 제한 시간 (초)
    private Double completionRate; // 완료율 (0.0 ~ 1.0)
    private Double passRate; // 통과율 (0.0 ~ 1.0)
    private List<QuizProgressDto> quizProgress; // 퀴즈별 진행 상황
    private List<UserProgressDto> progressDetails; // 상세 진행 내역
    private Long nextLevelId;
    private String nextLevelTitle;
    private Boolean nextLevelUnlocked;
    private Boolean levelPassed;

    // 징검다리 관련 필드 추가
    private List<StepProgressDto> steps;  // 징검다리 단계별 정보
    private Boolean isStepPassed;         // 징검다리 통과 여부
    private Integer currentStep;          // 현재 진행 중인 단계

    // 레벨 상태 열거형
    public enum LevelStatus {
        NOT_STARTED, IN_PROGRESS, COMPLETED, FAILED
    }

    // 퀴즈 진행 상황 DTO
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuizProgressDto {
        private Long quizId;
        private String quizTitle;
        private Integer quizNumber;
        private QuizStatus status;
        private Integer score;
        private Integer timeSpent;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private Boolean isCorrect;
    }

    // 퀴즈 상태 열거형
    public enum QuizStatus {
        NOT_STARTED, IN_PROGRESS, COMPLETED, FAILED
    }

    // 편의 메서드
    public boolean isLevelPassed() {
        return levelPassed != null && levelPassed;
    }
}