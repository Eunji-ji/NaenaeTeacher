package com.naenae.common.board.model;
import java.time.LocalDateTime;
public record BoardListItem(Long id, String title, String authorLabel, LocalDateTime createdAt,
                            long viewCount, int commentCount, int attachmentCount) {}