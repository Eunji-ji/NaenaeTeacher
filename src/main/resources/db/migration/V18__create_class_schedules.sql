CREATE TABLE class_schedules (
    id BIGSERIAL PRIMARY KEY,
    teacher_id BIGINT NOT NULL REFERENCES teachers (id) ON DELETE CASCADE,
    course_id BIGINT NOT NULL REFERENCES courses (id) ON DELETE CASCADE,
    weekday VARCHAR(20) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    lesson_title VARCHAR(150) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_class_schedules_weekday CHECK (weekday IN ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY')),
    CONSTRAINT chk_class_schedules_time CHECK (end_time > start_time)
);

CREATE INDEX idx_class_schedules_teacher_weekday_time
    ON class_schedules (teacher_id, weekday, start_time, end_time);

CREATE INDEX idx_class_schedules_course_id ON class_schedules (course_id);
