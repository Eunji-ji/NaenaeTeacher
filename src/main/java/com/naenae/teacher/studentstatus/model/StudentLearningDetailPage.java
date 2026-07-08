package com.naenae.teacher.studentstatus.model;

import java.util.List;

public record StudentLearningDetailPage(
        Long studentId,
        String studentName,
        String courseTitle,
        String schoolName,
        String phone,
        String memoSummary,
        int memoMaxLength,
        int currentYear,
        List<StudentLearningScoreRow> scores,
        List<StudentLearningChartPoint> chartPoints,
        String chartPolyline,
        int scoreDelta
) {
}
