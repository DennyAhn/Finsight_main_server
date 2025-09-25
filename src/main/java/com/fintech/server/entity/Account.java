package com.fintech.server.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // User 엔티티와 1:1 관계를 맺습니다. unique = true가 핵심입니다.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, unique = true)
    private String email;

    // DB 스키마와 일치하도록 email_normalized 필드 추가
    @Column(name = "email_normalized", nullable = false, unique = true)
    private String emailNormalized;

    @Column(name = "password_hash")
    private String password;

    @Column(name = "is_email_verified")
    private boolean isEmailVerified = false;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    // DB의 ENUM 타입과 매핑
    @Enumerated(EnumType.STRING)
    private AccountStatus status = AccountStatus.active;

    // DB의 ENUM 타입에 해당하는 Java Enum
    public enum AccountStatus {
        active, disabled, deleted
    }
}

