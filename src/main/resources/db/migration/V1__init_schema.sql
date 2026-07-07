CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('TEACHER', 'STUDENT', 'ADMIN')),
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(30),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE teachers (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id),
    academy_name VARCHAR(150),
    subject_name VARCHAR(100),
    introduction TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE students (
    id BIGSERIAL PRIMARY KEY,
    teacher_id BIGINT NOT NULL REFERENCES teachers(id),
    user_id BIGINT UNIQUE REFERENCES users(id),
    name VARCHAR(100) NOT NULL,
    school_name VARCHAR(150),
    grade VARCHAR(50),
    phone VARCHAR(30),
    parent_phone VARCHAR(30),
    memo_summary TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
        CHECK (status IN ('ACTIVE', 'PAUSED', 'ENDED')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE courses (
    id BIGSERIAL PRIMARY KEY,
    teacher_id BIGINT NOT NULL REFERENCES teachers(id),
    title VARCHAR(150) NOT NULL,
    description TEXT,
    day_of_week VARCHAR(20),
    start_time TIME,
    end_time TIME,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE course_students (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    student_id BIGINT NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    joined_at DATE NOT NULL DEFAULT CURRENT_DATE,
    UNIQUE (course_id, student_id)
);

CREATE TABLE assignments (
    id BIGSERIAL PRIMARY KEY,
    teacher_id BIGINT NOT NULL REFERENCES teachers(id),
    course_id BIGINT REFERENCES courses(id),
    title VARCHAR(200) NOT NULL,
    description TEXT,
    due_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN'
        CHECK (status IN ('OPEN', 'CLOSED')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE student_assignment_status (
    id BIGSERIAL PRIMARY KEY,
    assignment_id BIGINT NOT NULL REFERENCES assignments(id) ON DELETE CASCADE,
    student_id BIGINT NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED'
        CHECK (status IN ('NOT_STARTED', 'IN_PROGRESS', 'DONE', 'LATE')),
    submitted_at TIMESTAMP,
    teacher_comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (assignment_id, student_id)
);

CREATE TABLE attendance (
    id BIGSERIAL PRIMARY KEY,
    teacher_id BIGINT NOT NULL REFERENCES teachers(id),
    course_id BIGINT REFERENCES courses(id),
    student_id BIGINT NOT NULL REFERENCES students(id),
    attendance_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL
        CHECK (status IN ('PRESENT', 'ABSENT', 'LATE', 'EXCUSED')),
    study_status VARCHAR(20)
        CHECK (study_status IN ('GOOD', 'NORMAL', 'NEEDS_ATTENTION')),
    note TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (student_id, attendance_date, course_id)
);

CREATE TABLE teacher_memos (
    id BIGSERIAL PRIMARY KEY,
    teacher_id BIGINT NOT NULL REFERENCES teachers(id),
    student_id BIGINT NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    visibility VARCHAR(20) NOT NULL DEFAULT 'PRIVATE'
        CHECK (visibility IN ('PRIVATE', 'SHARED_WITH_STUDENT')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_students_teacher_id ON students(teacher_id);
CREATE INDEX idx_students_teacher_name ON students(teacher_id, name);
CREATE INDEX idx_courses_teacher_id ON courses(teacher_id);
CREATE INDEX idx_assignments_teacher_id ON assignments(teacher_id);
CREATE INDEX idx_attendance_teacher_date ON attendance(teacher_id, attendance_date);
CREATE INDEX idx_attendance_student_id ON attendance(student_id);
CREATE INDEX idx_teacher_memos_teacher_student ON teacher_memos(teacher_id, student_id);
