package com.fintech.server.quiz.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@Entity
@Table(name = "quizzes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 기본 생성자 (외부에서 직접 생성 불가)

public class Quiz {

    /**
     * 퀴즈의 고유 식별자
     * - 데이터베이스에서 자동 생성 (Auto Increment)
     * - API 응답에서 quiz.id로 전달됨
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 퀴즈 제목
     * - 최대 255자까지 저장 가능
     * - 필수 입력 값 (nullable = false)
     * - API 응답에서 quiz.title로 전달됨
     */
    @Column(length = 255, nullable = false)
    private String title;

    // ▼▼▼ 이 부분이 바로 "quizzes 테이블을 levels 테이블에 연결하는" 논리적 연결고리입니다. ▼▼▼
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "level_id", nullable = false)
    private Level level;
// ▲▲▲ ▲▲▲
    
    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Question> questions;
}
