package com.naenae.common.board.model;
import java.time.LocalDateTime;
public record BoardCommentItem(Long id, String authorLabel, String content, LocalDateTime createdAt, boolean canDelete) {}