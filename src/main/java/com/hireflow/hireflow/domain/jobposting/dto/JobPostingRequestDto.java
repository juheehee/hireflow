package com.hireflow.hireflow.domain.jobposting.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class JobPostingRequestDto {

    private String title;
    private String company;
    private String location;
    private String description;
    private String techStackTags;
    private LocalDate deadline;
    private String sourceUrl;
}
