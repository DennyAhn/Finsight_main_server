package com.fintech.server.quiz.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "questions")
@Getter
@Setter
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 퀴즈 ID
    @Column(name = "quiz_id", nullable = false)
    private Long quizId;

    // 문제 타입 (CONCEPT, STORY, ARTICLE)
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private QuestionType type;

    // DB의 'stem_md' 컬럼과 매핑
    @Column(name = "stem_md", columnDefinition = "TEXT", nullable = false)
    private String stemMd;

    // 난이도
    @Column(name = "difficulty")
    private Integer difficulty;

    // 정렬 순서
    @Column(name = "sort_order")
    private Integer sortOrder;

    // 필수 옵션 수
    @Column(name = "options_required")
    private Integer optionsRequired;

    // 최대 정답 수
    @Column(name = "max_correct")
    private Integer maxCorrect;

    // 옵션 셔플 여부
    @Column(name = "option_shuffle")
    private Boolean optionShuffle;

    // DB의 'answer_explanation_md' 컬럼과 매핑
    @Column(name = "answer_explanation_md", columnDefinition = "TEXT")
    private String answerExplanationMd;

    // 스토리 ID
    @Column(name = "story_id")
    private Long storyId;

    // 가상기사 ID
    @Column(name = "article_id")
    private Long articleId;

    // --- 학습 패널 컬럼 ---
    @Column(name = "hint_md", columnDefinition = "TEXT")
    private String hintMd;

    @Column(name = "teaching_intro_md", columnDefinition = "TEXT")
    private String teachingIntroMd;

    @Column(name = "teaching_summary_md", columnDefinition = "TEXT")
    private String teachingSummaryMd;

    @Column(name = "teaching_explainer_md", columnDefinition = "TEXT")
    private String teachingExplainerMd;

    @Column(name = "example_calc_md", columnDefinition = "TEXT")
    private String exampleCalcMd;
    
    @Column(name = "solving_keypoints_md", columnDefinition = "TEXT")
    private String solvingKeypointsMd;

    @Column(name = "teaching_illustration_url", length = 255)
    private String teachingIllustrationUrl;
    // --- --- --- --- ---

    // 생성/수정 시간
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 문제 타입 열거형
    public enum QuestionType {
        CONCEPT, STORY, ARTICLE
    }

    // Question은 Quiz에 속해있습니다 (다대일 관계)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", insertable = false, updatable = false)
    private Quiz quiz;

    // Question은 여러 개의 Option을 가집니다 (일대다 관계)
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<QuestionOption> options;
    
    // 가상기사 퀴즈를 위한 Article 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", insertable = false, updatable = false)
    private Article article;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

