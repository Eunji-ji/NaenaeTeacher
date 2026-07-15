package com.naenae.teacher.assignment.model;

import com.naenae.teacher.student.model.CourseOption;
import java.time.LocalDate;
import java.util.List;

public record AssignmentFormData(
        Long id,
        List<CourseOption> courses,
        String title,
        LocalDate startDate,
        LocalDate endDate,
        String contentHtml,
        List<AssignmentAttachmentItem> attachments
) {
}