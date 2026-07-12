package com.naenae.teacher.wordtest.model;

import com.naenae.teacher.student.model.CourseOption;
import java.time.LocalDate;
import java.util.List;

public record WordTestDetail(
        Long id,
        LocalDate startDate,
        LocalDate endDate,
        List<CourseOption> courses,
        List<WordTestWordRow> words
) {
}
