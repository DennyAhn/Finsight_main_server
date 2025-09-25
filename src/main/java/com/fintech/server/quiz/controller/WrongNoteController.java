package com.fintech.server.quiz.controller;

import com.fintech.server.quiz.dto.WrongNoteDto;
import com.fintech.server.quiz.service.WrongNoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wrong-notes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "오답 노트", description = "사용자 오답 노트 관리 API")
public class WrongNoteController {

    private final WrongNoteService wrongNoteService;

    @GetMapping
    @Operation(summary = "오답 노트 목록 조회", description = "사용자의 오답 노트 목록을 조회합니다.")
    public ResponseEntity<WrongNoteDto.ListResponse> getUserWrongNotes(
            @Parameter(description = "사용자 ID") @RequestParam Long userId,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "필터 (all, unresolved, pinned, needreview)") @RequestParam(defaultValue = "all") String filter) {
        
        WrongNoteDto.ListResponse response = wrongNoteService.getUserWrongNotes(userId, page, size, filter);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{noteId}")
    @Operation(summary = "특정 오답 노트 조회", description = "특정 오답 노트의 상세 정보를 조회합니다.")
    public ResponseEntity<WrongNoteDto.Response> getWrongNote(
            @Parameter(description = "사용자 ID") @RequestParam Long userId,
            @Parameter(description = "오답 노트 ID") @PathVariable Long noteId) {
        
        WrongNoteDto.Response response = wrongNoteService.getWrongNote(userId, noteId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{noteId}/personal-note")
    @Operation(summary = "개인 메모 업데이트", description = "오답 노트의 개인 메모를 업데이트합니다.")
    public ResponseEntity<WrongNoteDto.Response> updatePersonalNote(
            @Parameter(description = "사용자 ID") @RequestParam Long userId,
            @Parameter(description = "오답 노트 ID") @PathVariable Long noteId,
            @RequestBody String personalNoteMd) {
        
        WrongNoteDto.Response response = wrongNoteService.updatePersonalNote(userId, noteId, personalNoteMd);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{noteId}/toggle-resolved")
    @Operation(summary = "해결 상태 토글", description = "오답 노트의 해결 상태를 변경합니다.")
    public ResponseEntity<WrongNoteDto.Response> toggleResolved(
            @Parameter(description = "사용자 ID") @RequestParam Long userId,
            @Parameter(description = "오답 노트 ID") @PathVariable Long noteId) {
        
        WrongNoteDto.Response response = wrongNoteService.toggleResolved(userId, noteId);
        return ResponseEntity.ok(response);
    }


    @PutMapping("/{noteId}/mark-reviewed")
    @Operation(summary = "복습 완료 처리", description = "오답 노트를 복습 완료로 표시합니다.")
    public ResponseEntity<WrongNoteDto.Response> markAsReviewed(
            @Parameter(description = "사용자 ID") @RequestParam Long userId,
            @Parameter(description = "오답 노트 ID") @PathVariable Long noteId) {
        
        WrongNoteDto.Response response = wrongNoteService.markAsReviewed(userId, noteId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{noteId}")
    @Operation(summary = "오답 노트 삭제", description = "특정 오답 노트를 삭제합니다.")
    public ResponseEntity<Void> deleteWrongNote(
            @Parameter(description = "사용자 ID") @RequestParam Long userId,
            @Parameter(description = "오답 노트 ID") @PathVariable Long noteId) {
        
        wrongNoteService.deleteWrongNote(userId, noteId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/statistics")
    @Operation(summary = "오답 노트 통계", description = "사용자의 오답 노트 통계 정보를 조회합니다.")
    public ResponseEntity<WrongNoteDto.Statistics> getStatistics(
            @Parameter(description = "사용자 ID") @RequestParam Long userId) {
        
        WrongNoteDto.Statistics statistics = wrongNoteService.getWrongNoteStatistics(userId);
        return ResponseEntity.ok(statistics);
    }
}
