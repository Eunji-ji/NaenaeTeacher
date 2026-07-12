package com.naenae.common.vocabulary.repository;

import java.util.List;

import com.naenae.common.vocabulary.domain.TodaySentence;
import com.naenae.common.vocabulary.domain.WordLevel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodaySentenceRepository extends JpaRepository<TodaySentence, Long> {
    List<TodaySentence> findByLevelOrderBySentenceAsc(WordLevel level);
}
