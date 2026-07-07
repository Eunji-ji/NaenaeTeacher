package com.naenae.teacher.assignment.repository;

import com.naenae.teacher.assignment.domain.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
}
