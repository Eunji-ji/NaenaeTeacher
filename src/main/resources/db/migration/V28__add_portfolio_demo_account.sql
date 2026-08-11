-- Convert the untouched legacy seed teacher into a passwordless portfolio demo.
-- The stored BCrypt-shaped value is randomly generated and has no distributed plaintext.
UPDATE users
SET login_id = 'portfolio-demo',
    password_hash = '$2a$12$LvnpngUSFGqWXeBdVVWUpKepc45rfCXzS7zoUp4k044DfOd/qnKuo',
    name = '포트폴리오 체험 선생님',
    nickname = '체험 선생님',
    is_active = TRUE,
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'teacher@naenae.com'
  AND password_hash = '$2a$10$wJjzSJk1AxkqnnsRJMeZPOVP46S41womu2t/mYEICBel7ESH2JbQW'
  AND NOT EXISTS (
      SELECT 1 FROM users demo WHERE demo.login_id = 'portfolio-demo'
  );

-- Keep the feature available even when the legacy seed account was customized or removed.
INSERT INTO users (
    email,
    login_id,
    password_hash,
    role,
    name,
    nickname,
    is_active
)
SELECT
    NULL,
    'portfolio-demo',
    '$2a$12$LvnpngUSFGqWXeBdVVWUpKepc45rfCXzS7zoUp4k044DfOd/qnKuo',
    'TEACHER',
    '포트폴리오 체험 선생님',
    '체험 선생님',
    TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE login_id = 'portfolio-demo'
);

INSERT INTO teachers (user_id, academy_name, subject_name, introduction)
SELECT
    users.id,
    'Naenae 영어학원',
    '영어',
    'NaenaeTeacher 포트폴리오 체험계정입니다.'
FROM users
WHERE users.login_id = 'portfolio-demo'
ON CONFLICT (user_id) DO NOTHING;
