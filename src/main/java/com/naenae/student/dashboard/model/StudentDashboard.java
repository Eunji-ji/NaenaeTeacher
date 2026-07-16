package com.naenae.student.dashboard.model;
import com.naenae.common.board.model.BoardListItem;
import com.naenae.common.notice.model.DashboardNoticeItem;
import com.naenae.teacher.assignment.model.AssignmentListItem;
import java.util.List;
public record StudentDashboard(String studentName,String levelLabel,String word,String wordMeaning,String sentence,String sentenceMeaning,
                               List<AssignmentListItem> assignments,List<DashboardNoticeItem> notices,
                               List<BoardListItem> boardPosts) {}
