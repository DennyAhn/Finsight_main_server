package com.fintech.server.quiz.repository;

import com.fintech.server.quiz.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    
    /**
     * 특정 퀴즈의 모든 문제 조회
     */
    List<Question> findByQuizId(Long quizId);
    
    /**
     * 문제와 선택지를 함께 조회 (N+1 문제 방지)
     */
    @Query("SELECT q FROM Question q LEFT JOIN FETCH q.options WHERE q.id = :questionId")
    Optional<Question> findByIdWithOptions(@Param("questionId") Long questionId);
    
    /**
     * 특정 퀴즈의 문제들과 선택지를 함께 조회
     */
    @Query("SELECT DISTINCT q FROM Question q LEFT JOIN FETCH q.options WHERE q.quiz.id = :quizId ORDER BY q.id")
    List<Question> findByQuizIdWithOptions(@Param("quizId") Long quizId);
}
