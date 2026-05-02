package com.hireflow.hireflow.domain.application;

import com.hireflow.hireflow.domain.jobposting.JobPosting;
import com.hireflow.hireflow.domain.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "applications")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String status; // APPLIED/DOC_PASS/INTERVIEW_1/INTERVIEW_2/FINAL_PASS/REJECTED

    @Column(columnDefinition = "TEXT")
    private String memo;            // 커피챗, 과제전형 등 자유 기록

    @Column(nullable = false)
    private LocalDate appliedAt;

    private LocalDate interviewDate; // 면접 날짜, D-1 알림용

    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_posting_id", nullable = false)
    private JobPosting jobPosting;

    public void updateStatus(String status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateInterviewDate(LocalDate interviewDate) {
        this.interviewDate = interviewDate;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateMemo(String memo) {
        this.memo = memo;
        this.updatedAt = LocalDateTime.now();
    }
}
