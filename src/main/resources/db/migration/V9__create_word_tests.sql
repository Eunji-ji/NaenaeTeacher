CREATE TABLE word_tests (
    id BIGSERIAL PRIMARY KEY,
    teacher_id BIGINT NOT NULL REFERENCES teachers(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE word_test_courses (
    id BIGSERIAL PRIMARY KEY,
    word_test_id BIGINT NOT NULL REFERENCES word_tests(id) ON DELETE CASCADE,
    course_id BIGINT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    UNIQUE (word_test_id, course_id)
);

CREATE TABLE word_test_words (
    id BIGSERIAL PRIMARY KEY,
    word_test_id BIGINT NOT NULL REFERENCES word_tests(id) ON DELETE CASCADE,
    display_order INTEGER NOT NULL,
    word VARCHAR(150) NOT NULL,
    meaning VARCHAR(300),
    UNIQUE (word_test_id, display_order)
);

CREATE INDEX idx_word_tests_teacher_id ON word_tests(teacher_id);
CREATE INDEX idx_word_test_courses_course_id ON word_test_courses(course_id);
CREATE INDEX idx_word_test_words_test_id ON word_test_words(word_test_id);
