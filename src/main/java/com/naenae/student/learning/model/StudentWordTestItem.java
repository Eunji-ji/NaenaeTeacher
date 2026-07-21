package com.naenae.student.learning.model;

import java.time.LocalDate;

public record StudentWordTestItem(
        Long id,
        LocalDate startDate,
        LocalDate endDate,
        String courseNames,
        int wordCount,
        String statusLabel,
        String statusClass,
        String resultLabel
) {
}
