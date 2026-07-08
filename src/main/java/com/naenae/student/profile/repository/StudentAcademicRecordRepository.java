package com.naenae.student.profile.repository;

import java.util.List;
import java.util.Optional;

import com.naenae.student.profile.domain.AcademicExamType;
import com.naenae.student.profile.domain.StudentAcademicRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentAcademicRecordRepository extends JpaRepository<StudentAcademicRecord, Long> {
    List<StudentAcademicRecord> findByStudentIdAndStudentTeacherIdOrderByExamYearAscExamTypeAsc(Long studentId, Long teacherId);

    Optional<StudentAcademicRecord> findByStudentIdAndStudentTeacherIdAndExamYearAndExamType(
            Long studentId,
            Long teacherId,
            Integer examYear,
            AcademicExamType examType
    );
}
