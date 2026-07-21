package com.naenae.teacher.weeklytest.repository;

import com.naenae.teacher.weeklytest.domain.WeeklyTest;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeeklyTestRepository extends JpaRepository<WeeklyTest, Long> {
    Page<WeeklyTest> findByTeacherIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtDescIdDesc(
            Long teacherId, LocalDateTime from, LocalDateTime to, Pageable pageable);

    Optional<WeeklyTest> findByIdAndTeacherId(Long id, Long teacherId);

    Page<WeeklyTest> findDistinctByScoresStudentIdOrderByCreatedAtDescIdDesc(Long studentId, Pageable pageable);

    Optional<WeeklyTest> findDistinctByIdAndScoresStudentId(Long id, Long studentId);
}
