package com.naenae.teacher.course.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.naenae.teacher.course.domain.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByTeacherIdOrderByTitleAsc(Long teacherId);

    List<Course> findByTeacherIdAndIdInOrderByTitleAsc(Long teacherId, Collection<Long> ids);

    Optional<Course> findFirstByTeacherIdAndTitleIgnoreCase(Long teacherId, String title);
}
