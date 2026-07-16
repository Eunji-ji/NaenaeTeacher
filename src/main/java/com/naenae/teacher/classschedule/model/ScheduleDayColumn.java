package com.naenae.teacher.classschedule.model;

import com.naenae.teacher.classschedule.domain.ScheduleWeekday;
import java.util.List;

public record ScheduleDayColumn(ScheduleWeekday weekday, String label, String shortLabel,
                                List<ClassScheduleItem> schedules) {
}
