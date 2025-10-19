package com.fintech.server.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class WrongNoteDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private Long userId;
        private Long questionId;
        private String questionText;
        private Long lastAnswerOptionId;
        private String lastAnswerText;
        private Long correctOptionId;
        private String correctAnswerText;
        private Integer timesWrong;
        private LocalDateTime firstWrongAt;
        private LocalDateTime lastWrongAt;
        private LocalDateTime reviewedAt;
        private Boolean resolved;
        private String personalNoteMd;
        private String snapshotTeachingSummaryMd;
        private String snapshotTeachingExplainerMd;
        private String snapshotKeypointsMd;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        
        // 추가 정보
        private String quizTitle;
        private String sectorName;
        private String subsectorName;
        private List<OptionInfo> allOptions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionInfo {
        private Long id;
        private String text;
        private Boolean isCorrect;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        private Long questionId;
        private String personalNoteMd;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        private String personalNoteMd;
        private Boolean resolved;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Statistics {
        private Long totalCount;
        private Long unresolvedCount;
        private Long resolvedCount;
        private Long needReviewCount; // 1회 이상 틀린 문제
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubsectorStatistics {
        private Long subsectorId;
        private String subsectorName;
        private Long wrongCount; // 해당 서브섹터에서 틀린 문제 수
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LevelStatistics {
        private Long levelId;
        private Integer levelNumber;
        private String levelTitle;
        private String subsectorName; // 소속 서브섹터명
        private Long wrongCount; // 해당 레벨에서 틀린 문제 수
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ListResponse {
        private List<Response> wrongNotes;
        private Statistics statistics;
        private List<SubsectorStatistics> subsectorStatistics; // 서브섹터별 틀린 문제 수
        private List<LevelStatistics> levelStatistics; // 레벨별 틀린 문제 수
        private Integer totalPages;
        private Integer currentPage;
        private Integer pageSize;
    }
}
