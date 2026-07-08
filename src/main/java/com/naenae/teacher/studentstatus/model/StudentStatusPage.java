package com.naenae.teacher.studentstatus.model;

import java.util.List;

import com.naenae.teacher.student.model.CourseOption;

public record StudentStatusPage(
        List<CourseOption> courses,
        Long selectedCourseId,
        String selectedCourseTitle,
        List<StudentStatusRow> students
) {
}
