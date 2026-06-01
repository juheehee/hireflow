package com.hireflow.hireflow.domain.coverletter.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class CoverLetterScoreResponseDto {

    private final int score;
    private final String feedback;
    private final List<String> strengths;
    private final List<String> improvements;

    public CoverLetterScoreResponseDto(int score, String feedback,
                                       List<String> strengths, List<String> improvements) {
        this.score = score;
        this.feedback = feedback;
        this.strengths = strengths;
        this.improvements = improvements;
    }
}
