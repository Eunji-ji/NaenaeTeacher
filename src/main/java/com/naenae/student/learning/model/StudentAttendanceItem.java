package com.naenae.student.learning.model;

import java.time.LocalDate;

public record StudentAttendanceItem(
        LocalDate attendanceDate,
        String courseName,
        String statusLabel,
        String statusClass
) {
}
