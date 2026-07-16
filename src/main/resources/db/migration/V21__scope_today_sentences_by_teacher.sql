DELETE FROM today_sentence_selections;
DELETE FROM today_sentences;

ALTER TABLE today_sentences DROP CONSTRAINT uk_today_sentences_level_sentence;
DROP INDEX IF EXISTS idx_today_sentences_level;

ALTER TABLE today_sentences
    ADD COLUMN teacher_id BIGINT REFERENCES teachers (id) ON DELETE CASCADE;
ALTER TABLE today_sentences ALTER COLUMN teacher_id SET NOT NULL;
ALTER TABLE today_sentences
    ADD CONSTRAINT uk_today_sentences_teacher_level_sentence UNIQUE (teacher_id, level, sentence);
CREATE INDEX idx_today_sentences_teacher_level ON today_sentences (teacher_id, level);

ALTER TABLE today_sentence_selections DROP CONSTRAINT uk_today_sentence_selections_date_level;
DROP INDEX IF EXISTS idx_today_sentence_selections_date_level;
ALTER TABLE today_sentence_selections
    ADD COLUMN teacher_id BIGINT REFERENCES teachers (id) ON DELETE CASCADE;
ALTER TABLE today_sentence_selections ALTER COLUMN teacher_id SET NOT NULL;
ALTER TABLE today_sentence_selections
    ADD CONSTRAINT uk_today_sentence_selections_teacher_date_level
    UNIQUE (teacher_id, selection_date, level);
CREATE INDEX idx_today_sentence_selections_teacher_date_level
    ON today_sentence_selections (teacher_id, selection_date, level);
