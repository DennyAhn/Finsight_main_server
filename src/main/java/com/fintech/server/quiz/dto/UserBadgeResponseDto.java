package com.fintech.server.quiz.dto;

import com.fintech.server.quiz.entity.UserBadge;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserBadgeResponseDto {
    private Long id;
    private BadgeResponseDto badge;
    private Integer progress;
    private Boolean isAchieved;
    private LocalDateTime earnedAt;
    private LocalDateTime awardedAt;
    private String source;

    public static UserBadgeResponseDto from(UserBadge userBadge) {
        if (userBadge == null) {
            return null;
        }

        return UserBadgeResponseDto.builder()
                .id(userBadge.getId())
                .badge(BadgeResponseDto.from(userBadge.getBadge()))
                .progress(userBadge.getProgress())
                .isAchieved(userBadge.getIsAchieved())
                .earnedAt(userBadge.getEarnedAt())
                .awardedAt(userBadge.getAwardedAt())
                .source(userBadge.getSource())
                .build();
    }
}
