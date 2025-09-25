package com.fintech.server.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class AdminWrongNoteDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SectorStatistics {
        private Long sectorId;
        private String sectorName;
        private String sectorSlug;
        private Long totalWrongCount;
        private Long uniqueUsersCount;
        private Long totalQuestionsCount;
        private Double wrongAnswerRate; // 오답률 (%)
        private List<SubsectorStatistics> subsectors;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubsectorStatistics {
        private Long subsectorId;
        private String subsectorName;
        private String subsectorSlug;
        private Long totalWrongCount;
        private Long uniqueUsersCount;
        private Long totalQuestionsCount;
        private Double wrongAnswerRate; // 오답률 (%)
        private List<LevelStatistics> levels;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LevelStatistics {
        private Long levelId;
        private Integer levelNumber;
        private String levelTitle;
        private Long totalWrongCount;
        private Long uniqueUsersCount;
        private Long totalQuestionsCount;
        private Double wrongAnswerRate; // 오답률 (%)
        private List<QuizStatistics> quizzes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuizStatistics {
        private Long quizId;
        private String quizTitle;
        private Long totalWrongCount;
        private Long uniqueUsersCount;
        private Long totalQuestionsCount;
        private Double wrongAnswerRate; // 오답률 (%)
        private List<QuestionStatistics> questions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionStatistics {
        private Long questionId;
        private String questionText;
        private Long wrongCount;
        private Long uniqueUsersCount;
        private Double wrongAnswerRate; // 오답률 (%)
        private List<UserWrongInfo> recentWrongUsers;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserWrongInfo {
        private Long userId;
        private String userNickname;
        private String userEmail;
        private Integer timesWrong;
        private LocalDateTime lastWrongAt;
        private Boolean resolved;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverallStatistics {
        private Long totalWrongNotesCount;
        private Long totalUniqueUsersCount;
        private Long totalQuestionsCount;
        private Double overallWrongAnswerRate;
        private List<TopWrongQuestion> topWrongQuestions;
        private List<SectorStatistics> sectorStatistics;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopWrongQuestion {
        private Long questionId;
        private String questionText;
        private String quizTitle;
        private String sectorName;
        private String subsectorName;
        private Long wrongCount;
        private Double wrongAnswerRate;
    }

}
