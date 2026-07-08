package com.naenae.teacher.studentstatus.model;

public record StudentLearningRow(
        Long studentId,
        String studentName,
        String schoolName,
        String phone
) {
}
