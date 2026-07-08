CREATE TABLE today_words (
    id BIGSERIAL PRIMARY KEY,
    level VARCHAR(30) NOT NULL,
    word VARCHAR(120) NOT NULL,
    sentence TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_today_words_level_word UNIQUE (level, word),
    CONSTRAINT ck_today_words_level CHECK (level IN ('LOWER_ELEMENTARY', 'UPPER_ELEMENTARY', 'MIDDLE_SCHOOL'))
);

CREATE TABLE today_word_selections (
    id BIGSERIAL PRIMARY KEY,
    selection_date DATE NOT NULL,
    level VARCHAR(30) NOT NULL,
    today_word_id BIGINT NOT NULL REFERENCES today_words (id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_today_word_selections_date_level UNIQUE (selection_date, level),
    CONSTRAINT ck_today_word_selections_level CHECK (level IN ('LOWER_ELEMENTARY', 'UPPER_ELEMENTARY', 'MIDDLE_SCHOOL'))
);

CREATE INDEX idx_today_words_level ON today_words(level);
CREATE INDEX idx_today_word_selections_date_level ON today_word_selections(selection_date, level);
