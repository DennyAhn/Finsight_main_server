package com.fintech.server.quiz.repository;

import com.fintech.server.quiz.entity.UserBadge;
import com.fintech.server.quiz.entity.Badge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {
    
    // 특정 사용자의 모든 벳지 조회
    List<UserBadge> findByUser_IdOrderByEarnedAtDesc(Long userId);
    
    // 특정 사용자의 특정 벳지 조회
    Optional<UserBadge> findByUser_IdAndBadge_Id(Long userId, Long badgeId);
    
    // 특정 사용자의 획득한 벳지들만 조회
    List<UserBadge> findByUser_IdAndIsAchievedTrueOrderByEarnedAtDesc(Long userId);
    
    // 특정 사용자의 최고 레벨 벳지 조회
    @Query("SELECT ub FROM UserBadge ub WHERE ub.user.id = :userId AND ub.isAchieved = true ORDER BY ub.badge.levelNumber DESC")
    Optional<UserBadge> findHighestAchievedBadgeByUserId(@Param("userId") Long userId);
    
    // 특정 사용자의 다음 벳지 조회
    @Query("SELECT b FROM Badge b WHERE b.levelNumber = (SELECT COALESCE(MAX(ub.badge.levelNumber), 0) + 1 FROM UserBadge ub WHERE ub.user.id = :userId AND ub.isAchieved = true)")
    Optional<Badge> findNextBadgeForUser(@Param("userId") Long userId);
    
    // 특정 사용자의 모든 벳지 삭제
    @Modifying
    @Query("DELETE FROM UserBadge ub WHERE ub.user.id = :userId")
    int deleteByUser_Id(@Param("userId") Long userId);
}
