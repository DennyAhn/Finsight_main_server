package com.fintech.server.quiz.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Entity
@Table(name = "questions")
@Getter
@Setter
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // DB의 'stem_md' 컬럼과 매핑
    @Column(name = "stem_md", columnDefinition = "TEXT", nullable = false)
    private String stemMd;

    // DB의 'answer_explanation_md' 컬럼과 매핑 (이 부분이 누락되었습니다!)
    @Column(name = "answer_explanation_md", columnDefinition = "TEXT")
    private String answerExplanationMd;

    // --- 학습 패널 컬럼 ---
    @Column(name = "hint_md", columnDefinition = "TEXT")
    private String hintMd;

    @Column(name = "teaching_explainer_md", columnDefinition = "TEXT")
    private String teachingExplainerMd;
    
    @Column(name = "solving_keypoints_md", columnDefinition = "TEXT")
    private String solvingKeypointsMd;
    // --- --- --- --- ---

    // Question은 Quiz에 속해있습니다 (다대일 관계)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    // Question은 여러 개의 Option을 가집니다 (일대다 관계)
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<QuestionOption> options;
    
    // (difficulty 등 다른 컬럼들도 필요에 따라 여기에 추가할 수 있습니다)
}

