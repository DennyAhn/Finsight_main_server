package com.fintech.server.quiz.service;

import com.fintech.server.quiz.dto.AnswerRequestDto;
import com.fintech.server.quiz.dto.AnswerResponseDto;
import com.fintech.server.entity.User;
import com.fintech.server.quiz.dto.QuizResponseDto;
import com.fintech.server.quiz.entity.QuestionOption;
import com.fintech.server.quiz.entity.Quiz;
import com.fintech.server.quiz.entity.UserAnswer;
import com.fintech.server.quiz.repository.QuestionOptionRepository;
import com.fintech.server.quiz.repository.QuizRepository;
import com.fintech.server.quiz.repository.UserAnswerRepository;
import com.fintech.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final UserRepository userRepository; // 임시로 사용자 정보를 가져오기 위해 추가
    private final WrongNoteService wrongNoteService; // 오답 노트 서비스 추가
    private final BadgeService badgeService; // 배지 서비스 추가

    /**
     * 특정 ID의 퀴즈 정보를 조회하는 메소드
     */
    public QuizResponseDto findQuizById(Long quizId) {
        Quiz quiz = quizRepository.findByIdWithDetails(quizId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found with id: " + quizId));
        return convertToDto(quiz);
    }

    /**
     * 사용자가 제출한 답안을 채점하고 기록하는 메소드
     */
    @Transactional
    public AnswerResponseDto submitAnswer(AnswerRequestDto requestDto) {
        // Spring Security Context에서 인증된 사용자 ID 가져오기
        Long userId = getCurrentUserId(requestDto.getUserId());
        
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        QuestionOption selectedOption = questionOptionRepository.findById(requestDto.getSelectedOptionId())
                .orElseThrow(() -> new RuntimeException("Option not found with id: " + requestDto.getSelectedOptionId()));

        boolean isCorrect = selectedOption.isCorrect();

        // 사용자 답변 기록 저장
        UserAnswer userAnswer = new UserAnswer();
        userAnswer.setUser(currentUser);
        userAnswer.setQuestion(selectedOption.getQuestion());
        userAnswer.setSelectedOption(selectedOption);
        userAnswer.setCorrect(isCorrect);
        userAnswerRepository.save(userAnswer);

        // 틀린 답변인 경우 오답 노트 자동 생성
        if (!isCorrect) {
            try {
                wrongNoteService.createOrUpdateWrongNote(
                    currentUser.getId(), 
                    requestDto.getQuestionId(), 
                    requestDto.getSelectedOptionId()
                );
                log.info("오답 노트 자동 생성 완료: userId={}, questionId={}", 
                        currentUser.getId(), requestDto.getQuestionId());
            } catch (Exception e) {
                log.error("오답 노트 생성 중 오류 발생", e);
                // 오답 노트 생성 실패해도 답변 제출은 성공으로 처리
            }
        }

        // 퀴즈 답변 후 배지 업그레이드 확인
        try {
            badgeService.updateUserBadgeProgress(currentUser.getId());
            log.info("배지 진행 상황 업데이트 완료: userId={}", currentUser.getId());
        } catch (Exception e) {
            log.error("배지 업그레이드 확인 중 오류 발생", e);
        }

        // 정답 옵션 정보 조회
        QuestionOption correctOption = questionOptionRepository.findCorrectOptionByQuestionId(requestDto.getQuestionId())
                .orElseThrow(() -> new RuntimeException("Correct option not found for question id: " + requestDto.getQuestionId()));
        
        return AnswerResponseDto.builder()
                .isCorrect(isCorrect)
                .correctOptionId(correctOption.getId())
                .feedback(selectedOption.getQuestion().getAnswerExplanationMd())
                .build();
    }

    /**
     * 현재 인증된 사용자 ID를 가져오는 메서드
     * JWT 토큰이 있으면 토큰에서 추출, 없으면 fallback 사용자 ID 사용
     */
    private Long getCurrentUserId(Long fallbackUserId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.getPrincipal() instanceof Long) {
                Long authenticatedUserId = (Long) authentication.getPrincipal();
                log.debug("JWT 토큰에서 사용자 ID 추출: {}", authenticatedUserId);
                return authenticatedUserId;
            }
        } catch (Exception e) {
            log.warn("Spring Security Context에서 사용자 ID 추출 실패: {}", e.getMessage());
        }
        
        // JWT 토큰이 없거나 유효하지 않은 경우 fallback 사용
        if (fallbackUserId != null) {
            log.debug("Fallback 사용자 ID 사용: {}", fallbackUserId);
            return fallbackUserId;
        }
        
        // 기본값으로 1L 사용 (테스트 목적)
        log.warn("사용자 ID를 찾을 수 없어 기본값(1) 사용");
        return 1L;
    }

    private QuizResponseDto convertToDto(Quiz quiz) {
        return QuizResponseDto.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .questions(quiz.getQuestions() != null ? quiz.getQuestions().stream().map(question ->
                        QuizResponseDto.QuestionDto.builder()
                                .id(question.getId())
                                .stemMd(question.getStemMd())
                                .answerExplanationMd(question.getAnswerExplanationMd())
                                .hintMd(question.getHintMd())                           // 힌트 정보 추가
                                .teachingExplainerMd(question.getTeachingExplainerMd()) // 학습 패널 추가
                                .solvingKeypointsMd(question.getSolvingKeypointsMd())   // 핵심 포인트 추가
                                .type(question.getType() != null ? question.getType().name() : null)  // 문제 타입 추가
                                .article(question.getType() != null && 
                                         question.getType() == com.fintech.server.quiz.entity.Question.QuestionType.ARTICLE && 
                                         question.getArticle() != null ? 
                                        QuizResponseDto.ArticleDto.builder()
                                                .id(question.getArticle().getId())
                                                .title(question.getArticle().getTitle())
                                                .bodyMd(question.getArticle().getBodyMd())
                                                .imageUrl(question.getArticle().getImageUrl())
                                                .sourceNote(question.getArticle().getSourceNote())
                                                .build() : null)                        // 가상기사 정보 추가 (type이 ARTICLE인 경우만)
                                .options(question.getOptions() != null ? question.getOptions().stream()
                                        .sorted((o1, o2) -> o1.getId().compareTo(o2.getId())) // ID 기준 오름차순 정렬
                                        .map(option ->
                                        QuizResponseDto.OptionDto.builder()
                                                .id(option.getId())
                                                .label(option.getLabel())
                                                .contentMd(option.getContentMd())
                                                .build()
                                ).collect(Collectors.toList()) : Collections.emptyList())
                                .build()
                ).collect(Collectors.toList()) : Collections.emptyList())
                .build();
    }
}

