package com.naenae.common.board.model;
import java.time.LocalDateTime;
import java.util.List;
public record BoardDetail(Long id, String title, String authorLabel, LocalDateTime createdAt, long viewCount,
                          String contentHtml, List<BoardAttachmentItem> attachments, List<BoardCommentItem> comments,
                          boolean canEdit, boolean canDelete) {}