package com.naenae.common.vocabulary.repository;

import java.util.List;

import com.naenae.common.vocabulary.domain.TodayWord;
import com.naenae.common.vocabulary.domain.WordLevel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodayWordRepository extends JpaRepository<TodayWord, Long> {
    long countByLevel(WordLevel level);

    List<TodayWord> findByLevelOrderByWordAsc(WordLevel level);
}
