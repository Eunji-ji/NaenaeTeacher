package com.naenae.student.weeklytest.model;

import com.naenae.teacher.weeklytest.model.WeeklyTestAttachmentItem;
import java.time.LocalDateTime;
import java.util.List;

public record StudentWeeklyTestDetail(Long id, String name, String courseName, String remarks,
                                    LocalDateTime createdAt, Integer score,
                                    List<WeeklyTestAttachmentItem> attachments) {
}
