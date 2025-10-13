package com.fintech.server.quiz.controller;

import com.fintech.server.quiz.dto.BadgeResponseDto;
import com.fintech.server.quiz.dto.UserBadgeResponseDto;
import com.fintech.server.quiz.dto.UserBadgeSummaryDto;
import com.fintech.server.quiz.service.BadgeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/badges") // context-path(/api) 제외
@RequiredArgsConstructor
@Slf4j
@Tag(name = "뱃지 관리", description = "사용자 뱃지 조회 및 관리 API")
public class BadgeController {

    private final BadgeService badgeService;

    /**
     * 배지 시스템 초기화
     */
    @PostMapping("/init")
    public ResponseEntity<String> initBadges() {
        try {
            badgeService.initializeBadges();
            return ResponseEntity.ok("배지 시스템 초기화 완료");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("배지 시스템 초기화 실패: " + e.getMessage());
        }
    }

    /**
     * 사용자 배지 진행 상황 업데이트
     */
    @PostMapping("/update/{userId}")
    public ResponseEntity<String> updateUserBadgeProgress(@PathVariable Long userId) {
        try {
            badgeService.updateUserBadgeProgress(userId);
            return ResponseEntity.ok("사용자 " + userId + "의 배지 진행 상황 업데이트 완료");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("배지 업데이트 실패: " + e.getMessage());
        }
    }

    /**
     * 사용자 뱃지 요약 정보 조회
     * 현재 뱃지, 다음 뱃지, 모든 뱃지 목록을 포함한 종합 정보
     */
    @GetMapping("/user/{userId}/summary")
    @Operation(summary = "사용자 뱃지 요약 조회", description = "사용자의 현재 뱃지, 다음 뱃지, 모든 뱃지 목록을 조회합니다.")
    public ResponseEntity<?> getUserBadgeSummary(
            @Parameter(description = "사용자 ID") @PathVariable Long userId) {
        try {
            UserBadgeSummaryDto summary = badgeService.getUserBadgeSummary(userId);
            log.info("Badge summary retrieved for user: {}", userId);
            return ResponseEntity.ok(summary);
        } catch (RuntimeException e) {
            log.warn("Failed to get badge summary for user: {}, error: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error getting badge summary for user: {}", userId, e);
            return ResponseEntity.internalServerError()
                    .body("Internal server error: " + e.getMessage());
        }
    }

    /**
     * 사용자 현재 뱃지 조회
     */
    @GetMapping("/user/{userId}/current")
    @Operation(summary = "사용자 현재 뱃지 조회", description = "사용자의 현재 획득한 최고 레벨 뱃지를 조회합니다.")
    public ResponseEntity<?> getCurrentBadge(
            @Parameter(description = "사용자 ID") @PathVariable Long userId) {
        try {
            BadgeResponseDto currentBadge = badgeService.getCurrentBadgeResponse(userId);
            log.info("Current badge retrieved for user: {}", userId);
            return ResponseEntity.ok(currentBadge);
        } catch (RuntimeException e) {
            log.warn("Failed to get current badge for user: {}, error: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error getting current badge for user: {}", userId, e);
            return ResponseEntity.internalServerError()
                    .body("Internal server error: " + e.getMessage());
        }
    }

    /**
     * 사용자 획득한 뱃지 목록 조회
     */
    @GetMapping("/user/{userId}/achieved")
    @Operation(summary = "사용자 획득 뱃지 목록 조회", description = "사용자가 획득한 모든 뱃지 목록을 조회합니다.")
    public ResponseEntity<?> getAchievedBadges(
            @Parameter(description = "사용자 ID") @PathVariable Long userId) {
        try {
            List<UserBadgeResponseDto> achievedBadges = badgeService.getAchievedBadgeResponses(userId);
            log.info("Achieved badges retrieved for user: {}, count: {}", userId, achievedBadges.size());
            return ResponseEntity.ok(achievedBadges);
        } catch (RuntimeException e) {
            log.warn("Failed to get achieved badges for user: {}, error: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error getting achieved badges for user: {}", userId, e);
            return ResponseEntity.internalServerError()
                    .body("Internal server error: " + e.getMessage());
        }
    }

    /**
     * 사용자 모든 뱃지 목록 조회 (진행률 포함)
     */
    @GetMapping("/user/{userId}/all")
    @Operation(summary = "사용자 모든 뱃지 목록 조회", description = "사용자의 모든 뱃지 목록을 진행률과 함께 조회합니다.")
    public ResponseEntity<?> getAllUserBadges(
            @Parameter(description = "사용자 ID") @PathVariable Long userId) {
        try {
            List<UserBadgeResponseDto> allBadges = badgeService.getAllUserBadgeResponses(userId);
            log.info("All badges retrieved for user: {}, count: {}", userId, allBadges.size());
            return ResponseEntity.ok(allBadges);
        } catch (RuntimeException e) {
            log.warn("Failed to get all badges for user: {}, error: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error getting all badges for user: {}", userId, e);
            return ResponseEntity.internalServerError()
                    .body("Internal server error: " + e.getMessage());
        }
    }

    /**
     * 모든 뱃지 목록 조회
     */
    @GetMapping
    @Operation(summary = "모든 뱃지 목록 조회", description = "시스템에 등록된 모든 뱃지 목록을 조회합니다.")
    public ResponseEntity<?> getAllBadges() {
        try {
            List<BadgeResponseDto> allBadges = badgeService.getAllBadgeResponses();
            log.info("All badges retrieved, count: {}", allBadges.size());
            return ResponseEntity.ok(allBadges);
        } catch (Exception e) {
            log.error("Unexpected error getting all badges", e);
            return ResponseEntity.internalServerError()
                    .body("Internal server error: " + e.getMessage());
        }
    }
}
