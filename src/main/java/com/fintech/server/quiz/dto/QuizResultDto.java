package com.fintech.server.quiz.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QuizResultDto {
    private int totalQuestions;    // 전체 문항 수
    private int correctAnswers;    // 맞힌 문항 수
    private boolean passed;        // 통과 여부
}