package com.fintech.server.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProgressDto {
    private Long id;
    private Long userId;
    private Long quizId;
    private String quizTitle;
    private Long levelId;
    private Integer levelNumber;
    private String levelTitle;
    private Long subsectorId;
    private String subsectorName;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private Integer score;
    private Boolean passed;
    private Integer teachingViews;
    private LocalDateTime createdAt;
}
