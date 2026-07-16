DELETE FROM today_word_selections;
DELETE FROM today_words;

ALTER TABLE today_words
    DROP CONSTRAINT uk_today_words_level_word,
    DROP COLUMN sentence,
    ADD COLUMN teacher_id BIGINT NOT NULL REFERENCES teachers (id) ON DELETE CASCADE;

ALTER TABLE today_words
    ADD CONSTRAINT uk_today_words_teacher_level_word UNIQUE (teacher_id, level, word);

DROP INDEX idx_today_words_level;
CREATE INDEX idx_today_words_teacher_level ON today_words (teacher_id, level);

ALTER TABLE today_word_selections
    DROP CONSTRAINT uk_today_word_selections_date_level,
    ADD COLUMN teacher_id BIGINT NOT NULL REFERENCES teachers (id) ON DELETE CASCADE;

ALTER TABLE today_word_selections
    ADD CONSTRAINT uk_today_word_selections_teacher_date_level UNIQUE (teacher_id, selection_date, level);

DROP INDEX idx_today_word_selections_date_level;
CREATE INDEX idx_today_word_selections_teacher_date_level
    ON today_word_selections (teacher_id, selection_date, level);
