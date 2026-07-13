# PC_3 PostgreSQL 및 애플리케이션 실행 가이드

작성일: 2026-07-13
프로젝트: NaenaeTeacher

## 구성

- PC_3 역할: PostgreSQL Docker 서버
- PC_3 주소: `192.168.0.2`
- 애플리케이션 PC 주소: `192.168.0.21`
- PostgreSQL 포트: `5432`
- 데이터베이스: `naenae_teacher`
- 데이터베이스 사용자: `naenae`
- Spring Boot 포트: `8081`
- Java: Java 21

비밀번호는 이 문서나 Git 추적 파일에 기록하지 않는다. 실행할 때 `DB_PASSWORD` 환경 변수로만 주입한다.

## 1. PC_3에서 PostgreSQL 시작

PC_3에서 프로젝트의 `docker-compose.yml`이 있는 디렉터리로 이동한 후 실행한다.

```powershell
docker compose up -d
docker compose ps
docker compose exec -T postgres pg_isready -U naenae -d naenae_teacher
docker compose port postgres 5432
```

정상 상태:

- PostgreSQL 컨테이너가 실행 중이다.
- `pg_isready`가 `accepting connections`를 반환한다.
- 공개 포트가 `0.0.0.0:5432`로 표시된다.

`pg_isready`를 컨테이너 안에서 실행한 결과는 PostgreSQL 프로세스 상태만 확인한다. 다른 PC의 접속 가능 여부는 별도로 확인해야 한다.

## 2. PC_3 Windows 방화벽 설정

PC_3의 관리자 PowerShell에서 애플리케이션 PC만 PostgreSQL에 접근하도록 허용한다.

```powershell
New-NetFirewallRule `
  -DisplayName "Naenae PostgreSQL TCP 5432" `
  -Enabled True `
  -Direction Inbound `
  -Action Allow `
  -Protocol TCP `
  -LocalPort 5432 `
  -RemoteAddress 192.168.0.21 `
  -Profile Any
```

규칙 확인:

```powershell
Get-NetFirewallRule -DisplayName "Naenae PostgreSQL TCP 5432" |
    Format-List DisplayName, Enabled, Direction, Action, Profile

Get-NetFirewallRule -DisplayName "Naenae PostgreSQL TCP 5432" |
    Get-NetFirewallPortFilter

Get-NetFirewallRule -DisplayName "Naenae PostgreSQL TCP 5432" |
    Get-NetFirewallAddressFilter
```

방화벽 규칙은 일반적으로 생성 즉시 적용된다.

## 3. 애플리케이션 PC에서 DB 연결 확인

먼저 TCP 연결을 확인한다.

```powershell
Test-NetConnection 192.168.0.2 -Port 5432
```

정상 결과:

```text
SourceAddress    : 192.168.0.21
RemoteAddress    : 192.168.0.2
RemotePort       : 5432
TcpTestSucceeded : True
```

이번 점검에서는 PostgreSQL JDBC 드라이버를 사용한 계정 인증과 읽기 전용 쿼리도 성공했다.

```text
database=naenae_teacher
SELECT 1 result=1
```

TCP 연결이 실패하면 DB 이름이나 계정 정보를 확인하기 전에 PC_3의 방화벽과 네트워크부터 확인한다. TCP 연결이 성립하지 않은 상태에서는 PostgreSQL 인증 단계에 도달하지 않는다.

## 4. Spring Boot 실행

애플리케이션 PC의 프로젝트 루트에서 환경 변수를 설정하고 실행한다.

```powershell
$env:JAVA_HOME='C:\workSp\java'
$env:PORT='8081'
$env:SPRING_PROFILES_ACTIVE='local'
$env:DB_URL='jdbc:postgresql://192.168.0.2:5432/naenae_teacher'
$env:DB_USERNAME='naenae'
$env:DB_PASSWORD='<PC_3 PostgreSQL 비밀번호>'

.\gradlew.bat bootRun --no-daemon
```

8081 포트에 기존 프로세스가 있는지 먼저 확인하려면 다음 명령을 사용한다.

```powershell
Get-NetTCPConnection -LocalPort 8081 -State Listen -ErrorAction SilentlyContinue
```

## 5. 기동 확인

```powershell
curl.exe -sS http://127.0.0.1:8081/actuator/health
curl.exe -I http://127.0.0.1:8081/
```

2026-07-13 확인 결과:

- `/actuator/health`: HTTP `200`, `{"status":"UP"}`
- `/`: HTTP `302`, `/teacher/login`으로 이동
- HikariCP의 PC_3 PostgreSQL 연결 성공
- PostgreSQL 버전: 16.14
- Flyway 마이그레이션 11개 검증 성공
- 스키마 버전: 11
- 추가 마이그레이션 불필요
- Spring Boot가 `8081`에서 정상 기동

접속 URL:

- `http://localhost:8081`
- `http://localhost:8081/teacher/login`

백그라운드 실행 로그:

- `build/run/server.out.log`
- `build/run/server.err.log`

## 6. 서버 종료

8081을 사용 중인 애플리케이션만 종료한다.

```powershell
$pid8081 = (Get-NetTCPConnection `
  -LocalPort 8081 `
  -State Listen `
  -ErrorAction SilentlyContinue
).OwningProcess

if ($pid8081) {
    Stop-Process -Id $pid8081 -Force
}
```

## 문제 해결 요약

### PC_3 내부에서는 연결되지만 애플리케이션 PC에서는 타임아웃

다음을 순서대로 확인한다.

1. PC_3의 실제 IP가 `192.168.0.2`인지 확인한다.
2. `docker compose port postgres 5432`가 `0.0.0.0:5432`인지 확인한다.
3. 애플리케이션 PC의 IP가 방화벽 규칙의 `RemoteAddress`와 같은지 확인한다.
4. Windows Defender 외의 백신 또는 보안 프로그램 방화벽을 확인한다.
5. 공유기의 Wi-Fi 클라이언트 격리 설정을 확인한다.

Ping은 ICMP 방화벽 정책 때문에 실패할 수 있다. PostgreSQL 접근 가능 여부는 `Test-NetConnection ... -Port 5432`의 `TcpTestSucceeded`로 판단한다.

## 보안 주의사항

- DB 비밀번호를 문서, 소스 코드 또는 Git 커밋에 추가하지 않는다.
- PostgreSQL 5432 포트를 인터넷 전체에 공개하지 않는다.
- 방화벽은 `LocalSubnet` 전체보다 필요한 애플리케이션 PC IP만 허용한다.
- PC 주소가 바뀌면 PC_3의 방화벽 `RemoteAddress`도 갱신한다.
- 장기적으로 `docker-compose.yml`과 로컬 설정의 DB 비밀번호 기본값도 환경 변수로 분리한다.
