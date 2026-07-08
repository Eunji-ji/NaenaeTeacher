package com.naenae.teacher.studentstatus.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.naenae.student.profile.domain.Student;
import com.naenae.student.profile.repository.StudentRepository;
import com.naenae.teacher.course.domain.CourseStudent;
import com.naenae.teacher.course.repository.CourseRepository;
import com.naenae.teacher.course.repository.CourseStudentRepository;
import com.naenae.teacher.profile.domain.Teacher;
import com.naenae.teacher.profile.repository.TeacherRepository;
import com.naenae.teacher.student.model.CourseOption;
import com.naenae.teacher.studentstatus.model.StudentStatusPage;
import com.naenae.teacher.studentstatus.model.StudentStatusRow;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TeacherStudentStatusService {

    private final TeacherRepository teacherRepository;
    private final CourseRepository courseRepository;
    private final CourseStudentRepository courseStudentRepository;
    private final StudentRepository studentRepository;

    public TeacherStudentStatusService(
            TeacherRepository teacherRepository,
            CourseRepository courseRepository,
            CourseStudentRepository courseStudentRepository,
            StudentRepository studentRepository
    ) {
        this.teacherRepository = teacherRepository;
        this.courseRepository = courseRepository;
        this.courseStudentRepository = courseStudentRepository;
        this.studentRepository = studentRepository;
    }

    @Transactional(readOnly = true)
    public StudentStatusPage getPage(Long teacherUserId, Long courseId) {
        Teacher teacher = getTeacher(teacherUserId);
        List<CourseOption> courses = courseRepository.findByTeacherIdOrderByTitleAsc(teacher.getId()).stream()
                .map(course -> new CourseOption(course.getId(), course.getTitle()))
                .toList();

        if (courses.isEmpty()) {
            return new StudentStatusPage(List.of(), null, null, List.of());
        }

        Long selectedCourseId = courseId != null ? courseId : courses.get(0).id();
        String selectedCourseTitle = courseRepository.findByIdAndTeacherId(selectedCourseId, teacher.getId())
                .orElseThrow(() -> new IllegalArgumentException("선택한 반을 찾을 수 없습니다."))
                .getTitle();

        List<CourseStudent> mappings = courseStudentRepository
                .findByCourseIdAndStudentTeacherIdOrderByStudentNameAsc(selectedCourseId, teacher.getId());

        Map<Long, StudentStatusRow> rows = new LinkedHashMap<>();
        for (CourseStudent mapping : mappings) {
            Student student = mapping.getStudent();
            rows.put(student.getId(), new StudentStatusRow(
                    student.getId(),
                    student.getName(),
                    mapping.getCourse().getTitle(),
                    student.getSchoolName(),
                    student.getPhone(),
                    student.getMemoSummary()
            ));
        }

        return new StudentStatusPage(courses, selectedCourseId, selectedCourseTitle, rows.values().stream().toList());
    }

    @Transactional
    public void saveMemo(Long teacherUserId, Long studentId, String memoSummary) {
        Teacher teacher = getTeacher(teacherUserId);
        Student student = studentRepository.findByIdAndTeacherId(studentId, teacher.getId())
                .orElseThrow(() -> new IllegalArgumentException("학생을 찾을 수 없습니다."));

        student.updateMemoSummary(normalizeMemo(memoSummary));
        studentRepository.save(student);
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
        if (trimmed.length() > 1000) {
            throw new IllegalArgumentException("특징은 1000자 이내로 입력해 주세요.");
        }
        return trimmed;
    }
}
