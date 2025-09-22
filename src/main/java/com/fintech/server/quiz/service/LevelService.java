package com.fintech.server.quiz.service;

import com.fintech.server.quiz.dto.LevelCompletionDto;
import com.fintech.server.quiz.dto.LevelProgressDto;
import com.fintech.server.quiz.entity.Level;
import com.fintech.server.quiz.entity.Quiz;
import com.fintech.server.quiz.entity.UserAnswer;
import com.fintech.server.quiz.repository.LevelRepository;
import com.fintech.server.quiz.repository.QuizRepository;
import com.fintech.server.quiz.repository.UserAnswerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LevelService {

    private final LevelRepository levelRepository;
    private final QuizRepository quizRepository;
    private final UserAnswerRepository userAnswerRepository;

    private static final int QUESTIONS_PER_LEVEL = 4;
    private static final int PASS_SCORE = 3; // 4문제 중 3문제 이상 맞춰야 통과

    /**
     * 레벨 진행 상황 조회
     */
    public LevelProgressDto getLevelProgress(Long levelId, Long userId) {
        Level level = levelRepository.findById(levelId)
                .orElseThrow(() -> new RuntimeException("Level not found with id: " + levelId));

        List<Quiz> quizzes = quizRepository.findByLevelIdOrderById(levelId);
        if (quizzes.size() != QUESTIONS_PER_LEVEL) {
            throw new RuntimeException("Level must have exactly " + QUESTIONS_PER_LEVEL + " quizzes");
        }

        // 퀴즈별 진행 상황 조회
        List<LevelProgressDto.QuizProgressDto> quizProgressList = new ArrayList<>();
        int completedQuizzes = 0;
        int correctAnswers = 0;
        LocalDateTime startedAt = null;
        LocalDateTime completedAt = null;
        int totalTimeSpent = 0;

        for (int i = 0; i < quizzes.size(); i++) {
            Quiz quiz = quizzes.get(i);
            LevelProgressDto.QuizProgressDto quizProgress = getQuizProgress(quiz, userId, i + 1);
            quizProgressList.add(quizProgress);

            if (quizProgress.getStatus() == LevelProgressDto.QuizStatus.COMPLETED) {
                completedQuizzes++;
                correctAnswers += quizProgress.getCorrectAnswers();
                totalTimeSpent += quizProgress.getTimeSpent();
                
                if (startedAt == null) {
                    startedAt = quizProgress.getCompletedAt().minusSeconds(quizProgress.getTimeSpent());
                }
                completedAt = quizProgress.getCompletedAt();
            }
        }

        // 레벨 상태 결정
        LevelProgressDto.LevelStatus levelStatus = determineLevelStatus(completedQuizzes, correctAnswers);
        
        // 다음 레벨 정보
        Long nextLevelId = getNextLevelId(levelId);
        String nextLevelTitle = getNextLevelTitle(nextLevelId);
        boolean nextLevelUnlocked = levelStatus == LevelProgressDto.LevelStatus.COMPLETED;

        return LevelProgressDto.builder()
                .levelId(levelId)
                .levelTitle(level.getTitle())
                .subsectorName(level.getSubsector().getName())
                .levelNumber(level.getLevelNumber())
                .learningGoal(level.getLearningGoal())
                .status(levelStatus)
                .completedQuizzes(completedQuizzes)
                .totalQuizzes(QUESTIONS_PER_LEVEL)
                .correctAnswers(correctAnswers)
                .remainingToPass(Math.max(0, PASS_SCORE - correctAnswers))
                .startedAt(startedAt)
                .completedAt(completedAt)
                .timeSpent(totalTimeSpent)
                .timeLimit(QUESTIONS_PER_LEVEL * 15 * 60) // 4문제 × 15분
                .quizProgress(quizProgressList)
                .nextLevelId(nextLevelId)
                .nextLevelTitle(nextLevelTitle)
                .nextLevelUnlocked(nextLevelUnlocked)
                .build();
    }

    /**
     * 레벨 완료 처리
     */
    @Transactional
    public LevelCompletionDto completeLevel(Long levelId, Long userId) {
        LevelProgressDto progress = getLevelProgress(levelId, userId);
        
        if (progress.getStatus() != LevelProgressDto.LevelStatus.COMPLETED) {
            throw new RuntimeException("Level is not completed yet");
        }

        // 레벨 완료 정보 생성
        boolean isPassed = progress.getCorrectAnswers() >= PASS_SCORE;
        int pointsEarned = calculatePoints(progress.getCorrectAnswers(), progress.getTimeSpent());
        
        LevelCompletionDto completion = LevelCompletionDto.builder()
                .levelId(levelId)
                .levelTitle(progress.getLevelTitle())
                .isCompleted(true)
                .isPassed(isPassed)
                .correctAnswers(progress.getCorrectAnswers())
                .totalQuestions(QUESTIONS_PER_LEVEL)
                .score(progress.getCorrectAnswers())
                .timeSpent(progress.getTimeSpent())
                .completedAt(LocalDateTime.now())
                .nextLevelId(progress.getNextLevelId())
                .nextLevelTitle(progress.getNextLevelTitle())
                .nextLevelUnlocked(isPassed)
                .pointsEarned(pointsEarned)
                .badgeEarned(isPassed ? "Level " + progress.getLevelNumber() + " Master" : null)
                .achievementMessage(isPassed ? 
                    "축하합니다! 레벨 " + progress.getLevelNumber() + "을 완료했습니다!" :
                    "아쉽게도 레벨 " + progress.getLevelNumber() + "을 통과하지 못했습니다. 다시 도전해보세요!")
                .build();

        log.info("Level completed: levelId={}, userId={}, passed={}, score={}/{}", 
                levelId, userId, isPassed, progress.getCorrectAnswers(), QUESTIONS_PER_LEVEL);

        return completion;
    }

    /**
     * 퀴즈 진행 상황 조회
     */
    private LevelProgressDto.QuizProgressDto getQuizProgress(Quiz quiz, Long userId, int quizNumber) {
        // 사용자 답변 조회
        List<UserAnswer> userAnswers = userAnswerRepository.findByUserIdAndQuizId(userId, quiz.getId());
        
        if (userAnswers.isEmpty()) {
            return LevelProgressDto.QuizProgressDto.builder()
                    .quizId(quiz.getId())
                    .quizTitle(quiz.getTitle())
                    .quizNumber(quizNumber)
                    .status(LevelProgressDto.QuizStatus.NOT_STARTED)
                    .correctAnswers(0)
                    .totalQuestions(1) // 각 퀴즈는 1문제
                    .completedAt(null)
                    .timeSpent(0)
                    .passed(false)
                    .build();
        }

        // 정답 개수 계산
        int correctAnswers = (int) userAnswers.stream()
                .filter(UserAnswer::isCorrect)
                .count();

        // 퀴즈 완료 시간
        LocalDateTime completedAt = userAnswers.stream()
                .map(UserAnswer::getAnsweredAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        // 소요 시간 계산 (대략적)
        int timeSpent = completedAt != null ? 300 : 0; // 기본 5분

        boolean passed = correctAnswers >= 1; // 1문제 중 1문제 맞춰야 통과

        return LevelProgressDto.QuizProgressDto.builder()
                .quizId(quiz.getId())
                .quizTitle(quiz.getTitle())
                .quizNumber(quizNumber)
                .status(LevelProgressDto.QuizStatus.COMPLETED)
                .correctAnswers(correctAnswers)
                .totalQuestions(1)
                .completedAt(completedAt)
                .timeSpent(timeSpent)
                .passed(passed)
                .build();
    }

    /**
     * 레벨 상태 결정
     */
    private LevelProgressDto.LevelStatus determineLevelStatus(int completedQuizzes, int correctAnswers) {
        if (completedQuizzes == 0) {
            return LevelProgressDto.LevelStatus.NOT_STARTED;
        } else if (completedQuizzes < QUESTIONS_PER_LEVEL) {
            return LevelProgressDto.LevelStatus.IN_PROGRESS;
        } else if (correctAnswers >= PASS_SCORE) {
            return LevelProgressDto.LevelStatus.COMPLETED;
        } else {
            return LevelProgressDto.LevelStatus.FAILED;
        }
    }

    /**
     * 다음 레벨 ID 조회
     */
    private Long getNextLevelId(Long currentLevelId) {
        // 현재 레벨의 다음 레벨 ID를 조회하는 로직
        // 실제 구현에서는 Level 엔티티에 nextLevelId 필드가 있거나
        // levelNumber를 기준으로 다음 레벨을 찾는 로직이 필요
        return currentLevelId + 1; // 임시 구현
    }

    /**
     * 다음 레벨 제목 조회
     */
    private String getNextLevelTitle(Long nextLevelId) {
        if (nextLevelId == null) return null;
        
        return levelRepository.findById(nextLevelId)
                .map(Level::getTitle)
                .orElse("다음 레벨");
    }

    /**
     * 포인트 계산
     */
    private int calculatePoints(int correctAnswers, int timeSpent) {
        int basePoints = correctAnswers * 100; // 문제당 100점
        int timeBonus = Math.max(0, 60 - (timeSpent / 60)) * 10; // 시간 보너스
        return basePoints + timeBonus;
    }
}
