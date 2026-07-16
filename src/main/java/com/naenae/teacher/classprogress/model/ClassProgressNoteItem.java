package com.naenae.teacher.classprogress.model;

import com.naenae.teacher.classprogress.domain.ProgressNoteColor;
import java.time.LocalDateTime;

public record ClassProgressNoteItem(Long id, String courseTitle, String lessonTitle,
                                    ProgressNoteColor noteColor, String memo, LocalDateTime createdAt) {
    public boolean hasCourse() {
        return courseTitle != null && !courseTitle.isBlank();
    }

    public boolean hasLesson() {
        return lessonTitle != null && !lessonTitle.isBlank();
    }

    public String colorClass() {
        return noteColor.getCssClass();
    }
}
