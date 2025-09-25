package com.fintech.server.quiz.repository;

import com.fintech.server.quiz.entity.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {
    // 특정 사용자의 특정 퀴즈 목록에 대한 진행 상황을 조회합니다.
    List<UserProgress> findByUserIdAndQuizIdIn(Long userId, Set<Long> quizIds);
    
    // 특정 사용자의 진행 기록 삭제
    void deleteByUserId(Long userId);
}
