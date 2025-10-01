package com.fintech.server.quiz.repository;

import com.fintech.server.quiz.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    /**
     * 특정 ID의 퀴즈를 조회할 때, 관련된 Question과 QuestionOption을
     * N+1 문제 없이 한 번의 쿼리로 모두 가져옵니다. (JOIN FETCH)
     * * @param quizId 조회할 퀴즈의 ID
     * @return 퀴즈와 모든 하위 정보가 포함된 Optional<Quiz>
     */
    @Query("SELECT DISTINCT q FROM Quiz q " +
           "LEFT JOIN FETCH q.questions qs " +
           "LEFT JOIN FETCH qs.options " +
           "LEFT JOIN FETCH qs.article " +
           "WHERE q.id = :quizId")
    Optional<Quiz> findByIdWithDetails(@Param("quizId") Long quizId);

    /**
     * 특정 레벨의 모든 퀴즈를 ID 순서대로 조회
     */
    List<Quiz> findByLevelIdOrderById(Long levelId);
}

/**
 * 퀴즈 데이터 접근을 위한 JPA Repository 인터페이스
 * 
 * 📌 프론트엔드 개발자를 위한 Repository 설명:
 * 
 * 🎯 역할:
 * - 퀴즈 관련 데이터베이스 쿼리 처리
 * - JPA 기본 CRUD 기능 제공 (상속받음)
 * - 커스텀 쿼리 메소드 정의
 * - 성능 최적화된 조회 기능
 * 
 * 🚀 성능 최적화:
 * - Fetch Join을 사용하여 N+1 문제 해결
 * - 단일 쿼리로 모든 관련 데이터 조회
 * - 프론트엔드 응답 속도 향상
 * 
 * 💡 데이터 흐름:
 * Controller → Service → Repository → Database
 *                              ↓
 *                         Entity 반환
 *                              ↓
 *                      Service에서 DTO 변환
 *                              ↓
 *                      Controller에서 JSON 응답
 */