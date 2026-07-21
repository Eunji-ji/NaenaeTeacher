package com.naenae.student.learning.model;

import com.naenae.common.pagination.PageView;

public record StudentAttendancePage(
        String studentName,
        int attendanceRate,
        long totalCount,
        long presentCount,
        long lateCount,
        long absentCount,
        long excusedCount,
        PageView<StudentAttendanceItem> records
) {
}
