package com.naenae.teacher.course.repository;

import com.naenae.teacher.course.domain.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {
}
