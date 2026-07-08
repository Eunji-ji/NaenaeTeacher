package com.naenae.student.dashboard.service;

import java.time.LocalDate;

import com.naenae.common.vocabulary.model.TodayWordView;
import com.naenae.common.vocabulary.service.TodayWordService;
import com.naenae.student.dashboard.model.StudentDashboard;
import com.naenae.student.profile.domain.Student;
import com.naenae.student.profile.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentDashboardService {

    private final StudentRepository studentRepository;
    private final TodayWordService todayWordService;

    public StudentDashboardService(StudentRepository studentRepository, TodayWordService todayWordService) {
        this.studentRepository = studentRepository;
        this.todayWordService = todayWordService;
    }

    @Transactional(readOnly = true)
    public StudentDashboard getDashboard(Long userId) {
        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("학생 정보를 찾을 수 없습니다."));
        TodayWordView todayWord = todayWordService.getStudentTodayWord(LocalDate.now(), student);
        return new StudentDashboard(
                student.getName(),
                todayWord.levelLabel(),
                todayWord.word(),
                todayWord.sentence()
        );
    }
}
