package com.naenae.common.board.model;
import java.util.List;
public record BoardFormData(Long id, String title, String contentHtml, List<BoardAttachmentItem> attachments) {}