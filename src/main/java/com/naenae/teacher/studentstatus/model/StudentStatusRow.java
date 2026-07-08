package com.naenae.teacher.studentstatus.model;

public record StudentStatusRow(
        Long studentId,
        String studentName,
        String courseTitle,
        String schoolName,
        String phone,
        String memoSummary
) {
}
