package com.naenae.teacher.assignment.repository;

import com.naenae.teacher.assignment.domain.Assignment;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    Page<Assignment> findByTeacherIdOrderByCreatedAtDescIdDesc(Long teacherId, Pageable pageable);

    Optional<Assignment> findByIdAndTeacherId(Long assignmentId, Long teacherId);
}