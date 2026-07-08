CREATE TABLE student_academic_records (
    id BIGSERIAL PRIMARY KEY,
    teacher_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    exam_year INTEGER NOT NULL,
    exam_type VARCHAR(20) NOT NULL,
    score INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_student_academic_records_teacher FOREIGN KEY (teacher_id) REFERENCES teachers (id),
    CONSTRAINT fk_student_academic_records_student FOREIGN KEY (student_id) REFERENCES students (id),
    CONSTRAINT uk_student_academic_record_year_type UNIQUE (student_id, exam_year, exam_type)
);
