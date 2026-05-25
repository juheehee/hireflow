package com.hireflow.hireflow.domain.match;

import com.hireflow.hireflow.domain.jobposting.JobPosting;
import com.hireflow.hireflow.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "match",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "job_posting_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_posting_id", nullable = false)
    private JobPosting jobPosting;

    @Column(nullable = false)
    private int score;

    @Column(name = "recommendation_reason")
    private String recommendationReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static Match create(User user, JobPosting jobPosting, int score, String reason) {
        Match match = new Match();
        match.user = user;
        match.jobPosting = jobPosting;
        match.score = score;
        match.recommendationReason = reason;
        match.createdAt = LocalDateTime.now();
        return match;
    }

    public void update(int score, String reason) {
        this.score = score;
        this.recommendationReason = reason;
    }
}
