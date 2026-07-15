package com.naenae.common.board.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter @Entity @Table(name = "board_attachments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardAttachment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "post_id", nullable = false) private BoardPost post;
    @Column(name = "original_name", nullable = false, length = 255) private String originalName;
    @Column(name = "stored_name", nullable = false, unique = true, length = 255) private String storedName;
    @Column(name = "content_type", length = 150) private String contentType;
    @Column(name = "file_size", nullable = false) private long fileSize;
    static BoardAttachment create(BoardPost post, String originalName, String storedName, String contentType, long fileSize) {
        BoardAttachment value = new BoardAttachment(); value.post = post; value.originalName = originalName;
        value.storedName = storedName; value.contentType = contentType; value.fileSize = fileSize; return value;
    }
}