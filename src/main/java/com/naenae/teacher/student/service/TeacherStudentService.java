package com.naenae.teacher.student.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.naenae.student.profile.domain.Student;
import com.naenae.student.profile.repository.StudentRepository;
import com.naenae.teacher.course.domain.Course;
import com.naenae.teacher.course.domain.CourseStudent;
import com.naenae.teacher.course.repository.CourseRepository;
import com.naenae.teacher.course.repository.CourseStudentRepository;
import com.naenae.teacher.profile.domain.Teacher;
import com.naenae.teacher.profile.repository.TeacherRepository;
import com.naenae.teacher.student.model.CourseOption;
import com.naenae.teacher.student.model.StudentListItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TeacherStudentService {

    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final CourseStudentRepository courseStudentRepository;

    public TeacherStudentService(
            TeacherRepository teacherRepository,
            StudentRepository studentRepository,
            CourseRepository courseRepository,
            CourseStudentRepository courseStudentRepository
    ) {
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.courseStudentRepository = courseStudentRepository;
    }

    @Transactional(readOnly = true)
    public List<CourseOption> getCourseOptions(Long teacherUserId) {
        Teacher teacher = getTeacher(teacherUserId);
        return courseRepository.findByTeacherIdOrderByTitleAsc(teacher.getId()).stream()
                .map(course -> new CourseOption(course.getId(), course.getTitle()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StudentListItem> getStudents(Long teacherUserId, Long courseId) {
        Teacher teacher = getTeacher(teacherUserId);
        List<CourseStudent> mappings = courseId == null
                ? courseStudentRepository.findByStudentTeacherIdOrderByCourseTitleAscStudentNameAsc(teacher.getId())
                : courseStudentRepository.findByCourseIdAndStudentTeacherIdOrderByStudentNameAsc(courseId, teacher.getId());

        Map<Long, StudentListItemBuilder> students = new LinkedHashMap<>();

        for (CourseStudent mapping : mappings) {
            Student student = mapping.getStudent();
            StudentListItemBuilder builder = students.computeIfAbsent(
                    student.getId(),
                    id -> new StudentListItemBuilder(student)
            );
            builder.courseNames.add(mapping.getCourse().getTitle());
        }

        return students.values().stream()
                .map(StudentListItemBuilder::build)
                .toList();
    }

    @Transactional
    public void createStudent(Long teacherUserId, String name, Long courseId, String schoolName, String phone) {
        Teacher teacher = getTeacher(teacherUserId);
        String normalizedName = requireText(name, "학생 이름을 입력해 주세요.");
        Course selectedCourse = resolveSelectedCourse(teacher.getId(), courseId);

        Student student = studentRepository.save(Student.create(
                teacher,
                normalizedName,
                normalizeOptional(schoolName),
                normalizeOptional(phone)
        ));

        if (!courseStudentRepository.existsByCourseIdAndStudentId(selectedCourse.getId(), student.getId())) {
            courseStudentRepository.save(CourseStudent.create(selectedCourse, student));
        }
    }

    @Transactional
    public void deleteStudent(Long teacherUserId, Long studentId) {
        Teacher teacher = getTeacher(teacherUserId);
        Student student = studentRepository.findByIdAndTeacherId(studentId, teacher.getId())
                .orElseThrow(() -> new IllegalArgumentException("삭제할 학생을 찾을 수 없습니다."));
        studentRepository.delete(student);
    }

    private Teacher getTeacher(Long teacherUserId) {
        return teacherRepository.findByUserId(teacherUserId)
                .orElseThrow(() -> new IllegalStateException("선생님 정보를 찾을 수 없습니다."));
    }

    private Course resolveSelectedCourse(Long teacherId, Long courseId) {
        if (courseId == null) {
            throw new IllegalArgumentException("반을 1개 선택해 주세요.");
        }

        return courseRepository.findByIdAndTeacherId(courseId, teacherId)
                .orElseThrow(() -> new IllegalArgumentException("선택한 반은 등록된 반이 아닙니다."));
    }

    private String requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private String normalizeOptional(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private static class StudentListItemBuilder {
        private final Student student;
        private final List<String> courseNames = new ArrayList<>();

        private StudentListItemBuilder(Student student) {
            this.student = student;
        }

        private StudentListItem build() {
            return new StudentListItem(
                    student.getId(),
                    student.getName(),
                    student.getSchoolName(),
                    student.getPhone(),
                    courseNames
            );
        }
    }
}
