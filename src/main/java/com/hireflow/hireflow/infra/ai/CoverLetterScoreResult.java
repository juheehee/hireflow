package com.hireflow.hireflow.infra.ai;

import java.util.List;

public record CoverLetterScoreResult(
        int score,
        String feedback,
        List<String> strengths,
        List<String> improvements) {}
