package com.fintech.server.quiz.repository;

import com.fintech.server.quiz.entity.Level;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LevelRepository extends JpaRepository<Level, Long> {
    @Query("SELECT l FROM Level l LEFT JOIN FETCH l.quizzes WHERE l.id = :levelId")
    Optional<Level> findByIdWithQuizzes(@Param("levelId") Long levelId);
    
    @Query("SELECT l FROM Level l JOIN FETCH l.subsector WHERE l.id = :levelId")
    Optional<Level> findByIdWithSubsector(@Param("levelId") Long levelId);
}
