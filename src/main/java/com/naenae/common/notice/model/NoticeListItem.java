package com.naenae.common.notice.model;
import java.time.LocalDateTime;
public record NoticeListItem(Long id, LocalDateTime createdAt, String title, String targetLabel, int attachmentCount) {}