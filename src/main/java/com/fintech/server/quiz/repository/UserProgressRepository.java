package com.fintech.server.quiz.repository;

import com.fintech.server.quiz.entity.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {
    
    /**
     * 특정 사용자의 모든 진행률 조회
     */
    List<UserProgress> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    /**
     * 특정 사용자의 특정 레벨 진행률 조회
     */
    @Query("SELECT up FROM UserProgress up " +
           "JOIN up.quiz q " +
           "JOIN q.level l " +
           "WHERE up.user.id = :userId AND l.id = :levelId " +
           "ORDER BY up.createdAt DESC")
    List<UserProgress> findByUserIdAndLevelId(@Param("userId") Long userId, @Param("levelId") Long levelId);
    
    /**
     * 특정 사용자의 특정 서브섹터 진행률 조회
     */
    @Query("SELECT up FROM UserProgress up " +
           "JOIN up.quiz q " +
           "JOIN q.level l " +
           "JOIN l.subsector s " +
           "WHERE up.user.id = :userId AND s.id = :subsectorId " +
           "ORDER BY l.levelNumber ASC, up.createdAt DESC")
    List<UserProgress> findByUserIdAndSubsectorId(@Param("userId") Long userId, @Param("subsectorId") Long subsectorId);
    
    /**
     * 특정 사용자의 완료한 퀴즈 수 (레벨별)
     */
    @Query("SELECT l.id, l.levelNumber, l.title, COUNT(up.id) " +
           "FROM UserProgress up " +
           "JOIN up.quiz q " +
           "JOIN q.level l " +
           "WHERE up.user.id = :userId AND up.finishedAt IS NOT NULL " +
           "GROUP BY l.id, l.levelNumber, l.title " +
           "ORDER BY l.levelNumber ASC")
    List<Object[]> findCompletedQuizzesByLevel(@Param("userId") Long userId);
    
    /**
     * 특정 사용자의 통과한 퀴즈 수 (레벨별)
     */
    @Query("SELECT l.id, l.levelNumber, l.title, COUNT(up.id) " +
           "FROM UserProgress up " +
           "JOIN up.quiz q " +
           "JOIN q.level l " +
           "WHERE up.user.id = :userId AND up.passed = true " +
           "GROUP BY l.id, l.levelNumber, l.title " +
           "ORDER BY l.levelNumber ASC")
    List<Object[]> findPassedQuizzesByLevel(@Param("userId") Long userId);
    
    /**
     * 특정 레벨의 전체 퀴즈 수
     */
    @Query("SELECT COUNT(q.id) FROM Quiz q WHERE q.level.id = :levelId")
    Long countQuizzesByLevelId(@Param("levelId") Long levelId);
    
    /**
     * 특정 서브섹터의 전체 퀴즈 수
     */
    @Query("SELECT COUNT(q.id) FROM Quiz q " +
           "JOIN q.level l " +
           "WHERE l.subsector.id = :subsectorId")
    Long countQuizzesBySubsectorId(@Param("subsectorId") Long subsectorId);
    
    /**
     * 특정 사용자의 진행률 조회 (간단한 버전)
     */
    List<UserProgress> findByUser_Id(Long userId);
    
    /**
     * 특정 사용자의 진행률 삭제
     */
    void deleteByUserId(Long userId);
    
    /**
     * 징검다리용 메서드 추가
     */
    @Query("SELECT COUNT(DISTINCT up.quiz.id) FROM UserProgress up WHERE up.user.id = :userId AND up.quiz.level.id = :levelId")
    Long countCompletedQuizzesByUserIdAndLevelId(@Param("userId") Long userId, @Param("levelId") Long levelId);
    
    @Query("SELECT COUNT(DISTINCT up.quiz.id) FROM UserProgress up WHERE up.user.id = :userId AND up.quiz.level.id = :levelId AND up.passed = true")
    Long countPassedQuizzesByUserIdAndLevelId(@Param("userId") Long userId, @Param("levelId") Long levelId);
}