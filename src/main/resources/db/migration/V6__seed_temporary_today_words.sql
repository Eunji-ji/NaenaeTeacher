INSERT INTO today_words (level, word, sentence, created_at, updated_at)
SELECT
    seed.level,
    seed.word,
    seed.sentence,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM (
    SELECT
        'LOWER_ELEMENTARY' AS level,
        'lower_word_' || LPAD(series::TEXT, 4, '0') AS word,
        'I can learn lower_word_' || LPAD(series::TEXT, 4, '0') || ' today.' AS sentence
    FROM generate_series(1, 1000) AS series

    UNION ALL

    SELECT
        'UPPER_ELEMENTARY' AS level,
        'upper_word_' || LPAD(series::TEXT, 4, '0') AS word,
        'I can practice upper_word_' || LPAD(series::TEXT, 4, '0') || ' today.' AS sentence
    FROM generate_series(1, 1000) AS series

    UNION ALL

    SELECT
        'MIDDLE_SCHOOL' AS level,
        'middle_word_' || LPAD(series::TEXT, 4, '0') AS word,
        'I can remember middle_word_' || LPAD(series::TEXT, 4, '0') || ' today.' AS sentence
    FROM generate_series(1, 1000) AS series
) seed
WHERE NOT EXISTS (
    SELECT 1
    FROM today_words
)
ON CONFLICT (level, word) DO NOTHING;
