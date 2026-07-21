CREATE TABLE weekly_tests (
    id BIGSERIAL PRIMARY KEY,
    teacher_id BIGINT NOT NULL REFERENCES teachers(id),
    course_id BIGINT NOT NULL REFERENCES courses(id),
    name VARCHAR(250) NOT NULL,
    remarks TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE weekly_test_attachments (
    id BIGSERIAL PRIMARY KEY,
    weekly_test_id BIGINT NOT NULL REFERENCES weekly_tests(id) ON DELETE CASCADE,
    original_name VARCHAR(255) NOT NULL,
    stored_name VARCHAR(255) NOT NULL UNIQUE,
    content_type VARCHAR(150),
    file_size BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE weekly_test_scores (
    id BIGSERIAL PRIMARY KEY,
    weekly_test_id BIGINT NOT NULL REFERENCES weekly_tests(id) ON DELETE CASCADE,
    student_id BIGINT NOT NULL REFERENCES students(id),
    score INTEGER CHECK (score BETWEEN 0 AND 100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_weekly_test_scores_test_student UNIQUE (weekly_test_id, student_id)
);

CREATE INDEX idx_weekly_tests_teacher_created ON weekly_tests(teacher_id, created_at DESC);
CREATE INDEX idx_weekly_test_scores_student ON weekly_test_scores(student_id, weekly_test_id);
