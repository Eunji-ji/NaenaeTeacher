package com.naenae.teacher.dashboard.model;

public record TeacherDashboard(
        int totalStudentCount,
        int todayAttendanceCount,
        int openAssignmentCount,
        int recentMemoCount
) {
}
