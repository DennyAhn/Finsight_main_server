package com.fintech.server.quiz.entity;

import com.fintech.server.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_wrong_notes")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class UserWrongNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_answer_option_id")
    private QuestionOption lastAnswerOption;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "correct_option_id")
    private QuestionOption correctOption;

    @Column(name = "times_wrong", nullable = false)
    private Integer timesWrong = 1;

    @Column(name = "first_wrong_at", nullable = false)
    private LocalDateTime firstWrongAt;

    @Column(name = "last_wrong_at", nullable = false)
    private LocalDateTime lastWrongAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "resolved", nullable = false)
    private Boolean resolved = false;


    @Column(name = "personal_note_md", columnDefinition = "TEXT")
    private String personalNoteMd;

    @Column(name = "snapshot_teaching_summary_md", columnDefinition = "TEXT")
    private String snapshotTeachingSummaryMd;

    @Column(name = "snapshot_teaching_explainer_md", columnDefinition = "TEXT")
    private String snapshotTeachingExplainerMd;

    @Column(name = "snapshot_keypoints_md", columnDefinition = "TEXT")
    private String snapshotKeypointsMd;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 편의 메서드
    public void incrementTimesWrong() {
        this.timesWrong++;
        this.lastWrongAt = LocalDateTime.now();
    }

    public void markAsReviewed() {
        this.reviewedAt = LocalDateTime.now();
    }

    public void markAsResolved() {
        this.resolved = true;
        this.reviewedAt = LocalDateTime.now();
    }

}
