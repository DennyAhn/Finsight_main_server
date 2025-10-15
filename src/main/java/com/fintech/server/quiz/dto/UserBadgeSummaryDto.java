package com.fintech.server.quiz.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class UserBadgeSummaryDto {
    private BadgeResponseDto currentBadge;
    private BadgeResponseDto nextBadge;
    private List<UserBadgeResponseDto> allBadges;
    private List<UserBadgeResponseDto> achievedBadges;
    private Integer totalBadges;
    private Integer achievedBadgesCount;
    private Integer progressPercentage;

    public static UserBadgeSummaryDto of(BadgeResponseDto currentBadge, 
                                       BadgeResponseDto nextBadge,
                                       List<UserBadgeResponseDto> allBadges,
                                       List<UserBadgeResponseDto> achievedBadges) {
        int totalBadges = allBadges.size();
        int achievedCount = achievedBadges.size();
        int progressPercentage = totalBadges > 0 ? (achievedCount * 100) / totalBadges : 0;

        return UserBadgeSummaryDto.builder()
                .currentBadge(currentBadge)
                .nextBadge(nextBadge)
                .allBadges(allBadges)
                .achievedBadges(achievedBadges)
                .totalBadges(totalBadges)
                .achievedBadgesCount(achievedCount)
                .progressPercentage(progressPercentage)
                .build();
    }
}

