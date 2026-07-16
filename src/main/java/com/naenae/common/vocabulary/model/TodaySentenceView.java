package com.naenae.common.vocabulary.model;

import com.naenae.common.vocabulary.domain.WordLevel;

public record TodaySentenceView(
        WordLevel level,
        String levelLabel,
        String sentence,
        String meaning
) {
}
