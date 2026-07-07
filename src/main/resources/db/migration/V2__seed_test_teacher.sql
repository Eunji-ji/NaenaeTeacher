WITH inserted_user AS (
    INSERT INTO users (
        email,
        password_hash,
        role,
        name,
        phone
    )
    VALUES (
        'teacher@naenae.com',
        '$2a$10$wJjzSJk1AxkqnnsRJMeZPOVP46S41womu2t/mYEICBel7ESH2JbQW',
        'TEACHER',
        '김지우',
        '010-0000-0000'
    )
    ON CONFLICT (email) DO UPDATE
        SET password_hash = EXCLUDED.password_hash,
            role = EXCLUDED.role,
            name = EXCLUDED.name,
            phone = EXCLUDED.phone,
            is_active = TRUE,
            updated_at = CURRENT_TIMESTAMP
    RETURNING id
)
INSERT INTO teachers (
    user_id,
    academy_name,
    subject_name,
    introduction
)
SELECT
    id,
    '내내 영어학원',
    '영어',
    'NaenaeTeacher 테스트 선생님 계정입니다.'
FROM inserted_user
ON CONFLICT (user_id) DO UPDATE
    SET academy_name = EXCLUDED.academy_name,
        subject_name = EXCLUDED.subject_name,
        introduction = EXCLUDED.introduction,
        updated_at = CURRENT_TIMESTAMP;
