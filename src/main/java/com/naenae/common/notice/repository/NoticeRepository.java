package com.naenae.common.notice.repository;

import com.naenae.common.notice.domain.Notice;
import java.util.Collection;
import java.util.Optional;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    Page<Notice> findByTeacherIdOrderByCreatedAtDescIdDesc(Long teacherId, Pageable pageable);
    Optional<Notice> findByIdAndTeacherId(Long noticeId, Long teacherId);
    Optional<Notice> findFirstByTeacherIdAndPublishStartDateLessThanEqualAndPublishEndDateGreaterThanEqualOrderByCreatedAtDescIdDesc(
            Long teacherId, LocalDate startDate, LocalDate endDate);

    @Query("""
            select distinct notice from Notice notice
            left join notice.courses mapping
            where notice.teacher.id = :teacherId
              and (notice.targetAll = true or mapping.course.id in :courseIds)
            order by notice.createdAt desc, notice.id desc
            """)
    Page<Notice> findVisibleToStudent(
            @Param("teacherId") Long teacherId,
            @Param("courseIds") Collection<Long> courseIds,
            Pageable pageable
    );

    @Query("""
            select distinct notice from Notice notice
            left join notice.courses mapping
            where notice.teacher.id = :teacherId
              and notice.publishStartDate <= :date
              and notice.publishEndDate >= :date
              and (notice.targetAll = true or mapping.course.id in :courseIds)
            order by notice.createdAt desc, notice.id desc
            """)
    Page<Notice> findVisibleToStudentOnDate(
            @Param("teacherId") Long teacherId,
            @Param("courseIds") Collection<Long> courseIds,
            @Param("date") LocalDate date,
            Pageable pageable
    );
}
