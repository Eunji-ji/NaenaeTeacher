package com.naenae.teacher.weeklytest.model;

import java.time.LocalDateTime;

public record WeeklyTestListItem(
        Long id,
        String name,
        String courseName,
        String averageScore,
        LocalDateTime createdAt
) {
}
