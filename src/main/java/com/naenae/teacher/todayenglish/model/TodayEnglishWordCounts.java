package com.naenae.teacher.todayenglish.model;

public record TodayEnglishWordCounts(long level1, long level2, long level3) {
    public long total() {
        return level1 + level2 + level3;
    }
}
