package com.naenae.teacher.todayenglish.model;

import com.naenae.common.vocabulary.domain.WordLevel;

public record TodayEnglishSentenceItem(Long id, WordLevel level, String levelLabel,
                                       String sentence, String meaning) {
}

