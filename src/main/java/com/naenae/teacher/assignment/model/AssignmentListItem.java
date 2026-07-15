package com.naenae.teacher.assignment.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AssignmentListItem(
        Long id,
        LocalDateTime createdAt,
        LocalDate startDate,
        LocalDate endDate,
        String courseNames,
        String title,
        int attachmentCount
) {
}