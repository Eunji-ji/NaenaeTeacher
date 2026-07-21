# NaenaeTeacher 운영 배포 가이드

이 문서는 Oracle Cloud Always Free Ubuntu VM 한 대에 NaenaeTeacher를 수동 배포하는 절차를 설명한다. 명령은 저장소 루트에서 실행한다. 애플리케이션의 운영 포트는 모든 구간에서 **8081**이다.

## 1. 배포 구조 요약

```text
사용자 브라우저
  -> HTTPS 443
Nginx
  -> http://127.0.0.1:8081
Spring Boot Docker Container (Java 21, prod profile, port 8081)
  -> jdbc:postgresql://db:5432/naenae_teacher
PostgreSQL 16 Docker Container
```

- Oracle Cloud Always Free VM
- Ubuntu Linux
- Docker / Docker Compose
- Nginx / Let's Encrypt
- Spring Boot / PostgreSQL
- Application Port: `8081`

앱의 호스트 포트는 `127.0.0.1:8081`에만 바인딩된다. PostgreSQL은 호스트 포트를 열지 않고 Compose 내부 `backend` 네트워크에서만 접근한다.

## 2. 서버 최초 준비

Ubuntu 패키지를 갱신한다.

```bash
sudo apt update
sudo apt upgrade -y
sudo apt install -y git curl nginx ca-certificates
```

Docker 공식 apt 저장소 방식으로 Docker Engine과 Compose 플러그인을 설치한다.

```bash
sudo install -m 0755 -d /etc/apt/keyrings
sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
sudo chmod a+r /etc/apt/keyrings/docker.asc

. /etc/os-release
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu $VERSION_CODENAME stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
sudo systemctl enable --now docker
sudo usermod -aG docker "$USER"
```

그룹 변경을 적용하려면 SSH에서 로그아웃 후 다시 접속한다. 설치 상태를 확인한다.

```bash
docker --version
docker compose version
```

Oracle Always Free VM의 메모리가 작다면 다른 대형 프로세스를 함께 실행하지 않는다. 기본 `JAVA_OPTS=-Xms128m -Xmx512m`에서 시작하고 실제 메모리 사용량을 관찰한 뒤 조정한다.

## 3. 방화벽 설정

Oracle Cloud Console의 VCN Security List 또는 Network Security Group ingress에 다음 TCP 포트만 허용한다.

- `22`: SSH. 가능하면 관리자 IP로 제한
- `80`: HTTP 및 Let's Encrypt 검증
- `443`: HTTPS

Ubuntu UFW도 사용한다면 SSH를 먼저 허용한 후 활성화한다.

```bash
sudo ufw allow OpenSSH
sudo ufw allow 'Nginx Full'
sudo ufw enable
sudo ufw status
```

`8081`과 `5432`는 Oracle 방화벽 및 UFW에서 열지 않는다. 외부 사용자는 Nginx의 80/443으로만 접근하며, Nginx가 내부 `127.0.0.1:8081`로 프록시한다. PostgreSQL 5432 포트는 외부 공개를 금지한다.

## 4. 프로젝트 배포 경로

```bash
mkdir -p ~/apps
cd ~/apps
git clone <repository-url> naenae-teacher
cd naenae-teacher
```

배포 스크립트는 기본 브랜치를 `main`으로 가정한다. 다른 브랜치를 운영한다면 `scripts/deploy.sh`의 pull 대상을 명시적으로 변경한다.

## 5. 운영 환경변수 설정

실제 환경 파일은 예시 파일을 복사해서 서버에서만 만든다.

```bash
cp .env.prod.example .env.prod
chmod 600 .env.prod
nano .env.prod
```

반드시 변경할 값:

- `POSTGRES_PASSWORD`
- `DB_PASSWORD` (`POSTGRES_PASSWORD`와 동일한 값)
- `JWT_SECRET`
- `SERVICE_OPERATOR_NAME`
- `PRIVACY_CONTACT_EMAIL`

`POSTGRES_USER`와 `DB_USERNAME`, `POSTGRES_DB`와 `DB_URL`의 DB명도 서로 일치해야 한다. Compose 실행 시 앱의 DB 호스트는 외부 IP가 아닌 `db`로 강제된다.

비밀번호와 secret에 예시의 `change-this` 값을 절대 그대로 사용하지 않는다. 랜덤 secret은 다음처럼 생성할 수 있다.

```bash
openssl rand -base64 48
```

포트와 프로파일은 다음 값을 유지한다.

```dotenv
SPRING_PROFILES_ACTIVE=prod
PORT=8081
```

`.env.prod`는 `.gitignore`에 포함되어 있으며 Git에 커밋하면 안 된다.

## 6. 최초 실행

먼저 Compose 구성을 검증한 뒤 빌드하고 실행한다.

