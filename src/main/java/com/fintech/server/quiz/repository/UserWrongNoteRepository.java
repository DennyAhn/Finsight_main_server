package com.fintech.server.quiz.repository;

import com.fintech.server.quiz.entity.UserWrongNote;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserWrongNoteRepository extends JpaRepository<UserWrongNote, Long> {
    
    /**
     * 특정 사용자의 모든 오답 노트 조회 (최신순)
     */
    @Query("SELECT uwn FROM UserWrongNote uwn WHERE uwn.user.id = :userId ORDER BY uwn.lastWrongAt DESC")
    List<UserWrongNote> findByUserIdOrderByLastWrongAtDesc(@Param("userId") Long userId);
    
    /**
     * 특정 사용자의 특정 문제에 대한 오답 노트 조회
     */
    @Query("SELECT uwn FROM UserWrongNote uwn WHERE uwn.user.id = :userId AND uwn.question.id = :questionId")
    Optional<UserWrongNote> findByUserIdAndQuestionId(@Param("userId") Long userId, @Param("questionId") Long questionId);
    
    /**
     * 특정 사용자의 미해결 오답 노트 조회
     */
    @Query("SELECT uwn FROM UserWrongNote uwn WHERE uwn.user.id = :userId AND uwn.resolved = false ORDER BY uwn.lastWrongAt DESC")
    List<UserWrongNote> findUnresolvedByUserId(@Param("userId") Long userId);
    
    
    /**
     * 특정 사용자의 해결된 오답 노트 조회 (복습용)
     */
    @Query("SELECT uwn FROM UserWrongNote uwn WHERE uwn.user.id = :userId AND uwn.resolved = true ORDER BY uwn.lastWrongAt DESC")
    List<UserWrongNote> findResolvedByUserId(@Param("userId") Long userId);
    
    /**
     * 특정 사용자의 복습이 필요한 오답 노트 조회 (1회 이상 틀린 문제)
     */
    @Query("SELECT uwn FROM UserWrongNote uwn WHERE uwn.user.id = :userId AND uwn.timesWrong >= 1 AND uwn.resolved = false ORDER BY uwn.timesWrong DESC, uwn.lastWrongAt DESC")
    List<UserWrongNote> findNeedReviewByUserId(@Param("userId") Long userId);
    
    /**
     * 특정 사용자의 오답 노트 통계
     */
    @Query("SELECT COUNT(uwn) FROM UserWrongNote uwn WHERE uwn.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(uwn) FROM UserWrongNote uwn WHERE uwn.user.id = :userId AND uwn.resolved = false")
    Long countUnresolvedByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(uwn) FROM UserWrongNote uwn WHERE uwn.user.id = :userId AND uwn.resolved = true")
    Long countResolvedByUserId(@Param("userId") Long userId);
    
    /**
     * 특정 사용자의 오답 노트 삭제 (게스트 정리용)
     */
    void deleteByUserId(Long userId);
    
    // ===== 관리자용 통계 쿼리 =====
    
    /**
     * 섹터별 오답 노트 통계
     */
    @Query("SELECT s.id, s.name, s.slug, COUNT(uwn) as wrongCount, COUNT(DISTINCT uwn.user.id) as uniqueUsers " +
           "FROM UserWrongNote uwn " +
           "JOIN uwn.question q " +
           "JOIN q.quiz quiz " +
           "JOIN quiz.level l " +
           "JOIN l.subsector ss " +
           "JOIN ss.sector s " +
           "GROUP BY s.id, s.name, s.slug " +
           "ORDER BY wrongCount DESC")
    List<Object[]> findWrongNoteStatisticsBySector();
    
    /**
     * 특정 섹터의 서브섹터별 오답 노트 통계
     */
    @Query("SELECT ss.id, ss.name, ss.slug, COUNT(uwn) as wrongCount, COUNT(DISTINCT uwn.user.id) as uniqueUsers " +
           "FROM UserWrongNote uwn " +
           "JOIN uwn.question q " +
           "JOIN q.quiz quiz " +
           "JOIN quiz.level l " +
           "JOIN l.subsector ss " +
           "WHERE ss.sector.id = :sectorId " +
           "GROUP BY ss.id, ss.name, ss.slug " +
           "ORDER BY wrongCount DESC")
    List<Object[]> findWrongNoteStatisticsBySubsector(@Param("sectorId") Long sectorId);
    
    /**
     * 특정 서브섹터의 레벨별 오답 노트 통계
     */
    @Query("SELECT l.id, l.levelNumber, l.title, COUNT(uwn) as wrongCount, COUNT(DISTINCT uwn.user.id) as uniqueUsers " +
           "FROM UserWrongNote uwn " +
           "JOIN uwn.question q " +
           "JOIN q.quiz quiz " +
           "JOIN quiz.level l " +
           "WHERE l.subsector.id = :subsectorId " +
           "GROUP BY l.id, l.levelNumber, l.title " +
           "ORDER BY l.levelNumber ASC")
    List<Object[]> findWrongNoteStatisticsByLevel(@Param("subsectorId") Long subsectorId);
    
    /**
     * 특정 레벨의 퀴즈별 오답 노트 통계
     */
    @Query("SELECT quiz.id, quiz.title, COUNT(uwn) as wrongCount, COUNT(DISTINCT uwn.user.id) as uniqueUsers " +
           "FROM UserWrongNote uwn " +
           "JOIN uwn.question q " +
           "JOIN q.quiz quiz " +
           "WHERE quiz.level.id = :levelId " +
           "GROUP BY quiz.id, quiz.title " +
           "ORDER BY wrongCount DESC")
    List<Object[]> findWrongNoteStatisticsByQuiz(@Param("levelId") Long levelId);
    
    /**
     * 특정 퀴즈의 문제별 오답 노트 통계
     */
    @Query("SELECT q.id, q.stemMd, COUNT(uwn) as wrongCount, COUNT(DISTINCT uwn.user.id) as uniqueUsers " +
           "FROM UserWrongNote uwn " +
           "JOIN uwn.question q " +
           "WHERE q.quiz.id = :quizId " +
           "GROUP BY q.id, q.stemMd " +
           "ORDER BY wrongCount DESC")
    List<Object[]> findWrongNoteStatisticsByQuestion(@Param("quizId") Long quizId);
    
    /**
     * 가장 많이 틀린 문제 TOP N
     */
    @Query("SELECT q.id, q.stemMd, quiz.title, ss.sector.name, ss.name, COUNT(uwn) as wrongCount " +
           "FROM UserWrongNote uwn " +
           "JOIN uwn.question q " +
           "JOIN q.quiz quiz " +
           "JOIN quiz.level l " +
           "JOIN l.subsector ss " +
           "GROUP BY q.id, q.stemMd, quiz.title, ss.sector.name, ss.name " +
           "ORDER BY wrongCount DESC")
    List<Object[]> findTopWrongQuestions(Pageable pageable);
    
    /**
     * 특정 문제의 오답 노트 상세 정보 (통계용)
     */
    @Query("SELECT uwn FROM UserWrongNote uwn " +
           "WHERE uwn.question.id = :questionId " +
           "ORDER BY uwn.lastWrongAt DESC")
    List<UserWrongNote> findByQuestionId(@Param("questionId") Long questionId, Pageable pageable);
}
