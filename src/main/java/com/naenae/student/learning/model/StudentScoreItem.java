package com.naenae.student.learning.model;

public record StudentScoreItem(
        int examYear,
        String examLabel,
        int score,
        Integer changeFromPrevious
) {
}
