package com.hireflow.hireflow.domain.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hireflow.hireflow.domain.application.Application;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApplicationResponseDto {

    private Long id;
    private String status;
    private String memo;
    private LocalDate appliedAt;
    private LocalDate interviewDate;
    private LocalDateTime updatedAt;

    // 공고 정보 (간략하게)
    private Long jobPostingId;
    private String jobPostingTitle;
    private String company;

    public ApplicationResponseDto(Application application) {
        this.id = application.getId();
        this.status = application.getStatus();
        this.memo = application.getMemo();
        this.appliedAt = application.getAppliedAt();
        this.interviewDate = application.getInterviewDate();
        this.updatedAt = application.getUpdatedAt();
        this.jobPostingId = application.getJobPosting().getId();
        this.jobPostingTitle = application.getJobPosting().getTitle();
        this.company = application.getJobPosting().getCompany();
    }
}
