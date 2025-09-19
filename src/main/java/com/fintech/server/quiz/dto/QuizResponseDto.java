package com.fintech.server.quiz.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class QuizResponseDto {

    private Long id;
    private String title;
    private List<QuestionDto> questions;

    // Question 정보를 담는 내부 DTO
    @Getter
    @Builder
    public static class QuestionDto {
        private Long id;
        private String stemMd;
        private String answerExplanationMd;
        private String hintMd;                    // 힌트 정보
        private String teachingExplainerMd;       // 학습 패널 (초록색 박스)
        private String solvingKeypointsMd;        // 핵심 포인트 (노란색 박스)
        private List<OptionDto> options;
    }

    // Option 정보를 담는 내부 DTO
    @Getter
    @Builder
    public static class OptionDto {
        // ▼▼▼ 이 필드가 누락되었을 가능성이 높습니다 ▼▼▼
        private Long id; 
        private String label;
        private String contentMd;
    }
}
