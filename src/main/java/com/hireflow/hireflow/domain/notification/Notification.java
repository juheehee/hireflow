package com.hireflow.hireflow.domain.notification;

import com.hireflow.hireflow.domain.application.Application;
import com.hireflow.hireflow.domain.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type; // DEADLINE_REMINDER / INTERVIEW_REMINDER / PARSE_COMPLETED

    @Lob
    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private boolean isRead; // DEFAULT false

    private LocalDateTime sentAt;    // 발송 전 null, 발송 후 시각

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;
}
