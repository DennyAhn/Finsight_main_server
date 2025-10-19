package com.fintech.server.quiz.service;

import com.fintech.server.entity.User;
import com.fintech.server.quiz.dto.BadgeResponseDto;
import com.fintech.server.quiz.dto.UserBadgeResponseDto;
import com.fintech.server.quiz.dto.UserBadgeSummaryDto;
import com.fintech.server.quiz.entity.Badge;
import com.fintech.server.quiz.entity.UserBadge;
import com.fintech.server.quiz.repository.BadgeRepository;
import com.fintech.server.quiz.repository.UserBadgeRepository;
import com.fintech.server.quiz.repository.UserAnswerRepository;
import com.fintech.server.quiz.repository.UserProgressRepository;
import com.fintech.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final UserProgressRepository userProgressRepository;
    private final UserRepository userRepository;

    /**
     * 사용자의 벳지 진행 상황 업데이트 (필요시에만)
     */
    @Transactional
    public void updateUserBadgeProgressIfNeeded(Long userId) {
        // 마지막 업데이트 시간 확인 (예: 1시간마다 업데이트)
        // 여기서는 간단히 항상 업데이트하도록 구현
        updateUserBadgeProgress(userId);
    }

    /**
     * 사용자의 벳지 진행 상황 업데이트 및 displayedBadge 자동 업데이트
     */
    @Transactional
    public void updateUserBadgeProgress(Long userId) {
        List<Badge> allBadges = badgeRepository.findAllByOrderByLevelNumberAsc();
        Badge previousHighestBadge = getCurrentBadgeLevel(userId);
        
        for (Badge badge : allBadges) {
            updateBadgeProgress(userId, badge);
        }
        
        // 배지 업그레이드 확인 및 User의 displayedBadge 업데이트
        Badge currentHighestBadge = getCurrentBadgeLevel(userId);
        if (previousHighestBadge == null || 
            (currentHighestBadge != null && !currentHighestBadge.getId().equals(previousHighestBadge.getId()))) {
            updateUserDisplayedBadge(userId, currentHighestBadge);
        }
    }

    /**
     * 특정 벳지의 진행 상황 업데이트
     */
    @Transactional
    public void updateBadgeProgress(Long userId, Badge badge) {
        // 사용자의 현재 퀴즈 완료 수와 정답 수 계산
        int completedQuizzes = calculateCompletedQuizzes(userId);
        int correctAnswers = calculateCorrectAnswers(userId);
        
        // 벳지 요구사항 확인
        boolean isAchieved = completedQuizzes >= badge.getRequiredQuizzes() && 
                           correctAnswers >= badge.getRequiredCorrectAnswers();
        
        // 진행률 계산 (0-100)
        int progress = calculateProgress(completedQuizzes, correctAnswers, badge);
        
        // 기존 UserBadge 조회 또는 생성
        Optional<UserBadge> existingUserBadge = userBadgeRepository.findByUser_IdAndBadge_Id(userId, badge.getId());
        
        if (existingUserBadge.isPresent()) {
            UserBadge userBadge = existingUserBadge.get();
            userBadge.setProgress(progress);
            userBadge.setIsAchieved(isAchieved);
            if (isAchieved && !userBadge.getIsAchieved()) {
                userBadge.setEarnedAt(java.time.LocalDateTime.now());
                userBadge.setAwardedAt(java.time.LocalDateTime.now());
                userBadge.setSource("quiz_completion"); // 기본 소스 설정
                log.info("User {} earned badge: {}", userId, badge.getName());
            }
            userBadgeRepository.save(userBadge);
        } else {
            UserBadge newUserBadge = new UserBadge();
            newUserBadge.setUserId(userId);
            newUserBadge.setBadgeId(badge.getId());
            newUserBadge.setProgress(progress);
            newUserBadge.setIsAchieved(isAchieved);
            if (isAchieved) {
                newUserBadge.setEarnedAt(java.time.LocalDateTime.now());
                newUserBadge.setAwardedAt(java.time.LocalDateTime.now());
                newUserBadge.setSource("quiz_completion"); // 기본 소스 설정
                log.info("User {} earned badge: {}", userId, badge.getName());
            }
            userBadgeRepository.save(newUserBadge);
        }
    }

    /**
     * 사용자의 현재 벳지 레벨 조회
     */
    public Badge getCurrentBadgeLevel(Long userId) {
        Optional<UserBadge> highestBadge = userBadgeRepository.findHighestAchievedBadgeByUserId(userId);
        
        if (highestBadge.isPresent()) {
            return highestBadge.get().getBadge();
        }
        
        // 아무 벳지도 없으면 첫 번째 벳지 반환
        return badgeRepository.findByLevelNumber(1).orElse(null);
    }

    /**
     * 사용자의 다음 벳지 조회
     */
    public Badge getNextBadgeLevel(Long userId) {
        return userBadgeRepository.findNextBadgeForUser(userId).orElse(null);
    }

    /**
     * 사용자의 모든 벳지 조회
     */
    public List<UserBadge> getUserBadges(Long userId) {
        return userBadgeRepository.findByUser_IdOrderByEarnedAtDesc(userId);
    }

    /**
     * 사용자의 획득한 벳지들만 조회
     */
    public List<UserBadge> getAchievedBadges(Long userId) {
        return userBadgeRepository.findByUser_IdAndIsAchievedTrueOrderByEarnedAtDesc(userId);
    }

    /**
     * 완료된 퀴즈 수 계산 (UserProgress 테이블 기준)
     */
    private int calculateCompletedQuizzes(Long userId) {
        return userProgressRepository.findByUser_Id(userId).size();
    }

    /**
     * 정답 수 계산
     */
    private int calculateCorrectAnswers(Long userId) {
        return (int) userAnswerRepository.findByUserId(userId).stream()
                .filter(answer -> answer.isCorrect())
                .count();
    }

    /**
     * 벳지 진행률 계산
     */
    private int calculateProgress(int completedQuizzes, int correctAnswers, Badge badge) {
        int quizProgress = Math.min(100, (completedQuizzes * 100) / badge.getRequiredQuizzes());
        int answerProgress = Math.min(100, (correctAnswers * 100) / badge.getRequiredCorrectAnswers());
        
        // 두 조건 중 더 낮은 진행률을 반환
        return Math.min(quizProgress, answerProgress);
    }

    /**
     * 벳지 개수 조회
     */
    public long getBadgeCount() {
        return badgeRepository.count();
    }

    /**
     * 벳지 데이터 초기화 (개발용)
     */
    @Transactional
    public void initializeBadges() {
        if (badgeRepository.count() > 0) {
            return; // 이미 초기화됨
        }

        Badge bronze = new Badge();
        bronze.setCode("BRONZE");
        bronze.setName("브론즈");
        bronze.setDescription("첫 번째 벳지입니다. 3개의 퀴즈를 완료하고 5개의 문제를 맞춰보세요!");
        bronze.setIconUrl("/icons/bronze.png");
        bronze.setLevelNumber(1);
        bronze.setRequiredQuizzes(3);
        bronze.setRequiredCorrectAnswers(5);
        badgeRepository.save(bronze);

        Badge silver = new Badge();
        silver.setCode("SILVER");
        silver.setName("실버");
        silver.setDescription("두 번째 벳지입니다. 3개의 문제를 맞춰보세요!");
        silver.setIconUrl("/icons/silver.png");
        silver.setLevelNumber(2);
        silver.setRequiredQuizzes(3);
        silver.setRequiredCorrectAnswers(3);
        badgeRepository.save(silver);

        Badge gold = new Badge();
        gold.setCode("GOLD");
        gold.setName("골드");
        gold.setDescription("세 번째 벳지입니다. 12개의 퀴즈를 완료하고 25개의 문제를 맞춰보세요!");
        gold.setIconUrl("/icons/gold.png");
        gold.setLevelNumber(3);
        gold.setRequiredQuizzes(12);
        gold.setRequiredCorrectAnswers(25);
        badgeRepository.save(gold);

        Badge platinum = new Badge();
        platinum.setCode("PLATINUM");
        platinum.setName("플레티넘");
        platinum.setDescription("네 번째 벳지입니다. 20개의 퀴즈를 완료하고 45개의 문제를 맞춰보세요!");
        platinum.setIconUrl("/icons/platinum.png");
        platinum.setLevelNumber(4);
        platinum.setRequiredQuizzes(20);
        platinum.setRequiredCorrectAnswers(45);
        badgeRepository.save(platinum);

        Badge diamond = new Badge();
        diamond.setCode("DIAMOND");
        diamond.setName("다이아");
        diamond.setDescription("다섯 번째 벳지입니다. 30개의 퀴즈를 완료하고 70개의 문제를 맞춰보세요!");
        diamond.setIconUrl("/icons/diamond.png");
        diamond.setLevelNumber(5);
        diamond.setRequiredQuizzes(30);
        diamond.setRequiredCorrectAnswers(70);
        badgeRepository.save(diamond);

        Badge master = new Badge();
        master.setCode("MASTER");
        master.setName("마스터");
        master.setDescription("최고 레벨 벳지입니다. 50개의 퀴즈를 완료하고 120개의 문제를 맞춰보세요!");
        master.setIconUrl("/icons/master.png");
        master.setLevelNumber(6);
        master.setRequiredQuizzes(50);
        master.setRequiredCorrectAnswers(120);
        badgeRepository.save(master);

        log.info("Badge system initialized with 6 levels");
    }

    /**
     * User의 displayedBadge 업데이트
     */
    @Transactional
    public void updateUserDisplayedBadge(Long userId, Badge newBadge) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setDisplayedBadge(newBadge);
            userRepository.save(user);
            log.info("User {} displayedBadge updated to: {}", userId, newBadge != null ? newBadge.getName() : "null");
        }
    }

    // ========== 새로운 API 메서드들 ==========

    /**
     * 사용자 뱃지 요약 정보 조회
     */
    public UserBadgeSummaryDto getUserBadgeSummary(Long userId) {
        // 뱃지 데이터 초기화 (개발용)
        initializeBadges();
        
        // 사용자의 뱃지 진행 상황 업데이트
        updateUserBadgeProgress(userId);
        
        // 현재 뱃지
        Badge currentBadge = getCurrentBadgeLevel(userId);
        BadgeResponseDto currentBadgeResponse = BadgeResponseDto.from(currentBadge);
        
        // 다음 뱃지
        Badge nextBadge = getNextBadgeLevel(userId);
        BadgeResponseDto nextBadgeResponse = BadgeResponseDto.from(nextBadge);
        
        // 모든 뱃지 목록
        List<UserBadge> allUserBadges = getUserBadges(userId);
        List<UserBadgeResponseDto> allBadgeResponses = allUserBadges.stream()
                .map(UserBadgeResponseDto::from)
                .collect(Collectors.toList());
        
        // 획득한 뱃지 목록
        List<UserBadge> achievedBadges = getAchievedBadges(userId);
        List<UserBadgeResponseDto> achievedBadgeResponses = achievedBadges.stream()
                .map(UserBadgeResponseDto::from)
                .collect(Collectors.toList());
        
        return UserBadgeSummaryDto.of(currentBadgeResponse, nextBadgeResponse, allBadgeResponses, achievedBadgeResponses);
    }

    /**
     * 사용자 현재 뱃지 조회 (ResponseDto)
     */
    public BadgeResponseDto getCurrentBadgeResponse(Long userId) {
        Badge currentBadge = getCurrentBadgeLevel(userId);
        return BadgeResponseDto.from(currentBadge);
    }

    /**
     * 사용자 획득한 뱃지 목록 조회 (ResponseDto)
     */
    public List<UserBadgeResponseDto> getAchievedBadgeResponses(Long userId) {
        List<UserBadge> achievedBadges = getAchievedBadges(userId);
        return achievedBadges.stream()
                .map(UserBadgeResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 사용자 모든 뱃지 목록 조회 (ResponseDto)
     */
    public List<UserBadgeResponseDto> getAllUserBadgeResponses(Long userId) {
        List<UserBadge> allUserBadges = getUserBadges(userId);
        return allUserBadges.stream()
                .map(UserBadgeResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 모든 뱃지 목록 조회 (ResponseDto)
     */
    public List<BadgeResponseDto> getAllBadgeResponses() {
        List<Badge> allBadges = badgeRepository.findAllByOrderByLevelNumberAsc();
        return allBadges.stream()
                .map(BadgeResponseDto::from)
                .collect(Collectors.toList());
    }
}
