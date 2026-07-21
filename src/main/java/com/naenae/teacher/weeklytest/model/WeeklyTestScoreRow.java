package com.naenae.teacher.weeklytest.model;

public record WeeklyTestScoreRow(Long id, Long studentId, String studentName, String schoolName, Integer score) {
}
