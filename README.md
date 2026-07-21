# NaenaeTeacher

학원 선생님과 학생이 함께 사용하는 반응형 웹 서비스입니다. 현재 단계는 선생님용 MVP 개발을 위한 Spring Boot 초기 세팅이며, 학생용 영역은 이후 확장을 고려해 URL, Role, 패키지 구조만 준비했습니다.

## 기술 스택

- Java 21
- Spring Boot 3.3.x
- Gradle
- Spring Web, Spring Security, Spring Data JPA
- PostgreSQL Driver, Flyway
- Thymeleaf, Bootstrap 5 CDN
- Validation, Lombok, Actuator
- springdoc-openapi

## 로컬 실행

필수 조건:

- JDK 21
- Docker Desktop 또는 PostgreSQL 16
- Eclipse 사용 시 Buildship Gradle Integration

```bash
docker compose up -d
./gradlew bootRun
```

Windows PowerShell에서는 다음 명령을 사용할 수 있습니다.

```powershell
.\gradlew.bat bootRun
```

Git에 포함된 최신 DB 덤프를 집 PC(PC2)의 Docker PostgreSQL에 복원하는 방법은 [`PC2_DB_SETUP.md`](PC2_DB_SETUP.md)를 참고하세요.

로컬 기본 DB 값은 `application-local.yml`에 개발 편의용 기본값으로 들어 있습니다. 운영 환경에서는 환경변수로 값을 주입하세요.

Eclipse에서는 `File > Import > Gradle > Existing Gradle Project`로 프로젝트 루트를 가져오세요. 기존 프로젝트로 열려 있다면 `Gradle > Refresh Gradle Project`를 실행하면 됩니다.

## Docker PostgreSQL

```bash
docker compose up -d
docker compose logs -f postgres
```

기본 DB:

- DB: `naenae_teacher`
- User: `naenae`
- Password: `naenae1234`
- Port: `5432`

## 환경변수

`.env.example`을 참고하세요. 실제 `.env`는 커밋하지 않습니다.

- `PORT`: 애플리케이션 포트
- `SPRING_PROFILES_ACTIVE`: `local` 또는 `prod`
- `DB_URL`: PostgreSQL JDBC URL
- `DB_USERNAME`: DB 사용자
- `DB_PASSWORD`: DB 비밀번호
- `JWT_SECRET`: 추후 JWT 인증 구현용 secret
- `DB_POOL_MAX_SIZE`: HikariCP 최대 풀 크기
- `DB_POOL_MIN_IDLE`: HikariCP 최소 idle 수

## Flyway

DB 스키마 생성은 Hibernate가 아니라 Flyway가 담당합니다.

- 마이그레이션 위치: `src/main/resources/db/migration`
- 초기 파일: `V1__init_schema.sql`
- JPA 설정: `ddl-auto: validate`
- 운영/로컬 공통: `open-in-view: false`

## 기본 URL

- `/` -> `/teacher/login`
- `/teacher/login` -> 선생님 로그인 화면
- `/teacher/dashboard` -> 선생님 대시보드
- `/student` -> 학생용 coming soon 화면
- `/api/health` -> `{ "status": "ok" }`
- `/actuator/health` -> Actuator health
- `/api/teacher/**` -> `TEACHER` 권한 예정
- `/api/student/**` -> `STUDENT` 권한 예정
- `/api/auth/**` -> 인증 API 공개 영역 예정

## 추후 개발 순서

1. 회원가입 또는 초기 관리자/선생님 계정 생성 방식 결정
2. 선생님 로그인 API와 세션/JWT 전략 확정
3. 학생 관리 CRUD
4. 수업 관리 및 학생 배정
5. 과제 관리와 제출 상태
6. 출석 관리
7. 학생용 화면/API 확장

## 배포 주의사항

- 운영에서는 `SPRING_PROFILES_ACTIVE=prod`를 사용하세요.
- DB 접속정보와 `JWT_SECRET`은 환경변수로만 주입하세요.
- 비밀번호는 `password_hash`에 BCrypt 해시만 저장하는 구조입니다.
- 무료 서버를 고려해 기본 `JAVA_OPTS`는 `-Xms128m -Xmx512m`로 낮게 잡았습니다.
- 운영 DB 스키마 변경은 반드시 Flyway 마이그레이션 파일로 관리하세요.
## 환경별 파일 저장소

과제·알림장·게시판 첨부파일의 업로드와 다운로드는 각 `app.storage.*-dir`에 지정한 동일한 저장소를 사용합니다.

- PC1: `application-pc1.yml`
- PC2: `application-pc2.yml`
- 운영 서버: `application-prod.yml`
- 모든 환경에서 `ASSIGNMENT_STORAGE_DIR`, `NOTICE_STORAGE_DIR`, `BOARD_STORAGE_DIR`, `PROFILE_STORAGE_DIR` 환경변수로 기본 경로를 재정의할 수 있습니다.
- 로컬 기본 프로필은 `pc1`이며, PC2에서는 `SPRING_PROFILES_ACTIVE=pc2`, 운영에서는 `SPRING_PROFILES_ACTIVE=prod`를 사용합니다.
- 실제 업로드 파일이 저장되는 `uploads/` 디렉터리는 Git에서 제외됩니다.