```bash
docker compose -f docker-compose.prod.yml --env-file .env.prod config --quiet
docker compose -f docker-compose.prod.yml --env-file .env.prod up -d --build
docker compose -f docker-compose.prod.yml --env-file .env.prod ps
```

PostgreSQL health check가 성공하면 앱이 시작된다. 앱 시작 시 Flyway가 `classpath:db/migration`의 미적용 버전을 실행하고, Hibernate는 결과 스키마를 `validate`만 한다. 최초 이미지 빌드는 Gradle 의존성 다운로드 때문에 시간이 걸릴 수 있다.

DB와 첨부파일은 각각 `naenae-teacher-postgres-data`, `naenae-teacher-uploads` named volume에 유지된다. `docker compose down`은 컨테이너만 내리며 데이터를 삭제하지 않는다. 운영에서 `docker compose down -v`는 사용하지 않는다.

## 7. 로그 확인

```bash
docker compose -f docker-compose.prod.yml --env-file .env.prod logs -f
```

서비스별 확인:

```bash
bash scripts/logs.sh app
bash scripts/logs.sh db
```

Docker JSON 로그는 컨테이너별 최대 10MB, 최대 5개 파일로 회전한다.

## 8. Health Check

VM 내부에서 확인한다.

```bash
curl --fail http://127.0.0.1:8081/actuator/health
```

정상 응답 예시:

```json
{"status":"UP"}
```

Compose 상태의 `app`도 잠시 후 `healthy`가 되어야 한다.

```bash
docker inspect --format='{{.State.Health.Status}}' naenae-teacher-app
```

## 9. 도메인과 Nginx 설정 적용

먼저 `your-domain.com`과 필요하면 `www.your-domain.com`의 DNS A 레코드를 VM 공인 IP로 연결한다. 아래 명령에서 도메인을 실제 값으로 바꾼다.

제공된 `nginx/naenae-teacher.conf`는 인증서가 이미 존재하는 **최종 HTTPS 설정**이다. 인증서가 없을 때 이 파일을 먼저 복사하면 `nginx -t`가 실패하므로, 최초 1회는 HTTP 설정으로 인증서를 발급한 후 최종 파일을 적용한다.

기본 사이트 충돌을 제거하고 Certbot용 임시 HTTP 사이트를 만든다.

```bash
sudo rm -f /etc/nginx/sites-enabled/default
sudo tee /etc/nginx/sites-available/naenae-teacher-bootstrap > /dev/null <<'NGINX'
server {
    listen 80;
    listen [::]:80;
    server_name your-domain.com www.your-domain.com;

    location / {
        proxy_pass http://127.0.0.1:8081;
        proxy_set_header Host $host;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
NGINX
sudo ln -s /etc/nginx/sites-available/naenae-teacher-bootstrap /etc/nginx/sites-enabled/naenae-teacher-bootstrap
sudo nginx -t
sudo systemctl reload nginx
```

## 10. SSL 인증서 발급

도메인 DNS가 VM 공인 IP를 가리키고 80/443 포트가 열린 뒤 Certbot을 실행한다.

```bash
sudo apt install -y certbot python3-certbot-nginx
sudo certbot --nginx -d your-domain.com -d www.your-domain.com
```

인증서 발급 후 저장소의 `nginx/naenae-teacher.conf` 안에 있는 모든 `your-domain.com`을 실제 도메인으로 변경한다. 그 다음 최종 설정을 설치한다.

```bash
sudo rm -f /etc/nginx/sites-enabled/naenae-teacher-bootstrap
sudo cp nginx/naenae-teacher.conf /etc/nginx/sites-available/naenae-teacher
sudo ln -s /etc/nginx/sites-available/naenae-teacher /etc/nginx/sites-enabled/naenae-teacher
sudo nginx -t
sudo systemctl reload nginx
```

최종 Nginx 설정의 핵심 프록시는 다음과 같다.

```nginx
proxy_pass http://127.0.0.1:8081;
```

HTTP는 HTTPS로 리다이렉트되며 `/assets`, `/css`, `/js`, `/images`, `/actuator/health`를 포함한 모든 앱 요청이 8081로 전달된다. 인증서 자동 갱신 상태를 점검한다.

```bash
sudo systemctl status certbot.timer
sudo certbot renew --dry-run
```

`www` DNS 레코드를 만들지 않았다면 Certbot과 Nginx 양쪽에서 `www.your-domain.com`을 제거한다.

## 11. 재배포

```bash
bash scripts/deploy.sh
```

스크립트는 `git pull --ff-only origin main`, Compose 설정 검증, 앱 이미지 빌드, 컨테이너 갱신, 최대 120초 health check를 순서대로 수행한다. health check 실패 시 최근 앱 로그를 출력하며 기존 DB 볼륨을 삭제하지 않는다.

