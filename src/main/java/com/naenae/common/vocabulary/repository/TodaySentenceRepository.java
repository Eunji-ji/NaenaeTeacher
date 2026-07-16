package com.naenae.common.vocabulary.repository;

import java.util.List;

import com.naenae.common.vocabulary.domain.TodaySentence;
import com.naenae.common.vocabulary.domain.WordLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TodaySentenceRepository extends JpaRepository<TodaySentence, Long> {
    List<TodaySentence> findByTeacherIdAndLevelOrderBySentenceAsc(Long teacherId, WordLevel level);
    Page<TodaySentence> findByTeacherIdOrderByLevelAscSentenceAsc(Long teacherId, Pageable pageable);
    Page<TodaySentence> findByTeacherIdAndLevelOrderBySentenceAsc(Long teacherId, WordLevel level, Pageable pageable);
    Optional<TodaySentence> findByIdAndTeacherId(Long id, Long teacherId);
    long countByTeacherIdAndLevel(Long teacherId, WordLevel level);
    long deleteByTeacherId(Long teacherId);
}
