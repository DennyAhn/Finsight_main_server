package com.fintech.server.quiz.repository;

import com.fintech.server.quiz.entity.UserDailyActivity;
import com.fintech.server.quiz.entity.UserDailyActivityId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserDailyActivityRepository extends JpaRepository<UserDailyActivity, UserDailyActivityId> {
    // 특정 사용자의 특정 기간 동안의 활동 기록을 조회합니다.
    List<UserDailyActivity> findByIdUserIdAndIdActivityDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
    
    // 특정 사용자의 특정 날짜 활동 기록을 조회합니다.
    Optional<UserDailyActivity> findByIdUserIdAndIdActivityDate(Long userId, LocalDate activityDate);
    
    // 특정 사용자의 활동 기록을 ID 내림차순으로 조회
    List<UserDailyActivity> findByIdUserIdOrderByIdActivityDateDesc(Long userId);
    
    // 특정 사용자의 활동 기록 삭제
    void deleteByIdUserId(Long userId);
}