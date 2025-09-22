package com.fintech.server.quiz.repository; // 패키지 경로를 quiz.repository로 변경

import com.fintech.server.quiz.entity.UserAnswer; // import 경로 변경
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserAnswerRepository extends JpaRepository<UserAnswer, Long> {
    
    /**
     * 특정 사용자의 특정 퀴즈에 대한 답변 조회
     */
    @Query("SELECT ua FROM UserAnswer ua WHERE ua.user.id = :userId AND ua.question.quiz.id = :quizId")
    List<UserAnswer> findByUserIdAndQuizId(@Param("userId") Long userId, @Param("quizId") Long quizId);
}
