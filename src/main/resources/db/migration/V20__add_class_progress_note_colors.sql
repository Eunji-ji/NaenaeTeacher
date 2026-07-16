ALTER TABLE class_progress_notes ADD COLUMN note_color VARCHAR(20);

UPDATE class_progress_notes
SET note_color = CASE MOD(id, 4)
    WHEN 0 THEN 'YELLOW'
    WHEN 1 THEN 'LIGHT_BLUE'
    WHEN 2 THEN 'LIGHT_GREEN'
    ELSE 'LIGHT_PINK'
END;

ALTER TABLE class_progress_notes ALTER COLUMN note_color SET NOT NULL;
ALTER TABLE class_progress_notes ALTER COLUMN note_color SET DEFAULT 'YELLOW';

ALTER TABLE class_progress_notes
    ADD CONSTRAINT chk_class_progress_notes_color
    CHECK (note_color IN ('YELLOW', 'LIGHT_BLUE', 'LIGHT_GREEN', 'LIGHT_PINK'));

