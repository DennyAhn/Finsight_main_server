package com.fintech.server.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

// 두 번째 화면을 위한 DTO
@Getter
@Builder
@AllArgsConstructor
public class SubsectorDetailResponseDto {
    private Long id;
    private String name;
    private String sectorName;
    private String description; // 소섹터의 학습 목표
    private List<LevelDto> levels;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class LevelDto {
        private Long id;
        private Integer levelNumber;
        private String title;
        private String learningGoal;
    }
}
