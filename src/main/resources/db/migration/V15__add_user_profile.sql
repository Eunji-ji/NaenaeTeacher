ALTER TABLE users ADD COLUMN nickname VARCHAR(100);
ALTER TABLE users ADD COLUMN profile_image_stored_name VARCHAR(255);
UPDATE users SET nickname = name WHERE nickname IS NULL OR BTRIM(nickname) = '';
ALTER TABLE users ALTER COLUMN nickname SET NOT NULL;