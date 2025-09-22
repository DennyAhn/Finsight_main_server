package com.fintech.server.quiz.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;

@Embeddable
@Getter
@Setter
@EqualsAndHashCode
public class UserDailyActivityId implements Serializable {
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "activity_date")
    private LocalDate activityDate;
}
