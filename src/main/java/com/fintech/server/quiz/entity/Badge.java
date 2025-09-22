package com.fintech.server.quiz.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "badges")
@Getter
@Setter
public class Badge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "icon_url")
    private String iconUrl;

    @Column(name = "criteria_json", columnDefinition = "TEXT")
    private String criteriaJson;

    @Column(name = "level_number", nullable = false)
    private Integer levelNumber; // 1=브론즈, 2=실버, 3=골드, 4=플레티넘, 5=다이아, 6=마스터

    @Column(name = "required_quizzes", nullable = false)
    private Integer requiredQuizzes; // 벳지를 얻기 위해 필요한 퀴즈 수

    @Column(name = "required_correct_answers", nullable = false)
    private Integer requiredCorrectAnswers; // 벳지를 얻기 위해 필요한 정답 수

    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt;

    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
        updatedAt = java.time.LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = java.time.LocalDateTime.now();
    }

    // 벳지 레벨 열거형
    public enum BadgeLevel {
        BRONZE(1, "브론즈", "#CD7F32"),
        SILVER(2, "실버", "#C0C0C0"),
        GOLD(3, "골드", "#FFD700"),
        PLATINUM(4, "플레티넘", "#E5E4E2"),
        DIAMOND(5, "다이아", "#B9F2FF"),
        MASTER(6, "마스터", "#800080");

        private final int levelNumber;
        private final String name;
        private final String color;

        BadgeLevel(int levelNumber, String name, String color) {
            this.levelNumber = levelNumber;
            this.name = name;
            this.color = color;
        }

        public int getLevelNumber() { return levelNumber; }
        public String getName() { return name; }
        public String getColor() { return color; }

        public static BadgeLevel fromLevelNumber(int levelNumber) {
            for (BadgeLevel level : values()) {
                if (level.levelNumber == levelNumber) {
                    return level;
                }
            }
            return BRONZE;
        }
    }
}
