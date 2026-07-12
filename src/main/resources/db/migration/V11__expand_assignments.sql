ALTER TABLE assignments ADD COLUMN start_date DATE;
ALTER TABLE assignments ADD COLUMN end_date DATE;
ALTER TABLE assignments ADD COLUMN content_html TEXT;

UPDATE assignments
SET start_date = COALESCE(created_at::date, CURRENT_DATE),
    end_date = COALESCE(due_date, created_at::date, CURRENT_DATE),
    content_html = COALESCE(description, '')
WHERE start_date IS NULL OR end_date IS NULL OR content_html IS NULL;

UPDATE assignments SET end_date = start_date WHERE end_date < start_date;
ALTER TABLE assignments ALTER COLUMN start_date SET NOT NULL;
ALTER TABLE assignments ALTER COLUMN end_date SET NOT NULL;
ALTER TABLE assignments ALTER COLUMN content_html SET NOT NULL;
ALTER TABLE assignments ADD CONSTRAINT chk_assignments_period CHECK (end_date >= start_date);

CREATE TABLE assignment_courses (
    id BIGSERIAL PRIMARY KEY,
    assignment_id BIGINT NOT NULL REFERENCES assignments(id) ON DELETE CASCADE,
    course_id BIGINT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    UNIQUE (assignment_id, course_id)
);

INSERT INTO assignment_courses (assignment_id, course_id)
SELECT id, course_id FROM assignments WHERE course_id IS NOT NULL;

CREATE TABLE assignment_attachments (
    id BIGSERIAL PRIMARY KEY,
    assignment_id BIGINT NOT NULL REFERENCES assignments(id) ON DELETE CASCADE,
    original_name VARCHAR(255) NOT NULL,
    stored_name VARCHAR(255) NOT NULL UNIQUE,
    content_type VARCHAR(150),
    file_size BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_assignment_courses_course ON assignment_courses(course_id);
CREATE INDEX idx_assignments_period ON assignments(start_date, end_date);
