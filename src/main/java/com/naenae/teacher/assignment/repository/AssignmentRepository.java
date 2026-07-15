package com.naenae.teacher.assignment.repository;

import com.naenae.teacher.assignment.domain.Assignment;
import com.naenae.teacher.assignment.domain.AssignmentStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    @Query(value = """
            select * from assignments assignment
            where assignment.teacher_id = :teacherId
              and (:inProgressOnly = false or assignment.status = 'IN_PROGRESS')
            order by case assignment.status when 'IN_PROGRESS' then 0 when 'SCHEDULED' then 1 else 2 end,
                     assignment.created_at desc, assignment.id desc
            """, countQuery = """
            select count(*) from assignments assignment
            where assignment.teacher_id = :teacherId
              and (:inProgressOnly = false or assignment.status = 'IN_PROGRESS')
            """, nativeQuery = true)
    Page<Assignment> findTeacherAssignments(@Param("teacherId") Long teacherId,
                                            @Param("inProgressOnly") boolean inProgressOnly,
                                            Pageable pageable);

    @Query("""
            select distinct assignment from Assignment assignment
            join assignment.courses mapping
            join CourseStudent enrollment on enrollment.course.id = mapping.course.id
            where enrollment.student.id = :studentId and assignment.status = :status
            order by assignment.createdAt desc, assignment.id desc
            """)
    Page<Assignment> findStudentAssignments(@Param("studentId") Long studentId,
                                            @Param("status") AssignmentStatus status,
                                            Pageable pageable);

    @Query("""
            select distinct assignment from Assignment assignment
            join assignment.courses mapping
            join CourseStudent enrollment on enrollment.course.id = mapping.course.id
            where assignment.id = :assignmentId
              and enrollment.student.id = :studentId
              and assignment.status = :status
            """)
    Optional<Assignment> findStudentVisibleAssignment(@Param("assignmentId") Long assignmentId,
                                                      @Param("studentId") Long studentId,
                                                      @Param("status") AssignmentStatus status);
    Optional<Assignment> findByIdAndTeacherId(Long assignmentId, Long teacherId);
    long countByTeacherIdAndStatus(Long teacherId, AssignmentStatus status);
}