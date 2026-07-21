package com.naenae.teacher.wordtest.repository;

import com.naenae.teacher.wordtest.domain.WordTest;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WordTestRepository extends JpaRepository<WordTest, Long> {
    Page<WordTest> findByTeacherIdOrderByCreatedAtDescIdDesc(Long teacherId, Pageable pageable);

    Optional<WordTest> findByIdAndTeacherId(Long id, Long teacherId);

    @Query("""
            select distinct wt from WordTest wt
            join wt.courses mapping
            where mapping.course.id in :courseIds
              and wt.startDate <= :date
              and wt.endDate >= :date
            order by wt.startDate desc, wt.id desc
            """)
    List<WordTest> findVisibleTests(
            @Param("courseIds") Collection<Long> courseIds,
            @Param("date") LocalDate date
    );

    @Query(
            value = """
                    select distinct wt from WordTest wt
                    join wt.courses mapping
                    where mapping.course.id in :courseIds
                    order by wt.startDate desc, wt.id desc
                    """,
            countQuery = """
                    select count(distinct wt.id) from WordTest wt
                    join wt.courses mapping
                    where mapping.course.id in :courseIds
                    """
    )
    Page<WordTest> findStudentTests(@Param("courseIds") Collection<Long> courseIds, Pageable pageable);
}
