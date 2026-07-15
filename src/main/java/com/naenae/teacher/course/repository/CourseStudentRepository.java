package com.naenae.teacher.course.repository;

import java.util.List;

import com.naenae.teacher.course.domain.CourseStudent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseStudentRepository extends JpaRepository<CourseStudent, Long> {
    List<CourseStudent> findByStudentTeacherIdOrderByCourseTitleAscStudentNameAsc(Long teacherId);

    List<CourseStudent> findByCourseIdAndStudentTeacherIdOrderByStudentNameAsc(Long courseId, Long teacherId);

    List<CourseStudent> findByStudent_IdOrderByCourseTitleAsc(Long studentId);

    boolean existsByCourseIdAndStudentId(Long courseId, Long studentId);

    @Query("select count(mapping) from CourseStudent mapping where mapping.course.teacher.id = :teacherId and mapping.course.active = true")
    long countActiveEnrollmentsByTeacherId(@Param("teacherId") Long teacherId);
}
