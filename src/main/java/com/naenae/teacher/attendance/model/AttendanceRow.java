package com.naenae.teacher.attendance.model;

import java.time.LocalDateTime;

public record AttendanceRow(
        Long studentId,
        String studentName,
        String schoolName,
        boolean checked,
        LocalDateTime checkedAt
) {
}
