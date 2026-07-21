ALTER TABLE teachers
    ADD COLUMN invitation_code VARCHAR(20);

CREATE UNIQUE INDEX uk_teachers_invitation_code
    ON teachers (invitation_code)
    WHERE invitation_code IS NOT NULL;
