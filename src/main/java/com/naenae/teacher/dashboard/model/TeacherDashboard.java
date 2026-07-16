package com.naenae.teacher.dashboard.model;

import com.naenae.common.board.model.BoardListItem;
import com.naenae.common.notice.model.DashboardNoticeItem;
import java.util.List;
import com.naenae.teacher.classschedule.model.ClassScheduleItem;

public record TeacherDashboard(
        int totalStudentCount,
        int todayPresentCount,
        int todayLateCount,
        int todayAbsentCount,
        int todayAttendanceRate,
        int openAssignmentCount,
        int recentMemoCount,
        DashboardNoticeItem todayNotice,
        List<BoardListItem> boardPosts,
        List<ClassScheduleItem> todaySchedules
) {
}
