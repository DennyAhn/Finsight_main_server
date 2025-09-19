package com.fintech.server.quiz.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AnswerResponseDto {
    private boolean isCorrect;
    private Long correctOptionId;
    private String feedback;
}