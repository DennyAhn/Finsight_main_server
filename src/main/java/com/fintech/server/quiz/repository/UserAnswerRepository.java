package com.fintech.server.quiz.repository; // 패키지 경로를 quiz.repository로 변경

import com.fintech.server.quiz.entity.UserAnswer; // import 경로 변경
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAnswerRepository extends JpaRepository<UserAnswer, Long> {
}
