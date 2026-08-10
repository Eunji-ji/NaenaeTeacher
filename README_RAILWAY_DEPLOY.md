# Railway + Neon PostgreSQL 배포 가이드

NaenaeTeacher 운영 배포의 기준 구성은 Spring Boot 애플리케이션은 Railway, PostgreSQL은 Neon이다. Java 21과 저장소의 `Dockerfile`을 사용하며 운영 프로필은 `prod`로 실행한다.

## 1. 배포 구조와 전제 조건

- Railway가 컨테이너의 `PORT`를 자동으로 주입한다. Railway Variables에 `PORT`를 직접 만들지 않는다.
- 애플리케이션 포트는 `server.port: ${PORT:8081}`이다. Railway에서는 동적 포트를 사용하고, 로컬에서 `PORT`가 없을 때만 8081을 사용한다.
- Neon 연결 문자열은 PostgreSQL JDBC 형식이어야 하며 TLS를 위해 `sslmode=require`를 포함한다.
- Flyway가 시작 시 미적용 migration을 실행하고, Hibernate는 `ddl-auto=validate`로 결과 스키마만 검증한다.
- 운영 비밀번호와 JWT secret은 Railway Variables에서만 주입한다. `.env` 파일이나 저장소에 실제 값을 기록하지 않는다.

## 2. Neon 데이터베이스 준비

Neon 프로젝트와 데이터베이스를 만든 뒤 Dashboard의 host, database, user, password를 확인한다. Railway에 넣을 URL은 다음 형식이다.

```text
jdbc:postgresql://Neon호스트/DB명?sslmode=require
```

Neon이 제공하는 문자열이 `postgresql://`로 시작하면 그대로 넣지 말고 앞부분을 `jdbc:postgresql://`로 바꾼다. 기존 query parameter가 있다면 `sslmode=require`를 `&sslmode=require`로 연결한다.

## 3. Railway 서비스 생성

1. Railway에서 이 Git 저장소를 연결해 새 서비스를 만든다.
2. 저장소 루트의 `Dockerfile`로 빌드되는지 확인한다.
3. 아래 Variables를 등록한다. `PORT`는 등록하지 않는다.
4. 배포 후 Railway Networking에서 public domain을 생성한다.
5. Healthcheck path를 설정하는 경우 `/actuator/health`를 사용한다.

## 4. Railway Variables

아래 값은 형식 예시이며 실제 secret을 문서나 코드에 저장하지 않는다.

```dotenv
SPRING_PROFILES_ACTIVE=prod
DB_URL=jdbc:postgresql://Neon호스트/DB명?sslmode=require
DB_USERNAME=Neon유저명
DB_PASSWORD=Neon비밀번호
JWT_SECRET=긴_랜덤_문자열
DB_POOL_MAX_SIZE=3
DB_POOL_MIN_IDLE=1
JAVA_OPTS=-Xms128m -Xmx512m
SERVICE_OPERATOR_NAME=운영자명
PRIVACY_CONTACT_EMAIL=운영자이메일
```

`JWT_SECRET`은 충분히 긴 cryptographically secure random 문자열을 사용한다. Railway가 제공하는 `PORT`는 이 목록에 추가하지 않는다.

## 5. 운영 설정 확인

`application-prod.yml`의 운영 핵심값은 다음과 같다.

- datasource: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` 필수
- HikariCP: `DB_POOL_MAX_SIZE` 기본 3, `DB_POOL_MIN_IDLE` 기본 1
- JPA: `spring.jpa.hibernate.ddl-auto=validate`
- Flyway: enabled, `classpath:db/migration`
- JWT: `JWT_SECRET` 필수

Neon 무료 또는 소규모 plan에서는 DB connection 수를 아끼기 위해 pool 기본값을 유지한다. 트래픽과 Neon connection 한도를 확인하기 전에는 pool을 크게 올리지 않는다.

## 6. 첫 배포와 migration

첫 시작 시 Flyway가 `V1`부터 최신 migration까지 적용한다. 기존 migration은 checksum이 기록되므로 이미 배포된 SQL 파일을 수정하지 말고 스키마 변경이 필요하면 다음 버전 파일을 추가한다. 이후 Hibernate `validate`가 entity와 실제 스키마의 불일치를 발견하면 애플리케이션 시작을 중단한다.

V2의 공개된 로컬 테스트 계정은 V27에서 비밀번호가 변경되지 않은 경우에만 비활성화된다. 운영 계정은 공개된 테스트 자격 증명을 재사용하지 말고 정상 가입 또는 별도 운영 절차로 생성한다.

배포 로그에서 다음을 확인한다.

- active profile이 `prod`인지
- Flyway migration이 성공했는지
- HikariCP가 Neon에 TLS로 연결했는지
- 서버가 Railway가 제공한 port에서 시작했는지
- `/actuator/health`가 `UP`인지

## 7. 첨부파일 저장 주의

DB는 Neon에 영속화되지만 컨테이너 로컬 파일은 재배포 시 보존을 기대하면 안 된다. 과제, 알림장, 게시판, 프로필, 주간테스트 첨부파일을 운영에서 사용하려면 Railway Volume 또는 외부 object storage를 별도로 구성하고 해당 `*_STORAGE_DIR` 변수를 영속 경로로 지정한다.

## 8. 문제 해결

- `Connection refused` 또는 인증 오류: `DB_URL`, username, password와 Neon branch 상태를 확인한다.
- TLS 오류: JDBC URL에 `sslmode=require`가 포함됐는지 확인한다.
- `Schema-validation` 오류: Flyway 실패 로그를 먼저 확인하고 `ddl-auto`를 `update`로 바꾸지 않는다.
- 502 또는 healthcheck 실패: `PORT`를 수동 등록하지 않았는지, 애플리케이션이 `0.0.0.0`에 bind했는지 확인한다.
- DB connection 부족: pool 변수를 기본값 3/1로 되돌리고 Neon connection 사용량을 확인한다.

## 배포 체크리스트

- [ ] `SPRING_PROFILES_ACTIVE=prod`가 등록되어 있다.
- [ ] `DB_URL`이 `jdbc:postgresql://`로 시작하고 `sslmode=require`를 포함한다.
- [ ] DB와 JWT 실제 secret은 Railway Variables에만 있다.
- [ ] Railway Variables에 `PORT`가 없다.
- [ ] JPA `ddl-auto=validate`와 Flyway enabled가 유지되어 있다.
- [ ] HikariCP 기본값이 max 3, min idle 1이다.
- [ ] 배포 로그의 migration과 healthcheck가 성공한다.