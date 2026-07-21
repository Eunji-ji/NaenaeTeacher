ALTER TABLE users
    ADD COLUMN terms_version VARCHAR(30),
    ADD COLUMN terms_agreed_at TIMESTAMP,
    ADD COLUMN privacy_version VARCHAR(30),
    ADD COLUMN privacy_agreed_at TIMESTAMP,
    ADD COLUMN age_or_guardian_confirmed_at TIMESTAMP;
