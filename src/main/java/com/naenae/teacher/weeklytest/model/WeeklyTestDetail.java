package com.naenae.teacher.weeklytest.model;

import java.time.LocalDateTime;
import java.util.List;

public record WeeklyTestDetail(
        Long id,
        String name,
        String courseName,
        String remarks,
        LocalDateTime createdAt,
        List<WeeklyTestAttachmentItem> attachments,
        List<WeeklyTestScoreRow> scores
) {
}
