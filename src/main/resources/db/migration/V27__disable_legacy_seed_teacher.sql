-- V2 created a local test account with publicly documented credentials.
-- Disable only the unchanged seed account so that fresh production databases
-- cannot authenticate with those credentials. Preserve accounts whose password
-- has already been changed by an operator.
UPDATE users
SET is_active = FALSE,
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'teacher@naenae.com'
  AND password_hash = '$2a$10$wJjzSJk1AxkqnnsRJMeZPOVP46S41womu2t/mYEICBel7ESH2JbQW';