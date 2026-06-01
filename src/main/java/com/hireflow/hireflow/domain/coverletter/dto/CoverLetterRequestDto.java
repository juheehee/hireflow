package com.hireflow.hireflow.domain.coverletter.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CoverLetterRequestDto {

    @NotBlank(message = "자소서 내용을 입력해주세요.")
    private String content;
}
