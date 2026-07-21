# 집 PC(PC2) DB 구성 및 덤프 복원 가이드

이 문서는 Git에 저장된 최신 PostgreSQL 덤프를 기준으로 집 PC의 NaenaeTeacher 개발 DB를 동일하게 구성하는 절차다. Windows PowerShell과 Docker Desktop 사용을 기준으로 한다.

## 1. 준비 사항

- Git
- Docker Desktop
- JDK 21
- 프로젝트 최신 `main` 브랜치
- 애플리케이션 포트 `8081`
- PostgreSQL 호스트 포트 `5432`

Docker Desktop을 먼저 실행하고 프로젝트를 최신 상태로 갱신한다.

```powershell
cd C:\workSp\NaenaeTeacher
git switch main
git pull origin main
docker version
docker compose version
```

집 PC의 프로젝트 경로가 다르면 실제 경로로 이동하면 된다.

## 2. 복원 대상 덤프

현재 최신 덤프는 다음 파일이다.

```text
db-transfer/naenae_teacher_20260721.dump
```

- 형식: PostgreSQL custom format
- PostgreSQL: 16.14
- Flyway 스키마: V26
- 포함 내용: 스키마와 DB 데이터 전체
- 포함되지 않는 내용: `uploads/`에 저장된 과제·알림장·게시판·프로필·주간테스트 실제 첨부파일

DB의 첨부파일 정보만 복원해도 실제 파일은 생기지 않는다. 첨부파일까지 동일하게 사용하려면 PC1의 `uploads/` 폴더를 별도로 안전하게 복사해야 한다.

## 3. PostgreSQL 컨테이너 실행

개발용 [docker-compose.yml](docker-compose.yml)은 다음 DB를 만든다.

- 컨테이너: `naenae-teacher-postgres`
- DB: `naenae_teacher`
- 사용자: `naenae`
- 비밀번호: `naenae1234`
- 포트: `localhost:5432`

컨테이너를 실행한다.

```powershell
docker compose up -d postgres
docker compose ps
```

DB 준비 상태를 확인한다.

```powershell
docker exec naenae-teacher-postgres `
  pg_isready -U naenae -d naenae_teacher
```

정상 결과에는 `accepting connections`가 표시된다.

## 4. 기존 집 PC DB가 있다면 먼저 백업

기존 DB가 전혀 없는 새 PC라면 이 단계는 건너뛴다. 기존 데이터가 있다면 애플리케이션을 먼저 종료한 후 백업한다.

컨테이너 내부에 custom-format 백업을 만든다.

```powershell
docker exec naenae-teacher-postgres `
  pg_dump -U naenae -d naenae_teacher `
  -Fc --no-owner --no-privileges `
  -f /tmp/naenae_teacher_before_restore.dump
```

집 PC로 복사한다.

```powershell
New-Item -ItemType Directory -Force .\backups | Out-Null
docker cp `
  naenae-teacher-postgres:/tmp/naenae_teacher_before_restore.dump `
  .\backups\naenae_teacher_before_restore.dump
docker exec naenae-teacher-postgres `
  rm -f /tmp/naenae_teacher_before_restore.dump
```

백업 파일이 존재하고 크기가 0보다 큰지 확인한다.

```powershell
Get-Item .\backups\naenae_teacher_before_restore.dump
```

`backups/`는 Git에서 제외된다.

## 5. 최신 덤프를 컨테이너로 복사

```powershell
docker cp `
  .\db-transfer\naenae_teacher_20260721.dump `
  naenae-teacher-postgres:/tmp/naenae_teacher_20260721.dump
```

복원 전에 덤프가 정상적인 PostgreSQL archive인지 확인한다.

```powershell
docker exec naenae-teacher-postgres `
  pg_restore --list /tmp/naenae_teacher_20260721.dump
```

archive 목록이 출력되면 정상이다.

## 6. 덤프 복원

이 작업은 집 PC의 `naenae_teacher` DB 객체와 데이터를 덤프 기준으로 교체한다. 필요한 기존 데이터가 있다면 반드시 4단계 백업을 먼저 완료한다. 복원 중에는 Spring Boot 애플리케이션을 실행하지 않는다.

```powershell
docker exec naenae-teacher-postgres `
  pg_restore `
  -U naenae `
  -d naenae_teacher `
  --clean `
  --if-exists `
  --no-owner `
  --no-privileges `
  --exit-on-error `
  /tmp/naenae_teacher_20260721.dump
```

복원이 끝나면 컨테이너의 임시 덤프를 제거한다.

```powershell
docker exec naenae-teacher-postgres `
  rm -f /tmp/naenae_teacher_20260721.dump
```

`docker compose down -v`는 PostgreSQL 볼륨과 데이터를 삭제하므로 실행하지 않는다.

## 7. 복원 결과 확인

