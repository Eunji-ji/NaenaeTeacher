package com.naenae.teacher.profile.repository;

import java.util.Optional;

import com.naenae.teacher.profile.domain.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    Optional<Teacher> findByUserId(Long userId);
}
