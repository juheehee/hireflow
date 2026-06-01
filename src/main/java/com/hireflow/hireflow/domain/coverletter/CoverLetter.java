package com.hireflow.hireflow.domain.coverletter;

import com.hireflow.hireflow.domain.application.Application;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "cover_letters")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoverLetter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String aiFeedback;       // AI 첨삭 결과, 받기 전엔 null

    private Integer aiScore;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    public void update(String content) {
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateScore(int aiScore, String aiFeedback) {
        this.aiScore = aiScore;
        this.aiFeedback = aiFeedback;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateFeedback(String improveContent) {
        this.aiFeedback = improveContent;
        this.updatedAt = LocalDateTime.now();
    }
}
