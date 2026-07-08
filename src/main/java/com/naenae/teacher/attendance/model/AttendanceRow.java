package com.naenae.teacher.attendance.model;

import java.time.LocalDateTime;

public record AttendanceRow(
        Long studentId,
        String studentName,
        String schoolName,
        String status,
        LocalDateTime checkedAt
) {
}
