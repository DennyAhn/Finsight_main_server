package com.fintech.server.quiz.entity;

import com.fintech.server.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_daily_activity")
@Getter
@Setter
public class UserDailyActivity {
    // user_id와 activity_date를 복합 기본 키로 사용합니다.
    @EmbeddedId
    private UserDailyActivityId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId") // UserDailyActivityId의 userId 필드에 매핑
    @JoinColumn(name = "user_id")
    private User user;
}