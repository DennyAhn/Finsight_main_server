package com.fintech.server.quiz.service;

import com.fintech.server.quiz.dto.LevelProgressDto;
import com.fintech.server.quiz.dto.SubsectorProgressDto;
import com.fintech.server.quiz.dto.UserProgressDto;
import com.fintech.server.quiz.entity.Level;
import com.fintech.server.quiz.entity.Subsector;
import com.fintech.server.quiz.entity.UserProgress;
import com.fintech.server.quiz.repository.UserProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserProgressService {
    
    private final UserProgressRepository userProgressRepository;
    
    /**
     * 사용자의 모든 진행률 조회
     */
    public List<UserProgressDto> getUserProgress(Long userId) {
        log.info("사용자 {}의 진행률 조회", userId);
        
        List<UserProgress> progressList = userProgressRepository.findByUserIdOrderByCreatedAtDesc(userId);
        
        return progressList.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * 특정 서브섹터의 특정 레벨 진행률 조회 (징검다리 기능 포함)
     */
    public LevelProgressDto getLevelProgress(Long userId, Long subsectorId, Long levelId) {
        log.info("사용자 {}의 서브섹터 {} 레벨 {} 진행률 조회 (징검다리 기능 포함)", userId, subsectorId, levelId);
        
        // 서브섹터와 레벨을 모두 고려하여 진행률 조회
        List<UserProgress> progressList = userProgressRepository.findByUserIdAndSubsectorIdAndLevelId(userId, subsectorId, levelId);
        
        if (progressList.isEmpty()) {
            log.warn("사용자 {}의 레벨 {} 진행률이 없습니다", userId, levelId);
            return LevelProgressDto.builder()
                    .levelId(levelId)
                    .completedQuizzes(0)
                    .passedQuizzes(0)
                    .failedQuizzes(0)
                    .completionRate(0.0)
                    .passRate(0.0)
                    .progressDetails(new ArrayList<>())
                    .steps(new ArrayList<>())
                    .isStepPassed(false)
                    .currentStep(1)
                    .build();
        }
        
        // 레벨 정보 가져오기
        UserProgress firstProgress = progressList.get(0);
        Level level = firstProgress.getQuiz().getLevel();
        
        // 통계 계산
        int completedQuizzes = (int) progressList.stream()
                .filter(p -> p.getFinishedAt() != null)
                .count();
        
        int passedQuizzes = (int) progressList.stream()
                .filter(p -> p.getPassed() != null && p.getPassed())
                .count();
        
        int failedQuizzes = completedQuizzes - passedQuizzes;
        
        // 전체 퀴즈 수 조회
        Long totalQuizzes = userProgressRepository.countQuizzesByLevelId(levelId);
        
        // 완료율과 통과율 계산
        double completionRate = totalQuizzes > 0 ? (double) completedQuizzes / totalQuizzes : 0.0;
        double passRate = completedQuizzes > 0 ? (double) passedQuizzes / completedQuizzes : 0.0;
        
        // 서브섹터 정보
        Subsector subsector = level.getSubsector();
        
        // 징검다리 정보 생성
        List<com.fintech.server.quiz.dto.StepProgressDto> steps = createStepProgress(levelId, userId, completedQuizzes, passedQuizzes, level);
        boolean isStepPassed = passedQuizzes >= 3; // 75% 이상 통과 (4문제 중 3문제 이상)
        int currentStep = calculateCurrentStep(completedQuizzes);
        
        return LevelProgressDto.builder()
                .levelId(levelId)
                .levelNumber(level.getLevelNumber())
                .levelTitle(level.getTitle())
                .subsectorId(subsector.getId())
                .subsectorName(subsector.getName())
                .learningGoal(level.getLearningGoal())
                .status(LevelProgressDto.LevelStatus.IN_PROGRESS)
                .totalQuizzes(totalQuizzes.intValue())
                .completedQuizzes(completedQuizzes)
                .passedQuizzes(passedQuizzes)
                .failedQuizzes(failedQuizzes)
                .correctAnswers(passedQuizzes)
                .remainingToPass(Math.max(0, totalQuizzes.intValue() - passedQuizzes))
                .completionRate(completionRate)
                .passRate(passRate)
                .levelPassed(passedQuizzes >= totalQuizzes.intValue())
                .progressDetails(progressList.stream()
                        .map(this::convertToDto)
                        .collect(Collectors.toList()))
                
                // 징검다리 정보 추가
                .steps(steps)
                .isStepPassed(isStepPassed)
                .currentStep(currentStep)
                .build();
    }
    
    /**
     * 특정 서브섹터의 진행률 조회
     */
    public SubsectorProgressDto getSubsectorProgress(Long userId, Long subsectorId) {
        log.info("사용자 {}의 서브섹터 {} 진행률 조회", userId, subsectorId);
        
        List<UserProgress> progressList = userProgressRepository.findByUserIdAndSubsectorId(userId, subsectorId);
        
        if (progressList.isEmpty()) {
            log.warn("사용자 {}의 서브섹터 {} 진행률이 없습니다", userId, subsectorId);
            return SubsectorProgressDto.builder()
                    .subsectorId(subsectorId)
                    .completedLevels(0)
                    .completedQuizzes(0)
                    .passedQuizzes(0)
                    .overallCompletionRate(0.0)
                    .overallPassRate(0.0)
                    .levelProgress(new ArrayList<>())
                    .build();
        }
        
        // 레벨별로 그룹화
        Map<Long, List<UserProgress>> progressByLevel = progressList.stream()
                .collect(Collectors.groupingBy(p -> p.getQuiz().getLevel().getId()));
        
        // 서브섹터 정보 가져오기
        UserProgress firstProgress = progressList.get(0);
        Subsector subsector = firstProgress.getQuiz().getLevel().getSubsector();
        
        // 레벨별 진행률 계산
        List<LevelProgressDto> levelProgressList = new ArrayList<>();
        int totalCompletedQuizzes = 0;
        int totalPassedQuizzes = 0;
        
        for (Map.Entry<Long, List<UserProgress>> entry : progressByLevel.entrySet()) {
            Long levelId = entry.getKey();
            List<UserProgress> levelProgress = entry.getValue();
            
            LevelProgressDto levelProgressDto = calculateLevelProgress(levelId, levelProgress);
            levelProgressList.add(levelProgressDto);
            
            totalCompletedQuizzes += levelProgressDto.getCompletedQuizzes();
            totalPassedQuizzes += levelProgressDto.getPassedQuizzes();
        }
        
        // 전체 통계 계산
        Long totalQuizzes = userProgressRepository.countQuizzesBySubsectorId(subsectorId);
        int completedLevels = (int) progressByLevel.keySet().size();
        
        double overallCompletionRate = totalQuizzes > 0 ? (double) totalCompletedQuizzes / totalQuizzes : 0.0;
        double overallPassRate = totalCompletedQuizzes > 0 ? (double) totalPassedQuizzes / totalCompletedQuizzes : 0.0;
        
        return SubsectorProgressDto.builder()
                .subsectorId(subsectorId)
                .subsectorName(subsector.getName())
                .totalLevels(completedLevels) // 실제로는 전체 레벨 수를 조회해야 함
                .completedLevels(completedLevels)
                .totalQuizzes(totalQuizzes.intValue())
                .completedQuizzes(totalCompletedQuizzes)
                .passedQuizzes(totalPassedQuizzes)
                .overallCompletionRate(overallCompletionRate)
                .overallPassRate(overallPassRate)
                .levelProgress(levelProgressList)
                .build();
    }
    
    /**
     * 레벨별 진행률 계산 헬퍼 메서드
     */
    private LevelProgressDto calculateLevelProgress(Long levelId, List<UserProgress> progressList) {
        if (progressList.isEmpty()) {
            return LevelProgressDto.builder()
                    .levelId(levelId)
                    .completedQuizzes(0)
                    .passedQuizzes(0)
                    .failedQuizzes(0)
                    .completionRate(0.0)
                    .passRate(0.0)
                    .progressDetails(new ArrayList<>())
                    .build();
        }
        
        Level level = progressList.get(0).getQuiz().getLevel();
        Subsector subsector = level.getSubsector();
        
        int completedQuizzes = (int) progressList.stream()
                .filter(p -> p.getFinishedAt() != null)
                .count();
        
        int passedQuizzes = (int) progressList.stream()
                .filter(p -> p.getPassed() != null && p.getPassed())
                .count();
        
        int failedQuizzes = completedQuizzes - passedQuizzes;
        
        Long totalQuizzes = userProgressRepository.countQuizzesByLevelId(levelId);
        
        double completionRate = totalQuizzes > 0 ? (double) completedQuizzes / totalQuizzes : 0.0;
        double passRate = completedQuizzes > 0 ? (double) passedQuizzes / completedQuizzes : 0.0;
        
        return LevelProgressDto.builder()
                .levelId(levelId)
                .levelNumber(level.getLevelNumber())
                .levelTitle(level.getTitle())
                .subsectorId(subsector.getId())
                .subsectorName(subsector.getName())
                .learningGoal(level.getLearningGoal())
                .status(LevelProgressDto.LevelStatus.IN_PROGRESS)
                .totalQuizzes(totalQuizzes.intValue())
                .completedQuizzes(completedQuizzes)
                .passedQuizzes(passedQuizzes)
                .failedQuizzes(failedQuizzes)
                .correctAnswers(passedQuizzes)
                .remainingToPass(Math.max(0, totalQuizzes.intValue() - passedQuizzes))
                .completionRate(completionRate)
                .passRate(passRate)
                .levelPassed(passedQuizzes >= totalQuizzes.intValue())
                .progressDetails(progressList.stream()
                        .map(this::convertToDto)
                        .collect(Collectors.toList()))
                .build();
    }
    
    /**
     * UserProgress 엔티티를 DTO로 변환
     */
    private UserProgressDto convertToDto(UserProgress progress) {
        return UserProgressDto.builder()
                .id(progress.getId())
                .userId(progress.getUser().getId())
                .quizId(progress.getQuiz().getId())
                .quizTitle(progress.getQuiz().getTitle())
                .levelId(progress.getQuiz().getLevel().getId())
                .levelNumber(progress.getQuiz().getLevel().getLevelNumber())
                .levelTitle(progress.getQuiz().getLevel().getTitle())
                .subsectorId(progress.getQuiz().getLevel().getSubsector().getId())
                .subsectorName(progress.getQuiz().getLevel().getSubsector().getName())
                .startedAt(progress.getStartedAt())
                .finishedAt(progress.getFinishedAt())
                .score(progress.getScore())
                .passed(progress.getPassed())
                .teachingViews(progress.getTeachingViews())
                .createdAt(progress.getCreatedAt())
                .build();
    }
    
    /**
     * 징검다리 단계별 진행률 생성
     */
    private List<com.fintech.server.quiz.dto.StepProgressDto> createStepProgress(Long levelId, Long userId, int completedQuizzes, int passedQuizzes, Level level) {
        // 현재 4문제를 1개 단계로 처리
        boolean isCompleted = completedQuizzes == 4;
        boolean isPassed = passedQuizzes >= 3; // 75% 이상 통과 (4문제 중 3문제 이상)
        double passRate = completedQuizzes > 0 ? (double) passedQuizzes / completedQuizzes : 0.0;
        
        // 레벨 정보를 사용하여 단계 설명 생성
        String stepDescription = level.getTitle() != null ? level.getTitle() : "기초 금융 상식";
        if (level.getLearningGoal() != null && !level.getLearningGoal().trim().isEmpty()) {
            stepDescription = level.getLearningGoal();
        }
        
        com.fintech.server.quiz.dto.StepProgressDto step = com.fintech.server.quiz.dto.StepProgressDto.builder()
                .stepNumber(1)
                .stepTitle("1단계")
                .completedQuizzes(completedQuizzes)
                .totalQuizzes(4)
                .passedQuizzes(passedQuizzes)
                .failedQuizzes(completedQuizzes - passedQuizzes)
                .isCompleted(isCompleted)
                .isPassed(isPassed)
                .passRate(passRate)
                .stepDescription(stepDescription)
                .build();
        
        return java.util.Arrays.asList(step);
    }

    /**
     * 사용자의 진행률 요약 조회 (레벨별)
     */
    public List<LevelProgressDto> getUserProgressSummary(Long userId) {
        // 전체 진행률을 가져와서 레벨별로 그룹화
        List<UserProgressDto> allProgress = getUserProgress(userId);
        
        // 레벨별로 그룹화하여 요약 생성
        return allProgress.stream()
                .collect(Collectors.groupingBy(
                        UserProgressDto::getLevelId,
                        Collectors.toList()
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
                .collect(Collectors.toList());
    }

    /**
     * 현재 진행 중인 단계 계산
     */
    private Integer calculateCurrentStep(int completedQuizzes) {
        if (completedQuizzes == 0) return 1;
        if (completedQuizzes < 4) return 1;
        return 1; // 현재는 1단계만
    }
}
