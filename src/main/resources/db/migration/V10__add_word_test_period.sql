ALTER TABLE word_tests ADD COLUMN start_date DATE;
ALTER TABLE word_tests ADD COLUMN end_date DATE;

UPDATE word_tests
SET start_date = CURRENT_DATE,
    end_date = CURRENT_DATE
WHERE start_date IS NULL OR end_date IS NULL;

ALTER TABLE word_tests ALTER COLUMN start_date SET NOT NULL;
ALTER TABLE word_tests ALTER COLUMN end_date SET NOT NULL;
ALTER TABLE word_tests ADD CONSTRAINT chk_word_tests_period CHECK (end_date >= start_date);

CREATE INDEX idx_word_tests_period ON word_tests(start_date, end_date);
