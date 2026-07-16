CREATE TABLE class_progress_notes (
    id BIGSERIAL PRIMARY KEY,
    teacher_id BIGINT NOT NULL REFERENCES teachers (id) ON DELETE CASCADE,
    course_id BIGINT REFERENCES courses (id) ON DELETE SET NULL,
    class_schedule_id BIGINT REFERENCES class_schedules (id) ON DELETE SET NULL,
    course_title VARCHAR(150),
    lesson_title VARCHAR(150),
    memo VARCHAR(1000) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_class_progress_notes_memo CHECK (char_length(btrim(memo)) BETWEEN 1 AND 1000)
);

CREATE INDEX idx_class_progress_notes_teacher_created
    ON class_progress_notes (teacher_id, created_at DESC, id DESC);

