package com.naenae.common.vocabulary.repository;

import java.util.List;
import java.util.Optional;

import com.naenae.common.vocabulary.domain.TodayWord;
import com.naenae.common.vocabulary.domain.WordLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TodayWordRepository extends JpaRepository<TodayWord, Long> {
    long countByTeacherIdAndLevel(Long teacherId, WordLevel level);

    List<TodayWord> findByTeacherIdAndLevelOrderByWordAsc(Long teacherId, WordLevel level);

    void deleteByTeacherId(Long teacherId);

    Page<TodayWord> findByTeacherIdOrderByLevelAscWordAsc(Long teacherId, Pageable pageable);

    Page<TodayWord> findByTeacherIdAndLevelOrderByWordAsc(Long teacherId, WordLevel level, Pageable pageable);

    Optional<TodayWord> findByIdAndTeacherId(Long id, Long teacherId);
}
