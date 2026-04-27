package com.hireflow.hireflow.domain.jobposting.dto;

import com.hireflow.hireflow.domain.jobposting.JobPosting;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class JobPostingResponseDto {
    private Long id;
    private String title;
    private String company;
    private String location;
    private String description;
    private String techStackTags;
    private LocalDate deadline;
    private String sourceUrl;
    private String source;

    public JobPostingResponseDto(JobPosting jp) {
        this.id = jp.getId();
        this.title = jp.getTitle();
        this.company = jp.getCompany();
        this.location = jp.getLocation();
        this.description = jp.getDescription();
        this.techStackTags = jp.getTechStackTags();
        this.deadline = jp.getDeadline();
        this.sourceUrl = jp.getSourceUrl();
        this.source = jp.getSource();
    }
}
