package com.naenae.teacher.weeklytest.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "weekly_test_attachments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeeklyTestAttachment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "weekly_test_id", nullable = false) private WeeklyTest weeklyTest;
    @Column(name = "original_name", nullable = false, length = 255) private String originalName;
    @Column(name = "stored_name", nullable = false, unique = true, length = 255) private String storedName;
    @Column(name = "content_type", length = 150) private String contentType;
    @Column(name = "file_size", nullable = false) private long fileSize;

    static WeeklyTestAttachment create(WeeklyTest test, String originalName, String storedName,
                                       String contentType, long fileSize) {
        WeeklyTestAttachment attachment = new WeeklyTestAttachment();
        attachment.weeklyTest = test;
        attachment.originalName = originalName;
        attachment.storedName = storedName;
        attachment.contentType = contentType;
        attachment.fileSize = fileSize;
        return attachment;
    }
}
