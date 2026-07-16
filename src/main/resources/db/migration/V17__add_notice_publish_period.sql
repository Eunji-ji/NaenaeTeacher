ALTER TABLE notices
    ADD COLUMN publish_start_date DATE NOT NULL DEFAULT CURRENT_DATE,
    ADD COLUMN publish_end_date DATE NOT NULL DEFAULT CURRENT_DATE;

ALTER TABLE notices
    ADD CONSTRAINT chk_notices_publish_period CHECK (publish_end_date >= publish_start_date);

CREATE INDEX idx_notices_teacher_publish_period
    ON notices (teacher_id, publish_start_date, publish_end_date, created_at DESC);
