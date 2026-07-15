package com.naenae.teacher.wordtest.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record WordTestListItem(
        Long id,
        LocalDateTime createdAt,
        LocalDate startDate,
        LocalDate endDate,
        String courseNames,
        int wordCount
) {
}