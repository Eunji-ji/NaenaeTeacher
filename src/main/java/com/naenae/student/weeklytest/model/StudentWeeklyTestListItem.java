package com.naenae.student.weeklytest.model;

import java.time.LocalDateTime;

public record StudentWeeklyTestListItem(Long id, String name, String courseName, LocalDateTime createdAt, Integer score) {
}
