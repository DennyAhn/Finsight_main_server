package com.fintech.server.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubsectorProgressDto {
    private Long subsectorId;
    private String subsectorName;
    private Integer totalLevels; // 전체 레벨 수
    private Integer completedLevels; // 완료한 레벨 수
    private Integer totalQuizzes; // 전체 퀴즈 수
    private Integer completedQuizzes; // 완료한 퀴즈 수
    private Integer passedQuizzes; // 통과한 퀴즈 수
    private Double overallCompletionRate; // 전체 완료율
    private Double overallPassRate; // 전체 통과율
    private List<LevelProgressDto> levelProgress; // 레벨별 진행률
}
