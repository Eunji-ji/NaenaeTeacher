package com.naenae.common.vocabulary.model;

import com.naenae.common.vocabulary.domain.WordLevel;

public record TodayWordView(
        WordLevel level,
        String levelLabel,
        String word,
        String sentence
) {
}
