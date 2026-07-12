package com.naenae.teacher.studentstatus.model;

public record StudentLearningScoreRow(
        int examYear,
        String examTypeLabel,
        int examOrder,
        int score
) {
}
