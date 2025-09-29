package com.fintech.server.entity;

import com.fintech.server.quiz.entity.Badge;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false) // 닉네임은 고유하고 필수입니다.
    private String nickname;

    @Column(nullable = false) // 연락처용 이메일 (필수)
    private String email;

    @Column(name = "is_guest", nullable = false)
    private Boolean isGuest = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // 현재 표시 중인 대표 배지 (nullable)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "displayed_badge_id")
    private Badge displayedBadge;

    // avatar_url, age_group 등 다른 프로필 컬럼들도 여기에 추가할 수 있습니다.
    
    // Account와 1:1 관계를 맺습니다. (User가 주인이 아님)
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Account account;
}
