package com.naenae.teacher.course.service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.naenae.teacher.course.domain.Course;
import com.naenae.teacher.course.repository.CourseRepository;
import com.naenae.teacher.profile.domain.Teacher;
import com.naenae.teacher.profile.repository.TeacherRepository;
import com.naenae.teacher.student.model.CourseOption;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TeacherCourseService {

    private final TeacherRepository teacherRepository;
    private final CourseRepository courseRepository;

    public TeacherCourseService(TeacherRepository teacherRepository, CourseRepository courseRepository) {
        this.teacherRepository = teacherRepository;
        this.courseRepository = courseRepository;
    }

    @Transactional(readOnly = true)
    public List<CourseOption> getCourses(Long teacherUserId) {
        Teacher teacher = getTeacher(teacherUserId);
        return courseRepository.findByTeacherIdOrderByTitleAsc(teacher.getId()).stream()
                .map(course -> new CourseOption(course.getId(), course.getTitle()))
                .toList();
    }

    @Transactional
    public int createCourses(Long teacherUserId, String courseTitles) {
        Teacher teacher = getTeacher(teacherUserId);
        List<String> titles = parseCourseTitles(courseTitles);
        int createdCount = 0;

        for (String title : titles) {
            boolean exists = courseRepository.findFirstByTeacherIdAndTitleIgnoreCase(teacher.getId(), title).isPresent();
            if (exists) {
                continue;
            }
            courseRepository.save(Course.create(teacher, title));
            createdCount++;
        }

        return createdCount;
    }

    private Teacher getTeacher(Long teacherUserId) {
        return teacherRepository.findByUserId(teacherUserId)
                .orElseThrow(() -> new IllegalStateException("선생님 정보를 찾을 수 없습니다."));
    }

    private List<String> parseCourseTitles(String courseTitles) {
        if (courseTitles == null) {
            throw new IllegalArgumentException("반 이름을 1개 이상 입력해 주세요.");
        }

        List<String> titles = Arrays.stream(courseTitles.split("[\\r\\n,]+"))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (titles.isEmpty()) {
            throw new IllegalArgumentException("반 이름을 1개 이상 입력해 주세요.");
        }
        return titles;
    }
}