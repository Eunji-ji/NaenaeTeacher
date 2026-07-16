package com.naenae.teacher.classprogress.repository;

import com.naenae.teacher.classprogress.domain.ClassProgressNote;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassProgressNoteRepository extends JpaRepository<ClassProgressNote, Long> {
    Page<ClassProgressNote> findByTeacherIdOrderByCreatedAtDescIdDesc(Long teacherId, Pageable pageable);
    Optional<ClassProgressNote> findByIdAndTeacherId(Long id, Long teacherId);
}
