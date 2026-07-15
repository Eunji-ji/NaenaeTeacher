package com.naenae.student.dashboard.model;
import com.naenae.common.notice.model.NoticeListItem;
import com.naenae.teacher.assignment.model.AssignmentListItem;
import java.util.List;
public record StudentDashboard(String studentName,String levelLabel,String word,String sentence,
                               List<AssignmentListItem> assignments,List<NoticeListItem> notices) {}