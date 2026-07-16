package com.naenae.student.dashboard.service;

import java.time.LocalDate;
import com.naenae.common.board.service.BoardService;
import com.naenae.common.vocabulary.model.TodayWordView;
import com.naenae.common.vocabulary.service.TodayWordService;
import com.naenae.common.vocabulary.service.TodaySentenceService;
import com.naenae.student.dashboard.model.StudentDashboard;
import com.naenae.student.assignment.service.StudentAssignmentService;
import com.naenae.student.notice.service.StudentNoticeService;
import com.naenae.student.profile.domain.Student;
import com.naenae.student.profile.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentDashboardService {
    private final StudentRepository studentRepository;
    private final TodayWordService todayWordService;
    private final StudentNoticeService studentNoticeService;
    private final StudentAssignmentService studentAssignmentService;
    private final BoardService boardService;
    private final TodaySentenceService todaySentenceService;

    public StudentDashboardService(StudentRepository studentRepository, TodayWordService todayWordService,
                                   StudentNoticeService studentNoticeService, StudentAssignmentService studentAssignmentService,
                                   BoardService boardService, TodaySentenceService todaySentenceService) {
        this.studentRepository = studentRepository;
        this.todayWordService = todayWordService;
        this.studentNoticeService = studentNoticeService;
        this.studentAssignmentService = studentAssignmentService;
        this.boardService = boardService;
        this.todaySentenceService = todaySentenceService;
    }

    @Transactional(readOnly = true)
    public StudentDashboard getDashboard(Long userId) {
        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("학생 정보를 찾을 수 없습니다."));
        TodayWordView todayWord = todayWordService.getStudentTodayWord(LocalDate.now(), student);
        var todaySentence = todaySentenceService.getStudentTodaySentence(LocalDate.now(), student);
        return new StudentDashboard(student.getName(), todayWord.levelLabel(), todayWord.word(), todayWord.meaning(), todaySentence.sentence(), todaySentence.meaning(),
                studentAssignmentService.getRecentAssignments(student, 5), studentNoticeService.getDashboardNotices(student, 5),
                boardService.getRecentPosts(userId, 3));
    }
}
