package com.naenae.teacher.classprogress.model;

import java.time.LocalTime;

public record ProgressScheduleOption(Long id, Long courseId, String weekdayLabel,
                                     LocalTime startTime, LocalTime endTime, String lessonTitle) {
}

