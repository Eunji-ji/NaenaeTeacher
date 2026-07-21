# NaenaeTeacher 테스트 DB 이전

`naenae_teacher_20260721.dump`는 PostgreSQL custom-format 덤프입니다.

- 생성일: 2026-07-21
- PostgreSQL: 16.14
- Flyway 스키마: V26
- 포함 범위: 현재 로컬 테스트 DB의 스키마와 데이터 전체

## 복원

대상 PostgreSQL에 빈 데이터베이스를 만든 뒤 실행합니다.

```powershell
createdb -U postgres naenae_teacher
pg_restore -U postgres -d naenae_teacher --clean --if-exists --no-owner --no-privileges .\naenae_teacher_20260721.dump
```

Docker의 PostgreSQL 16 컨테이너로 복원하는 경우:

```powershell
docker cp .\naenae_teacher_20260721.dump <컨테이너명>:/tmp/naenae_teacher_20260721.dump
docker exec <컨테이너명> pg_restore -U naenae -d naenae_teacher --clean --if-exists --no-owner --no-privileges /tmp/naenae_teacher_20260721.dump
```

복원 대상 DB에 필요한 데이터가 있으면 먼저 별도로 백업하세요. `--clean`은 덤프에 포함된 기존 객체를 삭제한 뒤 복원합니다.
