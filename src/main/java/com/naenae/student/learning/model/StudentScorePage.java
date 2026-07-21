package com.naenae.student.learning.model;

import java.util.List;

public record StudentScorePage(
        String studentName,
        List<StudentScoreItem> scores,
        Integer latestScore,
        Integer latestChange,
        int averageScore
) {
}
