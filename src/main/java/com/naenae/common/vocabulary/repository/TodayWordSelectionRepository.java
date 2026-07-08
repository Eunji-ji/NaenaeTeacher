package com.naenae.common.vocabulary.repository;

import java.time.LocalDate;
import java.util.Optional;

import com.naenae.common.vocabulary.domain.TodayWordSelection;
import com.naenae.common.vocabulary.domain.WordLevel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodayWordSelectionRepository extends JpaRepository<TodayWordSelection, Long> {
    Optional<TodayWordSelection> findBySelectionDateAndLevel(LocalDate selectionDate, WordLevel level);
}
