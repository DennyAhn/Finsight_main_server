package com.fintech.server.quiz.service;

import com.fintech.server.quiz.dto.AnswerRequestDto;
import com.fintech.server.quiz.dto.AnswerResponseDto;
import com.fintech.server.entity.User;
import com.fintech.server.quiz.dto.QuizResponseDto;
import com.fintech.server.quiz.dto.QuizCompleteResponse;
import com.fintech.server.quiz.entity.QuestionOption;
import com.fintech.server.quiz.entity.Quiz;
import com.fintech.server.quiz.entity.UserAnswer;
import com.fintech.server.quiz.entity.UserProgress;
import com.fintech.server.quiz.repository.QuestionOptionRepository;
import com.fintech.server.quiz.repository.QuizRepository;
import com.fintech.server.quiz.repository.UserAnswerRepository;
import com.fintech.server.quiz.repository.UserProgressRepository;
import com.fintech.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final UserProgressRepository userProgressRepository;
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

    /**
     * 퀴즈 완료 처리 메소드
     */
    @Transactional
    public QuizCompleteResponse completeQuiz(Long quizId, Long userId) {
        // 1. 사용자와 퀴즈 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found with id: " + quizId));

        // 2. 사용자의 모든 답변 조회
        List<UserAnswer> userAnswers = userAnswerRepository.findByUserIdAndQuizId(userId, quizId);
        
        if (userAnswers.isEmpty()) {
            throw new IllegalArgumentException("No answers found for this quiz. Please complete the quiz first.");
        }

        // 3. 점수 계산
        int correctAnswers = (int) userAnswers.stream()
                .filter(UserAnswer::isCorrect)
                .count();
        int totalQuestions = userAnswers.size();
        boolean passed = correctAnswers >= 3; // 3문제 이상 맞춰야 통과 (4문제 기준)
        
        // 4. UserProgress 저장 (퀴즈 완료 기록) - 중복 방지
        List<UserProgress> existingProgress = userProgressRepository.findByUserIdAndQuizId(userId, quizId);
        UserProgress userProgress;
        
        if (!existingProgress.isEmpty()) {
            // 기존 진행 기록이 있으면 업데이트
            userProgress = existingProgress.get(0); // 가장 최근 기록
            log.info("기존 UserProgress 업데이트: userId={}, quizId={}", userId, quizId);
        } else {
            // 새로운 진행 기록 생성
            userProgress = new UserProgress();
            userProgress.setUser(user);
            userProgress.setQuiz(quiz);
            log.info("새로운 UserProgress 생성: userId={}, quizId={}", userId, quizId);
        }
        
        userProgress.setScore(correctAnswers); // 정답 수 = 점수 (required_quizzes와 동일)
        userProgress.setPassed(passed);
        userProgress.setStartedAt(LocalDateTime.now().minusMinutes(5)); // 대략적 시작 시간
        userProgress.setFinishedAt(LocalDateTime.now());
        userProgress.setTeachingViews(0); // 기본값
        userProgressRepository.save(userProgress);
        
        // 5. 배지 업데이트 (이미 구현됨)
        try {
            badgeService.updateUserBadgeProgress(userId);
            log.info("배지 진행 상황 업데이트 완료: userId={}", userId);
        } catch (Exception e) {
            log.error("배지 업그레이드 확인 중 오류 발생", e);
        }

        // 6. 응답 생성
        String message = passed ? 
            String.format("축하합니다! %d문제 중 %d문제를 맞혔습니다.", totalQuestions, correctAnswers) :
            String.format("아쉽습니다. %d문제 중 %d문제를 맞혔습니다. 다시 도전해보세요!", totalQuestions, correctAnswers);

        return QuizCompleteResponse.builder()
            .totalQuestions(totalQuestions)
            .correctAnswers(correctAnswers)
            .passed(passed)
            .score(correctAnswers) // required_quizzes와 동일한 값
            .message(message)
            .build();
    }

    /**
     * 퀴즈 다시풀기 메소드 (이전 답변 삭제 후 새로 시작)
     */
    @Transactional
    public void retryQuiz(Long quizId, Long userId) {
        // 1. 사용자와 퀴즈 존재 확인
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with id: " + userId);
        }
        
        if (!quizRepository.existsById(quizId)) {
            throw new RuntimeException("Quiz not found with id: " + quizId);
        }

        // 2. 해당 퀴즈의 이전 답변들 삭제
        List<UserAnswer> existingAnswers = userAnswerRepository.findByUserIdAndQuizId(userId, quizId);
        if (!existingAnswers.isEmpty()) {
            userAnswerRepository.deleteAll(existingAnswers);
            log.info("이전 답변 삭제 완료: userId={}, quizId={}, 삭제된 답변 수={}", 
                    userId, quizId, existingAnswers.size());
        }

        // 3. 해당 퀴즈의 이전 UserProgress 삭제 (선택사항)
        List<UserProgress> existingProgress = userProgressRepository.findByUserIdAndQuizId(userId, quizId);
        if (!existingProgress.isEmpty()) {
            userProgressRepository.deleteAll(existingProgress);
            log.info("이전 진행 기록 삭제 완료: userId={}, quizId={}, 삭제된 기록 수={}", 
                    userId, quizId, existingProgress.size());
        }

        log.info("퀴즈 다시풀기 준비 완료: userId={}, quizId={}", userId, quizId);
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

    /**
     * 사용자 총점수 조회 메서드
     */
    public java.util.Map<String, Object> getUserTotalScore(Long userId) {
        log.info("사용자 {}의 총점수 조회", userId);
        
        // 사용자 존재 확인
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with id: " + userId);
        }
        
        // 사용자의 모든 UserProgress 조회
        List<com.fintech.server.quiz.entity.UserProgress> progressList = 
            userProgressRepository.findByUserIdOrderByCreatedAtDesc(userId);
        
        // 총점수 계산
        int totalScore = progressList.stream()
                .filter(p -> p.getFinishedAt() != null) // 완료된 퀴즈만
                .mapToInt(com.fintech.server.quiz.entity.UserProgress::getScore)
                .sum();
        
        // 통계 정보
        int totalQuizzes = progressList.size();
        int completedQuizzes = (int) progressList.stream()
                .filter(p -> p.getFinishedAt() != null)
                .count();
        int passedQuizzes = (int) progressList.stream()
                .filter(p -> p.getPassed() != null && p.getPassed())
                .count();
        
        // 응답 객체 생성
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("userId", userId);
        response.put("totalScore", totalScore);
        response.put("totalQuizzes", totalQuizzes);
        response.put("completedQuizzes", completedQuizzes);
        response.put("passedQuizzes", passedQuizzes);
        response.put("averageScore", completedQuizzes > 0 ? (double) totalScore / completedQuizzes : 0.0);
        response.put("passRate", completedQuizzes > 0 ? (double) passedQuizzes / completedQuizzes : 0.0);
        
        log.info("사용자 {}의 총점수 조회 완료: 총점={}, 완료퀴즈={}, 통과퀴즈={}", 
                userId, totalScore, completedQuizzes, passedQuizzes);
        
        return response;
    }
}

