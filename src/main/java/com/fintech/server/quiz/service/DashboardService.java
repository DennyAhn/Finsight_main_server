package com.fintech.server.quiz.service;

import com.fintech.server.entity.User;
import com.fintech.server.quiz.dto.DashboardDto;
import com.fintech.server.quiz.dto.LevelProgressDto;
import com.fintech.server.quiz.entity.Level;
import com.fintech.server.quiz.entity.Quiz;
import com.fintech.server.quiz.entity.UserAnswer;
import com.fintech.server.quiz.entity.UserDailyActivity;
import com.fintech.server.quiz.repository.LevelRepository;
import com.fintech.server.quiz.repository.UserAnswerRepository;
import com.fintech.server.quiz.repository.UserDailyActivityRepository;
import com.fintech.server.quiz.entity.Badge;
import com.fintech.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final UserRepository userRepository;
    private final LevelRepository levelRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final UserDailyActivityRepository userDailyActivityRepository;
    private final LevelService levelService;
    private final BadgeService badgeService;
    private final QuizService quizService;

    /**
     * 사용자 대시보드 조회
     */
    public DashboardDto getUserDashboard(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // 사용자 기본 정보
        DashboardDto.UserInfoDto userInfo = getUserInfo(user, userId);
        
        // 학습 통계
        DashboardDto.LearningStatsDto learningStats = getLearningStats(userId);
        
        // 주간 진행 현황
        List<DashboardDto.WeeklyProgressDto> weeklyProgress = getWeeklyProgress(userId);
        
        // 최근 활동
        List<DashboardDto.RecentActivityDto> recentActivities = getRecentActivities(userId);
        
        // 다음 레벨 추천
        DashboardDto.NextLevelRecommendationDto nextLevelRecommendation = getNextLevelRecommendation(userId);
        
        // 현재 진행 중인 레벨
        DashboardDto.CurrentLevelSessionDto currentLevelSession = getCurrentLevelSession(userId);

        return DashboardDto.builder()
                .userInfo(userInfo)
                .learningStats(learningStats)
                .weeklyProgress(weeklyProgress)
                .recentActivities(recentActivities)
                .nextLevelRecommendation(nextLevelRecommendation)
                .currentLevelSession(currentLevelSession)
                .build();
    }

    private DashboardDto.UserInfoDto getUserInfo(User user, Long userId) {
        // 벳지 시스템을 사용한 현재 레벨 계산
        String currentLevelTitle = "레벨 없음";
        Integer currentLevelNumber = 0;
        
        // 벳지 데이터 초기화는 한 번만 실행 (이미 초기화되었는지 확인)
        if (badgeService.getBadgeCount() == 0) {
            badgeService.initializeBadges();
        }
        
        // 사용자의 벳지 진행 상황 업데이트 (필요시에만)
        badgeService.updateUserBadgeProgressIfNeeded(userId);
        
        // 현재 벳지 레벨 조회
        Badge currentBadge = badgeService.getCurrentBadgeLevel(userId);
        
        if (currentBadge != null) {
            currentLevelTitle = currentBadge.getName();
            currentLevelNumber = currentBadge.getLevelNumber();
        }

        // 스트릭 계산 - 연속 학습 일수
        int streak = calculateStreak(userId);

        // 총 점수 계산 - 실제 퀴즈 점수 합계
        int totalScore = calculateTotalScore(userId);

        return DashboardDto.UserInfoDto.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .currentLevelTitle(currentLevelTitle)
                .currentLevelNumber(currentLevelNumber)
                .streak(streak)
                .totalScore(totalScore)
                .build();
    }

    private DashboardDto.LearningStatsDto getLearningStats(Long userId) {
        // 실제 계산 메서드들 사용
        int totalLevelsCompleted = calculateCompletedLevels(userId);
        int totalQuizzesCompleted = calculateCompletedQuizzes(userId);
        int totalQuestionsAnswered = userAnswerRepository.findByUserId(userId).size();
        int totalMinutesSpent = calculateTotalMinutesSpent(userId);
        double averageScore = calculateAverageScore(userId);

        return DashboardDto.LearningStatsDto.builder()
                .totalLevelsCompleted(totalLevelsCompleted)
                .totalQuizzesCompleted(totalQuizzesCompleted)
                .totalQuestionsAnswered(totalQuestionsAnswered)
                .totalMinutesSpent(totalMinutesSpent)
                .averageScore(averageScore)
                .build();
    }

    private List<DashboardDto.WeeklyProgressDto> getWeeklyProgress(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
        
        List<DashboardDto.WeeklyProgressDto> weeklyProgress = new ArrayList<>();
        
        for (int i = 0; i < 7; i++) {
            LocalDate date = weekStart.plusDays(i);
            boolean completed = hasUserActivityOnDate(userId, date);
            
            weeklyProgress.add(DashboardDto.WeeklyProgressDto.builder()
                    .dayOfMonth(date.getDayOfMonth())
                    .completed(completed)
                    .minutesSpent(completed ? 30 : 0) // 임시
                    .quizzesCompleted(completed ? 2 : 0) // 임시
                    .build());
        }
        
        return weeklyProgress;
    }

    private boolean hasUserActivityOnDate(Long userId, LocalDate date) {
        // 해당 날짜에 사용자 활동이 있는지 확인
        return userAnswerRepository.findByUserId(userId).stream()
                .anyMatch(answer -> answer.getAnsweredAt().toLocalDate().equals(date));
    }

    private List<DashboardDto.RecentActivityDto> getRecentActivities(Long userId) {
        // 최근 활동 데이터 조회 - 최근 7일간의 활동
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        
        List<UserAnswer> recentAnswers = userAnswerRepository.findByUserId(userId).stream()
                .filter(answer -> answer.getAnsweredAt().isAfter(sevenDaysAgo))
                .sorted(Comparator.comparing(UserAnswer::getAnsweredAt).reversed())
                .limit(10)
                .collect(Collectors.toList());

        return recentAnswers.stream()
                .map(answer -> {
                    Quiz quiz = answer.getQuestion().getQuiz();
                    return DashboardDto.RecentActivityDto.builder()
                            .type("QUIZ_COMPLETED")
                            .title(quiz.getTitle())
                            .score(answer.isCorrect() ? 100 : 0)
                            .activityTime(answer.getAnsweredAt())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private DashboardDto.NextLevelRecommendationDto getNextLevelRecommendation(Long userId) {
        // 현재 진행 중인 레벨 확인
        Level currentLevel = levelRepository.findById(1L).orElse(null); // 임시
        
        if (currentLevel != null) {
            return DashboardDto.NextLevelRecommendationDto.builder()
                    .levelId(currentLevel.getId())
                    .levelTitle(currentLevel.getTitle())
                    .subsectorName(currentLevel.getSubsector().getName())
                    .reason("현재 레벨 진행 중")
                    .progressPercentage(50)
                    .remainingQuizzes(2)
                    .difficulty("EASY")
                    .estimatedTime(30)
                    .learningGoal(currentLevel.getLearningGoal())
                    .build();
        }
        
        return null;
    }

    private DashboardDto.CurrentLevelSessionDto getCurrentLevelSession(Long userId) {
        // 현재 진행 중인 레벨 세션 조회
        try {
            LevelProgressDto progress = levelService.getLevelProgress(1L, userId, 1L); // 임시 (subsectorId 추가)
            
            return DashboardDto.CurrentLevelSessionDto.builder()
                    .sessionId("level_" + progress.getLevelId() + "_" + userId)
                    .levelId(progress.getLevelId())
                    .levelTitle(progress.getLevelTitle())
                    .subsectorName(progress.getSubsectorName())
                    .startedAt(progress.getStartedAt() != null ? progress.getStartedAt().toString() : null)
                    .timeLimit(progress.getTimeLimit())
                    .timeRemaining(progress.getTimeLimit() - progress.getTimeSpent())
                    .currentQuizIndex(progress.getCompletedQuizzes())
                    .completedQuizzes(progress.getCompletedQuizzes())
                    .correctAnswers(progress.getCorrectAnswers())
                    .remainingToPass(progress.getRemainingToPass())
                    .status(progress.getStatus().name())
                    .build();
        } catch (Exception e) {
            log.warn("Failed to get current level session for user: {}", userId, e);
            return null;
        }
    }

    // 실제 계산 메서드들
    private int calculateStreak(Long userId) {
        // 연속 학습 일수 계산
        List<UserDailyActivity> activities = userDailyActivityRepository.findByIdUserIdOrderByIdActivityDateDesc(userId);
        if (activities.isEmpty()) {
            return 0;
        }

        int streak = 0;
        LocalDate currentDate = LocalDate.now();
        
        for (UserDailyActivity activity : activities) {
            if (activity.getActivityDate().equals(currentDate) || 
                activity.getActivityDate().equals(currentDate.minusDays(streak))) {
                streak++;
                currentDate = currentDate.minusDays(1);
            } else {
                break;
            }
        }
        
        return streak;
    }

    private int calculateTotalScore(Long userId) {
        // QuizService의 정확한 총점수 계산 사용 (중복 제거 포함)
        try {
            java.util.Map<String, Object> scoreData = quizService.getUserTotalScore(userId);
            return (Integer) scoreData.get("totalScore");
        } catch (Exception e) {
            log.warn("Failed to get total score from QuizService for user: {}", userId, e);
            return 0;
        }
    }

    private int calculateCompletedLevels(Long userId) {
        // 완료된 레벨 수 계산
        List<Level> allLevels = levelRepository.findAll();
        int completedLevels = 0;
        
        for (Level level : allLevels) {
            try {
                LevelProgressDto progress = levelService.getLevelProgress(level.getId(), userId, level.getSubsector().getId());
                if (progress.getStatus() == LevelProgressDto.LevelStatus.COMPLETED && progress.isLevelPassed()) {
                    completedLevels++;
                }
            } catch (Exception e) {
                // 레벨 진행 상황을 가져올 수 없는 경우 무시
            }
        }
        
        return completedLevels;
    }

    private int calculateCompletedQuizzes(Long userId) {
        // QuizService의 정확한 완료된 퀴즈 수 계산 사용 (중복 제거 포함)
        try {
            java.util.Map<String, Object> scoreData = quizService.getUserTotalScore(userId);
            return (Integer) scoreData.get("completedQuizzes");
        } catch (Exception e) {
            log.warn("Failed to get completed quizzes from QuizService for user: {}", userId, e);
            return 0;
        }
    }

    private int calculateTotalMinutesSpent(Long userId) {
        // 총 학습 시간 계산 (분)
        List<UserAnswer> userAnswers = userAnswerRepository.findByUserId(userId);
        // TODO: 실제 소요 시간 계산 로직 필요 (현재는 임시로 5분씩)
        return userAnswers.size() * 5;
    }

    private double calculateAverageScore(Long userId) {
        // QuizService의 정확한 평균 점수 계산 사용 (중복 제거 포함)
        try {
            java.util.Map<String, Object> scoreData = quizService.getUserTotalScore(userId);
            return (Double) scoreData.get("averageScore");
        } catch (Exception e) {
            log.warn("Failed to get average score from QuizService for user: {}", userId, e);
            return 0.0;
        }
    }
}
