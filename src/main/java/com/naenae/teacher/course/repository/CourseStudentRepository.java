package com.naenae.teacher.course.repository;

import java.util.List;

import com.naenae.teacher.course.domain.CourseStudent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseStudentRepository extends JpaRepository<CourseStudent, Long> {
    List<CourseStudent> findByStudentTeacherIdOrderByCourseTitleAscStudentNameAsc(Long teacherId);

    List<CourseStudent> findByCourseIdAndStudentTeacherIdOrderByStudentNameAsc(Long courseId, Long teacherId);

    boolean existsByCourseIdAndStudentId(Long courseId, Long studentId);
}
