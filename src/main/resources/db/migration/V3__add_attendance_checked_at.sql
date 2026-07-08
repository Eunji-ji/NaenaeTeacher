ALTER TABLE attendance
    ADD COLUMN checked_at TIMESTAMP;

UPDATE attendance
SET checked_at = created_at
WHERE checked_at IS NULL;

ALTER TABLE attendance
    ALTER COLUMN checked_at SET NOT NULL;