배포 전에 작업 트리가 깨끗한지 확인한다. 서버에서 소스를 직접 수정하지 않는다.

```bash
git status --short
```

## 12. 재시작

이미지를 다시 빌드하지 않고 현재 컨테이너만 재시작한다.

```bash
bash scripts/restart.sh
curl --fail http://127.0.0.1:8081/actuator/health
```

## 13. DB 백업 및 복구 원칙

DB SQL 백업을 생성한다.

```bash
bash scripts/backup-db.sh
ls -lh backups/
```

백업 파일은 권한 600으로 `backups/naenae_teacher_YYYYMMDD_HHMMSS.sql`에 생성되고 Git에서 제외된다. 주말 자동 백업 예시는 다음과 같다.

```bash
crontab -e
```

```cron
0 3 * * 0 cd /home/ubuntu/apps/naenae-teacher && /usr/bin/bash scripts/backup-db.sh >> /home/ubuntu/naenae-db-backup.log 2>&1
```

실제 사용자명과 배포 경로에 맞게 수정한다. VM 장애에도 대비하려면 생성된 백업을 Oracle Object Storage 등 별도 저장소로 복사하고 복구 테스트를 정기적으로 수행한다. 첨부파일은 DB 백업에 포함되지 않으므로 `naenae-teacher-uploads` 볼륨도 별도로 백업해야 한다.

복구는 기존 데이터를 덮어쓸 수 있는 위험 작업이다. 자동화하지 않았으며, 복구 대상·백업 파일·점검 시간을 확정한 뒤 운영자가 수동 수행한다.

## 14. 장애 확인 방법

```bash
docker ps
docker compose -f docker-compose.prod.yml --env-file .env.prod ps
docker compose -f docker-compose.prod.yml --env-file .env.prod logs --tail=200 app
docker compose -f docker-compose.prod.yml --env-file .env.prod logs --tail=200 db
sudo nginx -t
sudo systemctl status nginx
curl --fail http://127.0.0.1:8081/actuator/health
```

추가 점검:

- 앱이 시작되지 않으면 DB health 상태, `.env.prod` 값 일치, Flyway 오류를 순서대로 확인한다.
- `Connection refused`이면 `docker compose ... ps`에서 앱이 running/healthy인지 확인한다.
- Nginx 502이면 앱 health와 `proxy_pass http://127.0.0.1:8081;`을 확인한다.
- 인증서 오류이면 DNS, 인증서 경로, `sudo certbot certificates` 결과를 확인한다.
- 디스크 부족이면 `df -h`, `docker system df`, 백업 및 Docker 로그 크기를 확인한다. 데이터 볼륨 삭제 명령은 실행하지 않는다.

## 15. 운영 주의사항

- `.env.prod`는 Git에 커밋하지 않는다.
- DB 비밀번호와 `JWT_SECRET`은 강력한 랜덤 문자열을 사용하고 안전한 비밀 저장소에 별도 보관한다.
- PostgreSQL 5432 포트를 외부에 공개하지 않는다.
- Spring Boot 8081 포트는 `127.0.0.1`에만 바인딩하고 외부에 공개하지 않는다.
- 외부 사용자는 Nginx 80/443으로만 접근하게 구성한다.
- 정기 DB 백업과 첨부파일 볼륨 백업을 수행하고 복구 가능성을 검증한다.
- 학생 개인정보, 로그인 ID, 초대코드, 비밀번호, 파일 내용 등을 로그에 출력하지 않는다.
- 운영 DB 직접 수동 수정은 최소화한다.
- 이미 적용된 Flyway 마이그레이션 파일은 수정하지 않고 새 버전 파일을 추가한다.
- 배포 전 로컬에서 `./gradlew test`와 `./gradlew clean bootJar`를 확인한다.
- 운영 컨테이너/볼륨을 삭제하는 `docker compose down -v`, `docker volume rm`, DB 초기화 명령을 실행하지 않는다.
- Docker 이미지와 Ubuntu 보안 업데이트를 정기적으로 적용하되 백업 후 점검 시간에 진행한다.

## 배포 전 최종 체크리스트

- [ ] DNS A 레코드가 VM 공인 IP를 가리킨다.
- [ ] Oracle ingress와 UFW에는 22/80/443만 필요한 범위로 열려 있다.
- [ ] `.env.prod`의 모든 `change-this` 값이 교체되었다.
- [ ] `PORT=8081`, `SPRING_PROFILES_ACTIVE=prod`이다.
- [ ] Compose의 app/db가 healthy이다.
- [ ] Flyway 오류가 없다.
- [ ] VM 내부 8081 health check가 UP이다.
- [ ] Nginx 설정 검증과 HTTPS 접속이 성공한다.
- [ ] DB 및 첨부파일 백업 계획이 준비되었다.
