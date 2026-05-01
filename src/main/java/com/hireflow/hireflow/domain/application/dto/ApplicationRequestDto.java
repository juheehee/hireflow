package com.hireflow.hireflow.domain.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ApplicationRequestDto {

    private Long jobPostingId; // 어떤 공고에 지원할지
    private String memo;       // 선택값
}
