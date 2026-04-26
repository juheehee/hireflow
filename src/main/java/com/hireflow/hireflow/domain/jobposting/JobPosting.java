package com.hireflow.hireflow.domain.jobposting;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_postings")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobPosting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String company;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;      // 공고 원문

    @Column(nullable = false)
    private String techStackTags;    // AI 추출 태그

    @Column(nullable = false)
    private LocalDate deadline;

    @Column(nullable = false)
    private String sourceUrl;        // 원본 공고 URL

    @Column(nullable = false)
    private String source;           // WANTED / SARAMIN / MANUAL

    private LocalDateTime crawledAt; // 직접입력(MANUAL)이면 null
}
