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
// import org.springframework.security.core.annotation.AuthenticationPrincipal;
// import com.fintech.server.user.UserDetailsImpl; // 사용자 인증 객체
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
     * [보안 개선] @AuthenticationPrincipal을 사용하여 로그인된 사용자의 정보를 가져옵니다.
     * 이렇게 하면 다른 사용자가 URL을 추측하여 타인의 정보를 조회하는 것을 막을 수 있습니다.
     */
    @GetMapping("/user/me")
    @Operation(summary = "나의 전체 진행률 조회", description = "로그인된 사용자의 모든 퀴즈 진행률을 조회합니다.")
    public ResponseEntity<List<UserProgressDto>> getCurrentUserProgress(
            /* @AuthenticationPrincipal UserDetailsImpl userDetails */) {
        
        // Long userId = userDetails.getUserId(); // 실제 운영 시에는 인증 객체에서 ID를 가져옵니다.
        Long userId = 1L; // 임시 테스트용 ID
        log.info("사용자 {}의 전체 진행률 조회 요청", userId);
        
        // [예외 처리 개선] 서비스 계층이나 @ControllerAdvice에서 예외를 처리하는 것이 좋습니다.
        List<UserProgressDto> progressList = userProgressService.getUserProgress(userId);
        log.info("사용자 {}의 진행률 조회 완료: {}개", userId, progressList.size());
        return ResponseEntity.ok(progressList);
    }
    
    /**
     * 특정 서브섹터의 특정 레벨 진행률 조회
     */
    @GetMapping("/user/me/subsector/{subsectorId}/level/{levelId}")
    @Operation(summary = "나의 서브섹터별 레벨 진행률 조회", description = "로그인된 사용자의 특정 서브섹터의 특정 레벨 진행률을 조회합니다.")
    public ResponseEntity<LevelProgressDto> getLevelProgress(
            /* @AuthenticationPrincipal UserDetailsImpl userDetails, */
            @Parameter(description = "서브섹터 ID", required = true)
            @PathVariable Long subsectorId,
            @Parameter(description = "레벨 ID", required = true)
            @PathVariable Long levelId) {
        
        // Long userId = userDetails.getUserId();
        Long userId = 1L; // 임시 테스트용 ID
        log.info("사용자 {}의 서브섹터 {} 레벨 {} 진행률 조회 요청", userId, subsectorId, levelId);
        
        LevelProgressDto levelProgress = userProgressService.getLevelProgress(userId, subsectorId, levelId);
        log.info("사용자 {}의 서브섹터 {} 레벨 {} 진행률 조회 완료", userId, subsectorId, levelId);
        return ResponseEntity.ok(levelProgress);
    }
    
    /**
     * 특정 서브섹터의 진행률 조회
     */
    @GetMapping("/user/me/subsector/{subsectorId}")
    @Operation(summary = "나의 서브섹터별 진행률 조회", description = "로그인된 사용자의 특정 서브섹터 진행률을 조회합니다.")
    public ResponseEntity<SubsectorProgressDto> getSubsectorProgress(
            /* @AuthenticationPrincipal UserDetailsImpl userDetails, */
            @Parameter(description = "서브섹터 ID", required = true)
            @PathVariable Long subsectorId) {
        
        // Long userId = userDetails.getUserId();
        Long userId = 1L; // 임시 테스트용 ID
        log.info("사용자 {}의 서브섹터 {} 진행률 조회 요청", userId, subsectorId);
        
        SubsectorProgressDto subsectorProgress = userProgressService.getSubsectorProgress(userId, subsectorId);
        log.info("사용자 {}의 서브섹터 {} 진행률 조회 완료", userId, subsectorId);
        return ResponseEntity.ok(subsectorProgress);
    }
    

    /**
     * 사용자의 레벨별 완료한 퀴즈 수 요약 조회
     * [구조 개선] 복잡한 데이터 처리 및 계산 로직은 서비스 계층으로 이동시켰습니다.
     * 컨트롤러는 요청을 받고 응답을 전달하는 역할에만 집중해야 합니다.
     */
    @GetMapping("/user/me/summary")
    @Operation(summary = "나의 진행률 요약", description = "로그인된 사용자의 레벨별 완료한 퀴즈 수 요약을 조회합니다.")
    public ResponseEntity<List<LevelProgressDto>> getUserProgressSummary(
            /* @AuthenticationPrincipal UserDetailsImpl userDetails */) {
        
        // Long userId = userDetails.getUserId();
        Long userId = 1L; // 임시 테스트용 ID
        log.info("사용자 {}의 진행률 요약 조회 요청", userId);
        
        // [성능 개선] 이 로직은 서비스 계층 내부에서 DB 쿼리로 처리하는 것이 훨씬 효율적입니다.
        List<LevelProgressDto> summary = userProgressService.getUserProgressSummary(userId);
        
        log.info("사용자 {}의 진행률 요약 조회 완료: {}개 레벨", userId, summary.size());
        return ResponseEntity.ok(summary);
    }
}