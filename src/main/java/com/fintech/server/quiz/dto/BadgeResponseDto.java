package com.fintech.server.quiz.dto;

import com.fintech.server.quiz.entity.Badge;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BadgeResponseDto {
    private Long id;
    private String code;
    private String name;
    private String description;
    private String iconUrl;
    private Integer levelNumber;
    private Integer requiredQuizzes;
    private Integer requiredCorrectAnswers;
    private String color;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static BadgeResponseDto from(Badge badge) {
        if (badge == null) {
            return null;
        }

        return BadgeResponseDto.builder()
                .id(badge.getId())
                .code(badge.getCode())
                .name(badge.getName())
                .description(badge.getDescription())
                .iconUrl(badge.getIconUrl())
                .levelNumber(badge.getLevelNumber())
                .requiredQuizzes(badge.getRequiredQuizzes())
                .requiredCorrectAnswers(badge.getRequiredCorrectAnswers())
                .color(Badge.BadgeLevel.fromLevelNumber(badge.getLevelNumber()).getColor())
                .createdAt(badge.getCreatedAt())
                .updatedAt(badge.getUpdatedAt())
                .build();
    }
}

