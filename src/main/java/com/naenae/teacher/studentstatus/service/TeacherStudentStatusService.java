package com.naenae.teacher.studentstatus.service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.naenae.student.profile.domain.AcademicExamType;
import com.naenae.student.profile.domain.Student;
import com.naenae.student.profile.domain.StudentAcademicRecord;
import com.naenae.student.profile.repository.StudentAcademicRecordRepository;
import com.naenae.student.profile.repository.StudentRepository;
import com.naenae.teacher.course.domain.CourseStudent;
import com.naenae.teacher.course.repository.CourseRepository;
import com.naenae.teacher.course.repository.CourseStudentRepository;
import com.naenae.teacher.profile.domain.Teacher;
import com.naenae.teacher.profile.repository.TeacherRepository;
import com.naenae.teacher.student.model.CourseOption;
import com.naenae.teacher.studentstatus.model.StudentLearningDetailPage;
import com.naenae.teacher.studentstatus.model.StudentLearningChartPoint;
import com.naenae.teacher.studentstatus.model.StudentLearningPage;
import com.naenae.teacher.studentstatus.model.StudentLearningRow;
import com.naenae.teacher.studentstatus.model.StudentLearningScoreRow;
import com.naenae.teacher.studentstatus.model.StudentLearningScoreTableRow;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TeacherStudentStatusService {

    private static final int MEMO_MAX_LENGTH = 4000;
    private static final int DEFAULT_CHART_SLOT_COUNT = 6;
    private static final int CHART_X_PADDING = 6;
    private static final int CHART_Y_TOP_PADDING = 6;
    private static final int CHART_Y_BOTTOM_PADDING = 4;

    private final TeacherRepository teacherRepository;
    private final CourseRepository courseRepository;
    private final CourseStudentRepository courseStudentRepository;
    private final StudentRepository studentRepository;
    private final StudentAcademicRecordRepository studentAcademicRecordRepository;

    public TeacherStudentStatusService(
            TeacherRepository teacherRepository,
            CourseRepository courseRepository,
            CourseStudentRepository courseStudentRepository,
            StudentRepository studentRepository,
            StudentAcademicRecordRepository studentAcademicRecordRepository
    ) {
        this.teacherRepository = teacherRepository;
        this.courseRepository = courseRepository;
        this.courseStudentRepository = courseStudentRepository;
        this.studentRepository = studentRepository;
        this.studentAcademicRecordRepository = studentAcademicRecordRepository;
    }

    @Transactional(readOnly = true)
    public StudentLearningPage getPage(Long teacherUserId, Long courseId) {
        Teacher teacher = getTeacher(teacherUserId);
        List<CourseOption> courses = courseRepository.findByTeacherIdOrderByTitleAsc(teacher.getId()).stream()
                .map(course -> new CourseOption(course.getId(), course.getTitle()))
                .toList();

        if (courses.isEmpty()) {
            return new StudentLearningPage(List.of(), null, null, List.of());
        }

        Long selectedCourseId = courseId != null ? courseId : courses.get(0).id();
        String selectedCourseTitle = courseRepository.findByIdAndTeacherId(selectedCourseId, teacher.getId())
                .orElseThrow(() -> new IllegalArgumentException("선택한 반을 찾을 수 없습니다."))
                .getTitle();

        List<CourseStudent> mappings = courseStudentRepository
                .findByCourseIdAndStudentTeacherIdOrderByStudentNameAsc(selectedCourseId, teacher.getId());

        Map<Long, StudentLearningRow> rows = new LinkedHashMap<>();
        for (CourseStudent mapping : mappings) {
            Student student = mapping.getStudent();
            rows.put(student.getId(), new StudentLearningRow(
                    student.getId(),
                    student.getName(),
                    student.getSchoolName(),
                    student.getPhone()
            ));
        }

        return new StudentLearningPage(courses, selectedCourseId, selectedCourseTitle, rows.values().stream().toList());
    }

    @Transactional(readOnly = true)
    public StudentLearningDetailPage getDetailPage(Long teacherUserId, Long courseId, Long studentId) {
        Teacher teacher = getTeacher(teacherUserId);
        Student student = studentRepository.findByIdAndTeacherId(studentId, teacher.getId())
                .orElseThrow(() -> new IllegalArgumentException("학생을 찾을 수 없습니다."));

        String courseTitle = courseId == null
                ? "-"
                : courseRepository.findByIdAndTeacherId(courseId, teacher.getId())
                .map(course -> course.getTitle())
                .orElse("-");

        int currentYear = LocalDate.now().getYear();
        List<StudentAcademicRecord> records = studentAcademicRecordRepository
                .findByStudentIdAndStudentTeacherIdOrderByExamYearAscExamTypeAsc(studentId, teacher.getId());

        List<StudentLearningScoreRow> scoreRows = records.stream()
                .map(record -> new StudentLearningScoreRow(
                        record.getExamYear(),
                        toLabel(record.getExamType()),
                        toExamOrder(record.getExamType()),
                        record.getScore()
                ))
                .toList();

        List<StudentLearningChartPoint> chartPoints = buildChartPoints(scoreRows);
        String chartPolyline = buildPolyline(chartPoints);
        List<StudentLearningScoreTableRow> scoreTableRows = buildScoreTableRows(records);

        int scoreDelta = 0;
        if (scoreRows.size() >= 2) {
            scoreDelta = scoreRows.get(scoreRows.size() - 1).score() - scoreRows.get(0).score();
        }

        return new StudentLearningDetailPage(
                student.getId(),
                student.getName(),
                courseTitle,
                student.getSchoolName(),
                student.getPhone(),
                student.getMemoSummary(),
                MEMO_MAX_LENGTH,
                currentYear,
                scoreRows,
                scoreTableRows,
                chartPoints,
                chartPolyline,
                scoreDelta
        );
    }

    @Transactional
    public void saveMemo(Long teacherUserId, Long studentId, String memoSummary) {
        Teacher teacher = getTeacher(teacherUserId);
        Student student = studentRepository.findByIdAndTeacherId(studentId, teacher.getId())
                .orElseThrow(() -> new IllegalArgumentException("학생을 찾을 수 없습니다."));

        student.updateMemoSummary(normalizeMemo(memoSummary));
        studentRepository.save(student);
    }

    @Transactional
    public void saveScore(Long teacherUserId, Long studentId, AcademicExamType examType, Integer score) {
        Teacher teacher = getTeacher(teacherUserId);
        Student student = studentRepository.findByIdAndTeacherId(studentId, teacher.getId())
                .orElseThrow(() -> new IllegalArgumentException("학생을 찾을 수 없습니다."));
        if (examType == null) {
            throw new IllegalArgumentException("시험 종류를 선택해 주세요.");
        }
        if (score == null || score < 0 || score > 100) {
            throw new IllegalArgumentException("성적은 0점부터 100점까지 입력해 주세요.");
        }

        int currentYear = LocalDate.now().getYear();
        StudentAcademicRecord record = studentAcademicRecordRepository
                .findByStudentIdAndStudentTeacherIdAndExamYearAndExamType(studentId, teacher.getId(), currentYear, examType)
                .orElseGet(() -> StudentAcademicRecord.create(teacher, student, currentYear, examType, score));
        record.updateScore(score);
        studentAcademicRecordRepository.save(record);
    }

    private Teacher getTeacher(Long teacherUserId) {
        return teacherRepository.findByUserId(teacherUserId)
                .orElseThrow(() -> new IllegalStateException("선생님 정보를 찾을 수 없습니다."));
    }

    private String normalizeMemo(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.length() > MEMO_MAX_LENGTH) {
            throw new IllegalArgumentException("특징은 4000자 이내로 입력해 주세요.");
        }
        return trimmed;
    }

    private String toLabel(AcademicExamType examType) {
        return switch (examType) {
            case MIDTERM -> "중간고사";
            case FINAL -> "기말고사";
        };
    }

    private int toExamOrder(AcademicExamType examType) {
        return switch (examType) {
            case MIDTERM -> 0;
            case FINAL -> 1;
        };
    }

    private List<StudentLearningChartPoint> buildChartPoints(List<StudentLearningScoreRow> scoreRows) {
        if (scoreRows.isEmpty()) {
            return List.of();
        }
        int firstYear = scoreRows.get(0).examYear();
        int maxSlot = scoreRows.stream()
                .mapToInt(scoreRow -> toSlotIndex(firstYear, scoreRow))
                .max()
                .orElse(0);
        int totalSlots = Math.max(DEFAULT_CHART_SLOT_COUNT, maxSlot + 1);
        return scoreRows.stream()
                .map(scoreRow -> {
                    int slotIndex = toSlotIndex(firstYear, scoreRow);
                    int x = toChartX(slotIndex, totalSlots);
                    int y = toChartY(scoreRow.score());
                    return new StudentLearningChartPoint(
                            x,
                            y,
                            scoreRow.examYear() + " " + scoreRow.examTypeLabel(),
                            scoreRow.score()
                    );
                })
                .toList();
    }

    private List<StudentLearningScoreTableRow> buildScoreTableRows(List<StudentAcademicRecord> records) {
        Map<Integer, ScoreTableAccumulator> rows = new LinkedHashMap<>();
        for (StudentAcademicRecord record : records) {
            ScoreTableAccumulator row = rows.computeIfAbsent(record.getExamYear(), ScoreTableAccumulator::new);
            if (record.getExamType() == AcademicExamType.MIDTERM) {
                row.midtermScore = record.getScore();
            } else if (record.getExamType() == AcademicExamType.FINAL) {
                row.finalScore = record.getScore();
            }
        }
        return rows.values().stream()
                .map(row -> new StudentLearningScoreTableRow(row.examYear, row.midtermScore, row.finalScore))
                .toList();
    }

    private static class ScoreTableAccumulator {
        private final int examYear;
        private Integer midtermScore;
        private Integer finalScore;

        private ScoreTableAccumulator(int examYear) {
            this.examYear = examYear;
        }
    }

    private int toSlotIndex(int firstYear, StudentLearningScoreRow scoreRow) {
        return Math.max(0, (scoreRow.examYear() - firstYear) * 2 + scoreRow.examOrder());
    }

    private int toChartX(int slotIndex, int totalSlots) {
        int usableWidth = 100 - (CHART_X_PADDING * 2);
        int denominator = Math.max(1, totalSlots - 1);
        return CHART_X_PADDING + Math.round((float) slotIndex * usableWidth / denominator);
    }

    private int toChartY(int score) {
        int usableHeight = 100 - CHART_Y_TOP_PADDING - CHART_Y_BOTTOM_PADDING;
        return CHART_Y_TOP_PADDING + Math.round((float) (100 - score) * usableHeight / 100);
    }

    private String buildPolyline(List<StudentLearningChartPoint> chartPoints) {
        if (chartPoints.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < chartPoints.size(); i++) {
            StudentLearningChartPoint point = chartPoints.get(i);
            if (i > 0) {
                builder.append(' ');
            }
            builder.append(point.x()).append(',').append(point.y());
        }
        return builder.toString();
    }
}
