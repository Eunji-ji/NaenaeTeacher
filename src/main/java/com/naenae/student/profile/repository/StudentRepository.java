package com.naenae.student.profile.repository;

import java.util.List;
import java.util.Optional;

import com.naenae.student.profile.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findByTeacherId(Long teacherId);

    List<Student> findByTeacherIdOrderByNameAsc(Long teacherId);

    List<Student> findByTeacherIdAndNameContainingIgnoreCase(Long teacherId, String name);

    Optional<Student> findByIdAndTeacherId(Long id, Long teacherId);
}
