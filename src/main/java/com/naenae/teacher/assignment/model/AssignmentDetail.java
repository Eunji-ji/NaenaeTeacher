package com.naenae.teacher.assignment.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record AssignmentDetail(
        Long id,
        LocalDateTime createdAt,
        LocalDate startDate,
        LocalDate endDate,
        String courseNames,
        String title,
        String contentHtml,
        List<AssignmentAttachmentItem> attachments
) {
}