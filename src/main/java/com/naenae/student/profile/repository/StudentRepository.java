package com.naenae.student.profile.repository;

import java.util.List;

import com.naenae.student.profile.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findByTeacherId(Long teacherId);

    List<Student> findByTeacherIdAndNameContainingIgnoreCase(Long teacherId, String name);
}
