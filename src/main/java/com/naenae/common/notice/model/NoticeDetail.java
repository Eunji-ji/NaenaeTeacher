package com.naenae.common.notice.model;
import java.time.LocalDateTime;
import java.util.List;
public record NoticeDetail(Long id, LocalDateTime createdAt, String title, String targetLabel, String contentHtml, List<NoticeAttachmentItem> attachments) {}