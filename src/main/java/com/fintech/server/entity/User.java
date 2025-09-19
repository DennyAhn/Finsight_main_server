package com.fintech.server.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    // avatar_url, age_group 등 다른 프로필 컬럼들도 여기에 추가할 수 있습니다.
    
    // Account와 1:1 관계를 맺습니다. (User가 주인이 아님)
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Account account;
}
