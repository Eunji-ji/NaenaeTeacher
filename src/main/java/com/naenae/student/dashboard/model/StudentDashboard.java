package com.naenae.student.dashboard.model;

import com.naenae.common.notice.model.NoticeListItem;
import java.util.List;

public record StudentDashboard(
        String studentName,
        String levelLabel,
        String word,
        String sentence,
        List<NoticeListItem> notices
) {
}