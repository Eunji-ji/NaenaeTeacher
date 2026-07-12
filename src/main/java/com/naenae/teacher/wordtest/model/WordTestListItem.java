package com.naenae.teacher.wordtest.model;

import java.time.LocalDate;

public record WordTestListItem(
        Long id,
        LocalDate startDate,
        LocalDate endDate,
        String courseNames,
        int wordCount
) {
}
