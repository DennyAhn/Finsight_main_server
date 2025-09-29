package com.fintech.server.quiz.controller;

import com.fintech.server.quiz.service.BadgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/badges") // context-path(/api) 제외
@RequiredArgsConstructor
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
}
