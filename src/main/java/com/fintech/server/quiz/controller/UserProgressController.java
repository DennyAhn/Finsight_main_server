package com.fintech.server.quiz.controller;

import com.fintech.server.quiz.dto.LevelProgressDto;
import com.fintech.server.quiz.dto.SubsectorProgressDto;
import com.fintech.server.quiz.dto.UserProgressDto;
import com.fintech.server.quiz.service.UserProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "사용자 진행률", description = "사용자의 퀴즈 진행률 조회 API")
public class UserProgressController {
    
    private final UserProgressService userProgressService;
    
    /**
     * 사용자의 모든 진행률 조회
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "사용자 전체 진행률 조회", description = "특정 사용자의 모든 퀴즈 진행률을 조회합니다.")
    public ResponseEntity<List<UserProgressDto>> getUserProgress(
            @Parameter(description = "사용자 ID", required = true)
            @PathVariable Long userId) {
        
        log.info("사용자 {}의 전체 진행률 조회 요청", userId);
        
        try {
            List<UserProgressDto> progressList = userProgressService.getUserProgress(userId);
            log.info("사용자 {}의 진행률 조회 완료: {}개", userId, progressList.size());
            return ResponseEntity.ok(progressList);
        } catch (Exception e) {
            log.error("사용자 {}의 진행률 조회 실패: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 특정 레벨의 진행률 조회
     */
    @GetMapping("/user/{userId}/level/{levelId}")
    @Operation(summary = "레벨별 진행률 조회", description = "특정 사용자의 특정 레벨 진행률을 조회합니다.")
    public ResponseEntity<LevelProgressDto> getLevelProgress(
            @Parameter(description = "사용자 ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "레벨 ID", required = true)
            @PathVariable Long levelId) {
        
        log.info("사용자 {}의 레벨 {} 진행률 조회 요청", userId, levelId);
        
        try {
            LevelProgressDto levelProgress = userProgressService.getLevelProgress(userId, levelId);
            log.info("사용자 {}의 레벨 {} 진행률 조회 완료", userId, levelId);
            return ResponseEntity.ok(levelProgress);
        } catch (Exception e) {
            log.error("사용자 {}의 레벨 {} 진행률 조회 실패: {}", userId, levelId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 특정 서브섹터의 진행률 조회
     */
    @GetMapping("/user/{userId}/subsector/{subsectorId}")
    @Operation(summary = "서브섹터별 진행률 조회", description = "특정 사용자의 특정 서브섹터 진행률을 조회합니다.")
    public ResponseEntity<SubsectorProgressDto> getSubsectorProgress(
            @Parameter(description = "사용자 ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "서브섹터 ID", required = true)
            @PathVariable Long subsectorId) {
        
        log.info("사용자 {}의 서브섹터 {} 진행률 조회 요청", userId, subsectorId);
        
        try {
            SubsectorProgressDto subsectorProgress = userProgressService.getSubsectorProgress(userId, subsectorId);
            log.info("사용자 {}의 서브섹터 {} 진행률 조회 완료", userId, subsectorId);
            return ResponseEntity.ok(subsectorProgress);
        } catch (Exception e) {
            log.error("사용자 {}의 서브섹터 {} 진행률 조회 실패: {}", userId, subsectorId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 사용자의 레벨별 완료한 퀴즈 수 요약 조회
     */
    @GetMapping("/user/{userId}/summary")
    @Operation(summary = "사용자 진행률 요약", description = "사용자의 레벨별 완료한 퀴즈 수 요약을 조회합니다.")
    public ResponseEntity<List<LevelProgressDto>> getUserProgressSummary(
            @Parameter(description = "사용자 ID", required = true)
            @PathVariable Long userId) {
        
        log.info("사용자 {}의 진행률 요약 조회 요청", userId);
        
        try {
            // 전체 진행률을 가져와서 레벨별로 그룹화
            List<UserProgressDto> allProgress = userProgressService.getUserProgress(userId);
            
            // 레벨별로 그룹화하여 요약 생성
            List<LevelProgressDto> summary = allProgress.stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                            UserProgressDto::getLevelId,
                            java.util.stream.Collectors.toList()
                    ))
                    .entrySet().stream()
                    .map(entry -> {
                        Long levelId = entry.getKey();
                        List<UserProgressDto> levelProgress = entry.getValue();
                        
                        // 레벨 정보 가져오기
                        UserProgressDto firstProgress = levelProgress.get(0);
                        
                        // 통계 계산
                        int completedQuizzes = (int) levelProgress.stream()
                                .filter(p -> p.getFinishedAt() != null)
                                .count();
                        
                        int passedQuizzes = (int) levelProgress.stream()
                                .filter(p -> p.getPassed() != null && p.getPassed())
                                .count();
                        
                        int failedQuizzes = completedQuizzes - passedQuizzes;
                        
                        return LevelProgressDto.builder()
                                .levelId(levelId)
                                .levelNumber(firstProgress.getLevelNumber())
                                .levelTitle(firstProgress.getLevelTitle())
                                .subsectorId(firstProgress.getSubsectorId())
                                .subsectorName(firstProgress.getSubsectorName())
                                .learningGoal("") // 학습 목표는 별도 조회 필요
                                .status(LevelProgressDto.LevelStatus.IN_PROGRESS)
                                .totalQuizzes(0) // 전체 퀴즈 수는 별도 조회 필요
                                .completedQuizzes(completedQuizzes)
                                .passedQuizzes(passedQuizzes)
                                .failedQuizzes(failedQuizzes)
                                .correctAnswers(passedQuizzes)
                                .remainingToPass(Math.max(0, 0 - passedQuizzes))
                                .completionRate(0.0) // 전체 퀴즈 수가 필요
                                .passRate(completedQuizzes > 0 ? (double) passedQuizzes / completedQuizzes : 0.0)
                                .levelPassed(passedQuizzes >= 0) // 전체 퀴즈 수가 필요
                                .progressDetails(levelProgress)
                                .build();
                    })
                    .sorted((a, b) -> Integer.compare(a.getLevelNumber(), b.getLevelNumber()))
                    .collect(java.util.stream.Collectors.toList());
            
            log.info("사용자 {}의 진행률 요약 조회 완료: {}개 레벨", userId, summary.size());
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            log.error("사용자 {}의 진행률 요약 조회 실패: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
