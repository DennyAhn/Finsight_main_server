package com.fintech.server.quiz.repository;

import com.fintech.server.quiz.entity.Badge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BadgeRepository extends JpaRepository<Badge, Long> {
    
    // 레벨 번호로 벳지 조회
    Optional<Badge> findByLevelNumber(Integer levelNumber);
    
    // 모든 벳지를 레벨 순으로 조회
    List<Badge> findAllByOrderByLevelNumberAsc();
    
    // 특정 레벨 이하의 벳지들 조회
    List<Badge> findByLevelNumberLessThanEqualOrderByLevelNumberAsc(Integer levelNumber);
    
    // 코드로 벳지 조회
    Optional<Badge> findByCode(String code);
}
