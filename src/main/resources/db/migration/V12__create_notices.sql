CREATE TABLE notices (
    id BIGSERIAL PRIMARY KEY,
    teacher_id BIGINT NOT NULL REFERENCES teachers(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    content_html TEXT NOT NULL,
    target_all BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE notice_courses (
    id BIGSERIAL PRIMARY KEY,
    notice_id BIGINT NOT NULL REFERENCES notices(id) ON DELETE CASCADE,
    course_id BIGINT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    CONSTRAINT uk_notice_courses_notice_course UNIQUE (notice_id, course_id)
);

CREATE TABLE notice_attachments (
    id BIGSERIAL PRIMARY KEY,
    notice_id BIGINT NOT NULL REFERENCES notices(id) ON DELETE CASCADE,
    original_name VARCHAR(255) NOT NULL,
    stored_name VARCHAR(255) NOT NULL UNIQUE,
    content_type VARCHAR(150),
    file_size BIGINT NOT NULL
);

CREATE INDEX idx_notices_teacher_created ON notices(teacher_id, created_at DESC, id DESC);
CREATE INDEX idx_notice_courses_course ON notice_courses(course_id, notice_id);
CREATE INDEX idx_notice_attachments_notice ON notice_attachments(notice_id);