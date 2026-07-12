CREATE TABLE today_sentences (
    id BIGSERIAL PRIMARY KEY,
    level VARCHAR(30) NOT NULL,
    sentence TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_today_sentences_level_sentence UNIQUE (level, sentence),
    CONSTRAINT ck_today_sentences_level CHECK (level IN ('LOWER_ELEMENTARY', 'UPPER_ELEMENTARY', 'MIDDLE_SCHOOL'))
);

CREATE TABLE today_sentence_selections (
    id BIGSERIAL PRIMARY KEY,
    selection_date DATE NOT NULL,
    level VARCHAR(30) NOT NULL,
    today_sentence_id BIGINT NOT NULL REFERENCES today_sentences (id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_today_sentence_selections_date_level UNIQUE (selection_date, level),
    CONSTRAINT ck_today_sentence_selections_level CHECK (level IN ('LOWER_ELEMENTARY', 'UPPER_ELEMENTARY', 'MIDDLE_SCHOOL'))
);

CREATE INDEX idx_today_sentences_level ON today_sentences(level);
CREATE INDEX idx_today_sentence_selections_date_level ON today_sentence_selections(selection_date, level);

WITH
lower_subjects AS (
    SELECT * FROM unnest(ARRAY[
        'A kind heart',
        'A brave smile',
        'A good friend',
        'A bright mind',
        'A small step',
        'A happy learner',
        'A calm voice',
        'A helpful hand',
        'A warm word',
        'A new try'
    ]) AS t(value)
),
lower_verbs AS (
    SELECT * FROM unnest(ARRAY[
        'can make',
        'can help',
        'can start',
        'can bring',
        'can build',
        'can find',
        'can share',
        'can learn',
        'can grow',
        'can change'
    ]) AS t(value)
),
lower_endings AS (
    SELECT * FROM unnest(ARRAY[
        'a better day.',
        'a happy class.',
        'a strong dream.',
        'a gentle answer.',
        'a clear idea.',
        'a good habit.',
        'a peaceful moment.',
        'a friendly world.',
        'a hopeful morning.',
        'a wise choice.'
    ]) AS t(value)
),
upper_subjects AS (
    SELECT * FROM unnest(ARRAY[
        'A curious student',
        'A patient learner',
        'A careful reader',
        'A generous friend',
        'A focused mind',
        'A steady effort',
        'A thoughtful question',
        'A positive choice',
        'A respectful voice',
        'A quiet practice'
    ]) AS t(value)
),
upper_verbs AS (
    SELECT * FROM unnest(ARRAY[
        'turns mistakes into',
        'turns practice into',
        'turns questions into',
        'turns kindness into',
        'turns reading into',
        'turns patience into',
        'turns effort into',
        'turns courage into',
        'turns listening into',
        'turns small steps into'
    ]) AS t(value)
),
upper_endings AS (
    SELECT * FROM unnest(ARRAY[
        'stronger understanding.',
        'lasting confidence.',
        'brighter ideas.',
        'quiet leadership.',
        'useful knowledge.',
        'better judgment.',
        'real progress.',
        'new opportunities.',
        'deeper respect.',
        'meaningful success.'
    ]) AS t(value)
),
middle_subjects AS (
    SELECT * FROM unnest(ARRAY[
        'A disciplined mind',
        'A resilient learner',
        'A sincere question',
        'A thoughtful decision',
        'A consistent habit',
        'A humble attitude',
        'A courageous choice',
        'A reflective student',
        'A determined heart',
        'A responsible voice'
    ]) AS t(value)
),
middle_verbs AS (
    SELECT * FROM unnest(ARRAY[
        'transforms pressure into',
        'transforms failure into',
        'transforms curiosity into',
        'transforms patience into',
        'transforms discipline into',
        'transforms reflection into',
        'transforms responsibility into',
        'transforms courage into',
        'transforms honesty into',
        'transforms daily effort into'
    ]) AS t(value)
),
middle_endings AS (
    SELECT * FROM unnest(ARRAY[
        'lasting growth.',
        'clear wisdom.',
        'mature confidence.',
        'meaningful progress.',
        'strong character.',
        'deeper learning.',
        'better choices.',
        'quiet strength.',
        'valuable insight.',
        'future success.'
    ]) AS t(value)
)
INSERT INTO today_sentences (level, sentence, created_at, updated_at)
SELECT level, sentence, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM (
    SELECT 'LOWER_ELEMENTARY' AS level, s.value || ' ' || v.value || ' ' || e.value AS sentence
    FROM lower_subjects s
    CROSS JOIN lower_verbs v
    CROSS JOIN lower_endings e

    UNION ALL

    SELECT 'UPPER_ELEMENTARY' AS level, s.value || ' ' || v.value || ' ' || e.value AS sentence
    FROM upper_subjects s
    CROSS JOIN upper_verbs v
    CROSS JOIN upper_endings e

    UNION ALL

    SELECT 'MIDDLE_SCHOOL' AS level, s.value || ' ' || v.value || ' ' || e.value AS sentence
    FROM middle_subjects s
    CROSS JOIN middle_verbs v
    CROSS JOIN middle_endings e
) seed
ON CONFLICT (level, sentence) DO NOTHING;
