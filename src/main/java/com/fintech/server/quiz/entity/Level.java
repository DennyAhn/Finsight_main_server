package com.fintech.server.quiz.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.Set;

@Entity
@Table(name = "levels")
@Getter
@Setter
public class Level {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // DB의 'subsector_id' 컬럼과 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subsector_id", nullable = false)
    private Subsector subsector;

    @Column(name = "level_number", nullable = false)
    private Integer levelNumber;

    @Column
    private String title;

    @Column(name = "learning_goal", columnDefinition = "TEXT")
    private String learningGoal;

    // ▼▼▼ 누락되었던 badge_id 컬럼 추가 ▼▼▼
    @Column(name = "badge_id")
    private Long badgeId;
    // ▲▲▲ 누락되었던 badge_id 컬럼 추가 ▲▲▲
    
    @OneToMany(mappedBy = "level", fetch = FetchType.LAZY)
    private Set<Quiz> quizzes;
}

