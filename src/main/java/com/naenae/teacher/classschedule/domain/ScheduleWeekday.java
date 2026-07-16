package com.naenae.teacher.classschedule.domain;

public enum ScheduleWeekday {
    MONDAY("월요일", "월"),
    TUESDAY("화요일", "화"),
    WEDNESDAY("수요일", "수"),
    THURSDAY("목요일", "목"),
    FRIDAY("금요일", "금");

    private final String label;
    private final String shortLabel;

    ScheduleWeekday(String label, String shortLabel) {
        this.label = label;
        this.shortLabel = shortLabel;
    }

    public String getLabel() { return label; }
    public String getShortLabel() { return shortLabel; }
}
