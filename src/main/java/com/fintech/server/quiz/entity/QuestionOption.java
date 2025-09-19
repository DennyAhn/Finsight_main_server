package com.fintech.server.quiz.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "question_options")
@Getter
@Setter
public class QuestionOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Option은 Question에 속해있습니다 (다대일 관계)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private Question question;

    @Column(nullable = false)
    private String label;

    @Column(name = "content_md", columnDefinition = "TEXT", nullable = false)
    private String contentMd;

    @Column(name = "is_correct", nullable = false)
    private boolean isCorrect;

    // ▼▼▼ 누락되었던 컬럼 추가 ▼▼▼
    @Column(name = "feedback_md", columnDefinition = "TEXT")
    private String feedbackMd;

    @Column(name = "sort_order")
    private Integer sortOrder;
    // ▲▲▲ 누락되었던 컬럼 추가 ▲▲▲
}
