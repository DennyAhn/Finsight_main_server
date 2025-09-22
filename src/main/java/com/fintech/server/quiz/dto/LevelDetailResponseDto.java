package com.fintech.server.quiz.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class LevelDetailResponseDto {
    private String levelTitle;
    private String subsectorName;
    private List<WeeklyProgressDto> weeklyProgress; 
    private List<QuizStatusDto> quizzes;

    @Getter
    @Builder
    public static class WeeklyProgressDto {
        private int dayOfMonth;
        private boolean completed;
    }
    @Getter
    @Builder
    public static class QuizStatusDto {
        private Long id;
        private String title;
        private Integer sortOrder;
        private Status status; // COMPLETED, IN_PROGRESS, LOCKED
    }

    public enum Status {
        COMPLETED, IN_PROGRESS, LOCKED
    }
}