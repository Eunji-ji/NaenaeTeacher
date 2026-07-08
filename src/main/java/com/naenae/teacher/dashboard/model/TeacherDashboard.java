package com.naenae.teacher.dashboard.model;

public record TeacherDashboard(
        int totalStudentCount,
        int todayPresentCount,
        int todayLateCount,
        int todayAbsentCount,
        int todayAttendanceRate,
        int openAssignmentCount,
        int recentMemoCount
) {
}
