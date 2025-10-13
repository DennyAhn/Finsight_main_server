package com.fintech.server.quiz.entity;

import com.fintech.server.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_badges")
@Getter
@Setter
public class UserBadge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "badge_id", nullable = false)
    private Badge badge;

    // 편의 메서드들
    public void setUserId(Long userId) {
        if (this.user == null) {
            this.user = new User();
        }
        this.user.setId(userId);
    }

    public void setBadgeId(Long badgeId) {
        if (this.badge == null) {
            this.badge = new Badge();
        }
        this.badge.setId(badgeId);
    }

    @Column(name = "earned_at", nullable = false)
    private java.time.LocalDateTime earnedAt;

    @Column(name = "progress", nullable = false)
    private Integer progress; // 현재 진행률 (0-100)

    @Column(name = "is_achieved", nullable = false)
    private Boolean isAchieved = false; // 벳지 획득 여부

    @Column(name = "awarded_at")
    private java.time.LocalDateTime awardedAt; // 뱃지가 실제로 수여된 시간

    @Column(name = "source")
    private String source; // 뱃지 획득 소스 (예: "quiz_completion", "level_achievement")

    @Column(name = "meta_json", columnDefinition = "TEXT")
    private String metaJson; // 추가 메타데이터 (JSON 형태)

    @PrePersist
    protected void onCreate() {
        earnedAt = java.time.LocalDateTime.now();
    }
}
