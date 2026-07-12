package com.naenae.common.vocabulary.repository;

import java.time.LocalDate;
import java.util.Optional;

import com.naenae.common.vocabulary.domain.TodaySentenceSelection;
import com.naenae.common.vocabulary.domain.WordLevel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodaySentenceSelectionRepository extends JpaRepository<TodaySentenceSelection, Long> {
    Optional<TodaySentenceSelection> findBySelectionDateAndLevel(LocalDate selectionDate, WordLevel level);
}
