package com.fintech.server.quiz.controller;

import com.fintech.server.quiz.dto.AdminWrongNoteDto;
import com.fintech.server.quiz.service.AdminWrongNoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/wrong-notes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "관리자 오답 노트", description = "관리자용 오답 노트 통계 및 관리 API")
public class AdminWrongNoteController {

    private final AdminWrongNoteService adminWrongNoteService;

    @GetMapping("/statistics/overall")
    @Operation(summary = "전체 오답 노트 통계", description = "모든 섹터의 오답 노트 통계를 조회합니다.")
    public ResponseEntity<AdminWrongNoteDto.OverallStatistics> getOverallStatistics() {
        AdminWrongNoteDto.OverallStatistics statistics = adminWrongNoteService.getOverallStatistics();
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/statistics/sector/{sectorId}")
    @Operation(summary = "섹터별 오답 노트 통계", description = "특정 섹터의 서브섹터별 오답 노트 통계를 조회합니다.")
    public ResponseEntity<AdminWrongNoteDto.SectorStatistics> getSectorStatistics(
            @Parameter(description = "섹터 ID") @PathVariable Long sectorId) {
        
        AdminWrongNoteDto.SectorStatistics statistics = adminWrongNoteService.getSectorStatistics(sectorId);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/statistics/subsector/{subsectorId}")
    @Operation(summary = "서브섹터별 오답 노트 통계", description = "특정 서브섹터의 레벨별 오답 노트 통계를 조회합니다.")
    public ResponseEntity<AdminWrongNoteDto.SubsectorStatistics> getSubsectorStatistics(
            @Parameter(description = "서브섹터 ID") @PathVariable Long subsectorId) {
        
        AdminWrongNoteDto.SubsectorStatistics statistics = adminWrongNoteService.getSubsectorStatistics(subsectorId);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/statistics/quiz/{quizId}")
    @Operation(summary = "퀴즈별 오답 노트 통계", description = "특정 퀴즈의 문제별 오답 노트 통계를 조회합니다.")
    public ResponseEntity<AdminWrongNoteDto.QuizStatistics> getQuizStatistics(
            @Parameter(description = "퀴즈 ID") @PathVariable Long quizId) {
        
        AdminWrongNoteDto.QuizStatistics statistics = adminWrongNoteService.getQuizStatistics(quizId);
        return ResponseEntity.ok(statistics);
    }

    // 관리자 대시보드 기능들

    @GetMapping("/dashboard")
    @Operation(summary = "관리자 대시보드", description = "관리자 대시보드용 요약 통계를 조회합니다.")
    public ResponseEntity<AdminWrongNoteDto.OverallStatistics> getAdminDashboard() {
        // 전체 통계와 동일하지만 대시보드용으로 별도 엔드포인트 제공
        AdminWrongNoteDto.OverallStatistics statistics = adminWrongNoteService.getOverallStatistics();
        return ResponseEntity.ok(statistics);
    }

}
