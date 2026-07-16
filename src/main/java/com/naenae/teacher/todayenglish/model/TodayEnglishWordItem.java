package com.naenae.teacher.todayenglish.model;

import com.naenae.common.vocabulary.domain.WordLevel;

public record TodayEnglishWordItem(
        Long id,
        WordLevel level,
        String levelLabel,
        String word,
        String meaning
) {
}
