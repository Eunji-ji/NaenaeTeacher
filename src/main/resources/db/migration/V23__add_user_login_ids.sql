ALTER TABLE users
    ADD COLUMN login_id VARCHAR(50);

UPDATE users
SET login_id = email;

ALTER TABLE users
    ALTER COLUMN login_id SET NOT NULL;

ALTER TABLE users
    ALTER COLUMN email DROP NOT NULL;

CREATE UNIQUE INDEX uk_users_login_id
    ON users (login_id);
