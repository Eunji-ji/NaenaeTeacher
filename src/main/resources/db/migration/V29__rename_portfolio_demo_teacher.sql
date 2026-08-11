UPDATE users
SET name = '체험',
    nickname = '체험',
    updated_at = CURRENT_TIMESTAMP
WHERE login_id = 'portfolio-demo'
  AND role = 'TEACHER';
