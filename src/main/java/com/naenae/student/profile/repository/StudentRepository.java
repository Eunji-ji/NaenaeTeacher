package com.naenae.student.profile.repository;

import java.util.List;
import java.util.Optional;

import com.naenae.student.profile.domain.Student;
import com.naenae.teacher.profile.domain.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findByTeacherId(Long teacherId);

    List<Student> findByTeacherIdOrderByNameAsc(Long teacherId);

    List<Student> findByTeacherIdAndNameContainingIgnoreCase(Long teacherId, String name);

    Optional<Student> findByIdAndTeacherId(Long id, Long teacherId);

    Optional<Student> findByUserId(Long userId);

    List<Student> findByTeacherAndNameAndPhoneAndUserIsNull(Teacher teacher, String name, String phone);

    long countByTeacherId(Long teacherId);
}
