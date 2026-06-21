package com.hireflow.hireflow.domain.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter @Builder
@NoArgsConstructor @AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String techStack;        // 쉼표 구분 태그 ex) "Java,Spring,React"


    private String resumeUrl;        // S3 URL, 업로드 전엔 null

    @Enumerated(EnumType.STRING)
    private ResumeParseStatus resumeParseStatus; // NONE / PENDING / COMPLETED / FAILED

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public void uploadResume(String resumeUrl) {
        this.resumeUrl = resumeUrl;
        this.resumeParseStatus = ResumeParseStatus.PENDING;
        this.updatedAt = LocalDateTime.now();
    }

    public void completeResumeParsing(String parsedTechStack) {
        this.techStack = parsedTechStack;
        this.resumeParseStatus = ResumeParseStatus.COMPLETED;
        this.updatedAt = LocalDateTime.now();
    }

    public void failResumeParsing() {
        this.resumeParseStatus = ResumeParseStatus.FAILED;
        this.updatedAt = LocalDateTime.now();
    }

    public void confirmTechStack(String techStack) {
        this.techStack = techStack;
        this.updatedAt = LocalDateTime.now();
    }
}
