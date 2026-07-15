package com.naenae.common.notice.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "notice_attachments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoticeAttachment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "notice_id", nullable = false) private Notice notice;
    @Column(name = "original_name", nullable = false, length = 255) private String originalName;
    @Column(name = "stored_name", nullable = false, unique = true, length = 255) private String storedName;
    @Column(name = "content_type", length = 150) private String contentType;
    @Column(name = "file_size", nullable = false) private long fileSize;
    static NoticeAttachment create(Notice notice, String originalName, String storedName, String contentType, long fileSize) {
        NoticeAttachment value = new NoticeAttachment(); value.notice = notice; value.originalName = originalName;
        value.storedName = storedName; value.contentType = contentType; value.fileSize = fileSize; return value;
    }
}