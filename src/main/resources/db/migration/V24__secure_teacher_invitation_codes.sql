ALTER TABLE teachers
    ALTER COLUMN invitation_code TYPE VARCHAR(64),
    ADD COLUMN invitation_code_expires_at TIMESTAMP,
    ADD COLUMN invitation_code_use_count INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN invitation_code_max_uses INTEGER NOT NULL DEFAULT 100;

-- 기존 수동 코드는 길이가 짧고 추측 가능할 수 있으므로 모두 폐기한다.
UPDATE teachers
SET invitation_code = NULL,
    invitation_code_expires_at = NULL,
    invitation_code_use_count = 0;
