package com.fintech.server.quiz.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QuizCompleteResponse {
    private int totalQuestions;
    private int correctAnswers;
    private boolean passed;
    private int score;
    private String message;
}
