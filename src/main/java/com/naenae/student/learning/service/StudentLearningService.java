package com.naenae.student.learning.service;

import com.naenae.common.pagination.PageView;
import com.naenae.common.pagination.PaginationSupport;
import com.naenae.student.learning.model.StudentAttendanceItem;
import com.naenae.student.learning.model.StudentAttendancePage;
import com.naenae.student.learning.model.StudentScoreItem;
import com.naenae.student.learning.model.StudentScorePage;
import com.naenae.student.learning.model.StudentWordTestItem;
import com.naenae.student.profile.domain.AcademicExamType;
import com.naenae.student.profile.domain.Student;
import com.naenae.student.profile.domain.StudentAcademicRecord;
import com.naenae.student.profile.repository.StudentAcademicRecordRepository;
import com.naenae.student.profile.repository.StudentRepository;
import com.naenae.teacher.attendance.domain.Attendance;
import com.naenae.teacher.attendance.domain.AttendanceStatus;
import com.naenae.teacher.attendance.repository.AttendanceRepository;
import com.naenae.teacher.course.repository.CourseStudentRepository;
import com.naenae.teacher.wordtest.domain.WordTest;
import com.naenae.teacher.wordtest.repository.WordTestRepository;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentLearningService {

    private final StudentRepository studentRepository;
    private final StudentAcademicRecordRepository academicRecordRepository;
    private final CourseStudentRepository courseStudentRepository;
    private final WordTestRepository wordTestRepository;
    private final AttendanceRepository attendanceRepository;

    public StudentLearningService(
            StudentRepository studentRepository,
            StudentAcademicRecordRepository academicRecordRepository,
            CourseStudentRepository courseStudentRepository,
            WordTestRepository wordTestRepository,
            AttendanceRepository attendanceRepository
    ) {
        this.studentRepository = studentRepository;
        this.academicRecordRepository = academicRecordRepository;
        this.courseStudentRepository = courseStudentRepository;
        this.wordTestRepository = wordTestRepository;
        this.attendanceRepository = attendanceRepository;
    }

    @Transactional(readOnly = true)
    public StudentScorePage getScores(Long userId) {
        Student student = getStudent(userId);
        List<StudentAcademicRecord> records = academicRecordRepository
                .findByStudentIdAndStudentTeacherIdOrderByExamYearAscExamTypeAsc(
                        student.getId(),
                        student.getTeacher().getId()
                ).stream()
                .sorted(Comparator.comparingInt(StudentAcademicRecord::getExamYear)
                        .thenComparingInt(record -> examOrder(record.getExamType())))
                .toList();

        List<StudentScoreItem> scores = new java.util.ArrayList<>();
        Integer previousScore = null;
        int total = 0;
        for (StudentAcademicRecord record : records) {
            Integer change = previousScore == null ? null : record.getScore() - previousScore;
            scores.add(new StudentScoreItem(
                    record.getExamYear(),
                    examLabel(record.getExamType()),
                    record.getScore(),
                    change
            ));
            previousScore = record.getScore();
            total += record.getScore();
        }

        Integer latestScore = scores.isEmpty() ? null : scores.get(scores.size() - 1).score();
        Integer latestChange = scores.isEmpty() ? null : scores.get(scores.size() - 1).changeFromPrevious();
        int averageScore = scores.isEmpty() ? 0 : Math.round((float) total / scores.size());
        return new StudentScorePage(student.getName(), List.copyOf(scores), latestScore, latestChange, averageScore);
    }

    @Transactional(readOnly = true)
    public PageView<StudentWordTestItem> getWordTests(Long userId, int page) {
        Student student = getStudent(userId);
        List<Long> courseIds = courseStudentRepository.findByStudent_IdOrderByCourseTitleAsc(student.getId()).stream()
                .map(mapping -> mapping.getCourse().getId())
                .distinct()
                .toList();
        if (courseIds.isEmpty()) {
            return PaginationSupport.toView(Page.empty(PaginationSupport.pageRequest(page)));
        }
        LocalDate today = LocalDate.now();
        Page<WordTest> tests = wordTestRepository.findStudentTests(courseIds, PaginationSupport.pageRequest(page));
        return PaginationSupport.toView(tests.map(test -> wordTestItem(test, today)));
    }

    @Transactional(readOnly = true)
    public StudentAttendancePage getAttendance(Long userId, int page) {
        Student student = getStudent(userId);
        Long teacherId = student.getTeacher().getId();
        List<Attendance> allRecords = attendanceRepository.findByStudentIdAndTeacherId(student.getId(), teacherId);
        long present = countStatus(allRecords, AttendanceStatus.PRESENT);
        long late = countStatus(allRecords, AttendanceStatus.LATE);
        long absent = countStatus(allRecords, AttendanceStatus.ABSENT);
        long excused = countStatus(allRecords, AttendanceStatus.EXCUSED);
        long total = allRecords.size();
        int rate = total == 0 ? 0 : (int) Math.round((double) (present + late) * 100 / total);

        PageView<StudentAttendanceItem> records = PaginationSupport.toView(
                attendanceRepository.findByStudentIdAndTeacherIdOrderByAttendanceDateDescIdDesc(
                        student.getId(),
                        teacherId,
                        PaginationSupport.pageRequest(page)
                ).map(this::attendanceItem)
        );
        return new StudentAttendancePage(
                student.getName(), rate, total, present, late, absent, excused, records
        );
    }

    private StudentWordTestItem wordTestItem(WordTest test, LocalDate today) {
        String statusLabel;
        String statusClass;
        if (today.isBefore(test.getStartDate())) {
            statusLabel = "예정";
            statusClass = "scheduled";
        } else if (today.isAfter(test.getEndDate())) {
            statusLabel = "종료";
            statusClass = "completed";
        } else {
            statusLabel = "진행 중";
            statusClass = "active";
        }
        String courseNames = test.getCourses().stream()
                .map(mapping -> mapping.getCourse().getTitle())
                .distinct()
                .sorted()
                .reduce((left, right) -> left + ", " + right)
                .orElse("-");
        return new StudentWordTestItem(
                test.getId(), test.getStartDate(), test.getEndDate(), courseNames, test.getWords().size(),
                statusLabel, statusClass, "결과 미등록"
        );
    }

    private StudentAttendanceItem attendanceItem(Attendance attendance) {
        return new StudentAttendanceItem(
                attendance.getAttendanceDate(),
                attendance.getCourse() == null ? "공통" : attendance.getCourse().getTitle(),
                attendanceLabel(attendance.getStatus()),
                attendance.getStatus().name().toLowerCase()
        );
    }

    private long countStatus(List<Attendance> records, AttendanceStatus status) {
        return records.stream().filter(record -> record.getStatus() == status).count();
    }

    private int examOrder(AcademicExamType examType) {
        return examType == AcademicExamType.MIDTERM ? 0 : 1;
    }

    private String examLabel(AcademicExamType examType) {
        return examType == AcademicExamType.MIDTERM ? "중간고사" : "기말고사";
    }

    private String attendanceLabel(AttendanceStatus status) {
        return switch (status) {
            case PRESENT -> "출석";
            case ABSENT -> "결석";
            case LATE -> "지각";
            case EXCUSED -> "인정결석";
        };
    }

    private Student getStudent(Long userId) {
        return studentRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("학생 정보를 찾을 수 없습니다."));
    }
}
