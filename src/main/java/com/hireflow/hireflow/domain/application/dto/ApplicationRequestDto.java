package com.hireflow.hireflow.domain.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationRequestDto {

    private Long jobPostingId; // 어떤 공고에 지원할지
    private String memo;       // 선택값
}