Flyway 버전이 26인지 확인한다.

```powershell
docker exec naenae-teacher-postgres `
  psql -U naenae -d naenae_teacher `
  -Atc "select version from flyway_schema_history where success = true order by installed_rank desc limit 1"
```

주요 테이블과 사용자 수를 확인한다.

```powershell
docker exec naenae-teacher-postgres `
  psql -U naenae -d naenae_teacher `
  -c "select count(*) as user_count from users"

docker exec naenae-teacher-postgres `
  psql -U naenae -d naenae_teacher `
  -c "select count(*) as student_count from students"
```

## 8. PC2 프로파일로 애플리케이션 실행

같은 PowerShell 창에서 PC2 프로파일과 8081 포트를 지정한다.

```powershell
$env:SPRING_PROFILES_ACTIVE='pc2'
$env:PORT='8081'
$env:DB_URL='jdbc:postgresql://localhost:5432/naenae_teacher'
$env:DB_USERNAME='naenae'
$env:DB_PASSWORD='naenae1234'
```

집 PC의 프로젝트 경로가 `C:\workSp\NaenaeTeacher`가 아니라면 업로드 경로도 실제 위치로 지정한다.

```powershell
$env:ASSIGNMENT_STORAGE_DIR='D:/apps/NaenaeTeacher/uploads/assignments'
$env:NOTICE_STORAGE_DIR='D:/apps/NaenaeTeacher/uploads/notices'
$env:BOARD_STORAGE_DIR='D:/apps/NaenaeTeacher/uploads/board'
$env:PROFILE_STORAGE_DIR='D:/apps/NaenaeTeacher/uploads/profiles'
$env:WEEKLY_TEST_STORAGE_DIR='D:/apps/NaenaeTeacher/uploads/weekly-tests'
```

애플리케이션을 실행한다.

```powershell
.\gradlew.bat bootRun
```

`pc2` 프로파일은 공통 `local` DB 설정과 `application-pc2.yml`의 파일 저장 경로를 함께 사용한다.

## 9. 애플리케이션 확인

다른 PowerShell 창에서 확인한다.

```powershell
curl.exe -sS http://localhost:8081/actuator/health
```

정상 응답:

```json
{"status":"UP"}
```

브라우저 접속:

```text
http://localhost:8081
http://localhost:8081/teacher/login
http://localhost:8081/student/login
```

덤프에 저장된 계정은 기존 비밀번호의 BCrypt 해시를 그대로 포함한다. 따라서 PC1에서 사용하던 로그인 ID와 비밀번호로 로그인한다.

## 10. 자주 발생하는 문제

### 5432 포트가 이미 사용 중

```powershell
netstat -ano | Select-String ':5432'
docker ps
```

기존 로컬 PostgreSQL 또는 다른 컨테이너가 5432를 사용하고 있다면 해당 서비스를 확인한다. 데이터를 확인하지 않고 컨테이너나 볼륨을 삭제하지 않는다.

### DB 비밀번호 인증 실패

PostgreSQL named volume이 예전에 만들어졌다면 `docker-compose.yml`의 비밀번호를 바꿔도 기존 DB 비밀번호는 자동으로 변경되지 않는다. 기존 볼륨의 실제 계정 비밀번호를 사용하거나, 필요한 데이터를 먼저 백업한 뒤 별도의 초기화 계획을 세운다.

### Flyway validate 오류

```powershell
git pull origin main
docker exec naenae-teacher-postgres `
  psql -U naenae -d naenae_teacher `
  -c "select installed_rank, version, description, success from flyway_schema_history order by installed_rank"
```

덤프는 V26 기준이다. 이후 소스에 V27 이상의 마이그레이션이 추가됐다면 앱 시작 시 Flyway가 새 버전을 자동 적용한다. 이미 적용된 마이그레이션 SQL 파일은 수정하지 않는다.

### 첨부파일 다운로드 실패

DB 덤프는 파일 메타데이터만 포함한다. PC1의 `uploads/` 실제 파일과 PC2의 `app.storage.*-dir` 경로를 함께 맞춰야 한다.

## 빠른 실행 순서

새 집 PC에서 기존 DB가 없을 때의 최소 명령은 다음과 같다.

```powershell
git pull origin main
docker compose up -d postgres
docker cp .\db-transfer\naenae_teacher_20260721.dump naenae-teacher-postgres:/tmp/naenae_teacher_20260721.dump
docker exec naenae-teacher-postgres pg_restore -U naenae -d naenae_teacher --clean --if-exists --no-owner --no-privileges --exit-on-error /tmp/naenae_teacher_20260721.dump
docker exec naenae-teacher-postgres rm -f /tmp/naenae_teacher_20260721.dump
$env:SPRING_PROFILES_ACTIVE='pc2'
$env:PORT='8081'
.\gradlew.bat bootRun
```
