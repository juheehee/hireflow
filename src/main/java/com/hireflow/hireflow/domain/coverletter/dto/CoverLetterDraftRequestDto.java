package com.hireflow.hireflow.domain.coverletter.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CoverLetterDraftRequestDto {

    @NotBlank(message = "자소서 문항을 입력해주세요.")
    private String question; // 예: "지원동기를 작성해주세요"

    @NotBlank(message = "강조하고 싶은 내용을 입력해주세요.")
    private String prompt;   // 예: "백엔드 신입, 강조하고 싶은 경험:..."
}
