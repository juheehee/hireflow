package com.hireflow.hireflow.domain.coverletter.dto;

import com.hireflow.hireflow.domain.coverletter.CoverLetter;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CoverLetterResponseDto {

    private final Long id;
    private final String content;
    private final Integer aiScore;
    private final String aiFeedback;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public CoverLetterResponseDto(CoverLetter coverLetter) {
        this.id = coverLetter.getId();
        this.content = coverLetter.getContent();
        this.aiScore = coverLetter.getAiScore();
        this.aiFeedback = coverLetter.getAiFeedback();
        this.createdAt = coverLetter.getCreatedAt();
        this.updatedAt = coverLetter.getUpdatedAt();
    }
}
