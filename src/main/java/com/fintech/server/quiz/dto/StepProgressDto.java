package com.fintech.server.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StepProgressDto {
    private Integer stepNumber;        // 1, 2, 3, 4
    private String stepTitle;          // "1단계", "2단계" 등
    private Integer completedQuizzes;  // 완료한 퀴즈 수
    private Integer totalQuizzes;      // 전체 퀴즈 수 (4)
    private Integer passedQuizzes;     // 통과한 퀴즈 수
    private Integer failedQuizzes;     // 실패한 퀴즈 수
    private Boolean isCompleted;       // 징검다리 완성 여부
    private Boolean isPassed;          // 징검다리 통과 여부 (50% 이상)
    private Double passRate;           // 통과율
    private String stepDescription;    // 단계 설명
}
