ALTER TABLE assignments DROP CONSTRAINT IF EXISTS assignments_status_check;
UPDATE assignments SET status = 'IN_PROGRESS' WHERE status = 'OPEN';
UPDATE assignments SET status = 'COMPLETED' WHERE status = 'CLOSED';
ALTER TABLE assignments ALTER COLUMN status SET DEFAULT 'IN_PROGRESS';
ALTER TABLE assignments ADD CONSTRAINT assignments_status_check
    CHECK (status IN ('IN_PROGRESS', 'SCHEDULED', 'COMPLETED'));
CREATE INDEX idx_assignments_teacher_status_created ON assignments(teacher_id, status, created_at DESC, id DESC);