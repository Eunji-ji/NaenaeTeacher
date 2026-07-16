package com.naenae.teacher.classschedule.model;

import com.naenae.teacher.classschedule.domain.ScheduleWeekday;
import java.time.LocalTime;

public record ClassScheduleItem(Long id, Long courseId, String courseTitle, ScheduleWeekday weekday,
                                String weekdayLabel, String lessonTitle, LocalTime startTime, LocalTime endTime) {
}
