package com.naenae.common.notice.model;

import java.time.LocalDateTime;

public record DashboardNoticeItem(
        Long id,
        LocalDateTime createdAt,
        String title,
        String targetLabel,
        String summary
) {
}
