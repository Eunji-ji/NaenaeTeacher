package com.naenae.teacher.attendance.model;

import java.time.LocalDate;
import java.util.List;

import com.naenae.teacher.student.model.CourseOption;

public record AttendancePage(
        List<CourseOption> courses,
        Long selectedCourseId,
        String selectedCourseTitle,
        LocalDate selectedDate,
        String selectedDateLabel,
        LocalDate previousDate,
        LocalDate nextDate,
        List<AttendanceRow> students
) {
}
