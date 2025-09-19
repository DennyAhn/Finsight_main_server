package com.fintech.server.quiz.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * 퀴즈 정보를 저장하는 JPA 엔티티
 * 
 * 📌 프론트엔드 개발자를 위한 데이터베이스 구조 설명:
 * 
 * 🗄️ 테이블: quizzes
 * 
 * 📊 데이터 구조:
 * - id: 퀴즈 고유 식별자 (Primary Key, Auto Increment)
 * - title: 퀴즈 제목 (최대 255자)
 * - questions: 이 퀴즈에 속한 문제들 (1:N 관계)
 * 
 * 🔗 관계:
 * - Quiz (1) ← → (N) Question: 하나의 퀴즈는 여러 문제를 가질 수 있음
 * 
 * 💡 프론트엔드 관점에서의 의미:
 * - 하나의 Quiz는 여러 개의 Question을 포함하는 컨테이너 역할
 * - API 응답 시 QuizResponseDto로 변환되어 전달됨
 * 
 * ⚡ 성능 고려사항:
 * - questions는 LAZY 로딩으로 설정 (필요할 때만 조회)
 * - Repository의 fetch join으로 N+1 문제 해결
 */
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
    
    /**
     * 이 퀴즈에 속한 문제들의 목록
     * 
     * 🔗 관계 설정:
     * - @OneToMany: Quiz(1) → Question(N) 관계
     * - mappedBy = "quiz": Question 엔티티의 quiz 필드가 외래키 관리
     * - FetchType.LAZY: 필요할 때만 문제들을 조회 (성능 최적화)
     * 
     * 📤 API 응답:
     * - QuizResponseDto.questions 배열로 변환되어 전달
     * - 각 Question은 QuestionDto로 변환됨
     */
    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Question> questions;
}
