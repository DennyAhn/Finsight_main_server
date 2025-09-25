package com.fintech.server.quiz.controller;

import com.fintech.server.quiz.dto.AnswerRequestDto; // DTO 임포트 추가
import com.fintech.server.quiz.dto.AnswerResponseDto; // DTO 임포트 추가
import com.fintech.server.quiz.dto.QuizResponseDto;
import com.fintech.server.quiz.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*; // PostMapping, RequestBody 임포트 추가

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/quizzes") 
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    /**
     * 특정 ID의 퀴즈 정보를 조회하는 API
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getQuiz(@PathVariable("id") Long quizId) {
        try {
            QuizResponseDto quizResponse = quizService.findQuizById(quizId);
            return ResponseEntity.ok(quizResponse);
        } catch (RuntimeException e) { // 더 구체적인 예외 처리
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error retrieving quiz: " + e.getMessage());
        }
    }

    /**
     * 사용자가 제출한 답안을 채점하는 API
     */
    @PostMapping("/submit-answer")
    public ResponseEntity<?> submitAnswer(@RequestBody AnswerRequestDto requestDto) {
        try {
            AnswerResponseDto response = quizService.submitAnswer(requestDto);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid request: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Internal server error: " + e.getMessage());
        }
    }

    /**
     * 퀴즈 완료 처리 API
     */
    @PostMapping("/{id}/complete")
    public ResponseEntity<?> completeQuiz(
            @PathVariable("id") Long quizId,
            @RequestParam("userId") Long userId) {
        try {
            // 퀴즈 완료 로직 구현 - 실제로는 QuizService에서 처리해야 함
            Map<String, Object> response = new HashMap<>();
            response.put("totalQuestions", 1);
            response.put("correctAnswers", 1);
            response.put("passed", true);
            
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error completing quiz: " + e.getMessage());
        }
    }
}




/**
     * 특정 퀴즈를 조회하는 API
     * 
     * 🌐 HTTP Method: GET
     * 🔗 URL: /api/quizzes/{id}
     * 
     * 📥 Request:
     * - Path Parameter: id (Long) - 조회할 퀴즈의 고유 ID
     * 
     * 📤 Response (200 OK):
     * {
     *   "id": 1,
     *   "title": "금융 기초 퀴즈",
     *   "questions": [
     *     {
     *       "id": 1,
     *       "stemMd": "## 문제 1\n다음 중 올바른 것은?",
     *       "answerExplanationMd": "## 해설\n정답은 A입니다.",
     *       "options": [
     *         {
     *           "label": "A",
     *           "contentMd": "첫 번째 선택지"
     *         },
     *         {
     *           "label": "B", 
     *           "contentMd": "두 번째 선택지"
     *         }
     *       ]
     *     }
     *   ]
     * }
     * 
     * 📤 Error Response (400 Bad Request):
     * - 존재하지 않는 퀴즈 ID를 요청한 경우
     * 
     * 💡 프론트엔드 사용 팁:
     * - stemMd, answerExplanationMd, contentMd 필드는 마크다운 형식입니다
     * - 마크다운 렌더링 라이브러리 사용을 권장합니다
     * - 정답 정보는 포함되지 않으므로 별도 채점 API가 필요합니다
     */