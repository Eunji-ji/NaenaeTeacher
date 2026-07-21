# NaenaeTeacher Worklog

## Current Goal

Implement teacher-side student management with multi-class registration.

## Current Status

- Current machine is `PC_1`.
- The user also works from `PC_2`; at the start of future work, check the `PC_2` notes in this `WORKLOG.md` so work is not duplicated.
- PC_2 did not make source changes yesterday; it only completed basic setup and got the server running.
- PC_2 could not continue feature work because the database was not available there.
- After today's work is complete, export a PostgreSQL dump from the current PC_1 database so PC_2 can import the same DB state.
- Java 21 installed.
- Project uses Spring Boot.
- PostgreSQL will be used.
- Teacher dashboard and student dashboard are required.
- UI should be responsive, clean, modern, and slightly cute.
- Gradle Java toolchain is set to Java 21 in `build.gradle`.
- `gradle.properties` points Gradle to the Eclipse JustJ / Temurin Java 21 JDK.
- Gradle 8.10.2 distribution was restored under the local Gradle cache.
- Windows `gradlew.bat` was added and uses the same local Gradle distribution flow as `gradlew`.
- `gradlew.bat --version` confirmed Gradle runs with Java 21.0.10.
- `gradle build` was verified successfully with temporary Gradle/build directories because this sandbox blocks deletion under the workspace.
- Java root package is now `com.naenae`.
- Teacher-facing code is under `com.naenae.teacher`.
- Student-facing code is under `com.naenae.student`.
- Shared config, base domain, exceptions, responses, and users are under `com.naenae.common`.
- Teacher dashboard is split into `dashboard/controller`, `dashboard/service`, and `dashboard/model`.
- Eclipse metadata now includes the Buildship Gradle classpath container so Spring/JPA/Lombok imports can resolve in the IDE.
- Constructor injection no longer depends on Lombok `@RequiredArgsConstructor` in config/service/controller classes that commonly show IDE errors.
- `CustomUserDetails` no longer depends on Lombok for its own getter, and `User` exposes explicit getters used by Spring Security.
- The new dashboard UI baseline from `src/main/naenae-teacher-dashboard` is being applied to Spring MVC views.
- Teacher dashboard view now lives under `src/main/webapp/WEB-INF/views/teacher/dashboard.html`.
- Dashboard-specific CSS is separated under `src/main/webapp/assets/css/naenae-dashboard.css`.
- `src/main/webapp` is packaged into `META-INF/resources` so the dashboard view and CSS are available from the executable jar.
- Thymeleaf is configured to resolve `WEB-INF/views` before the default template location, while existing templates can remain under `src/main/resources/templates`.
- Git repository is initialized and connected to GitHub at `https://github.com/Eunji-ji/NaenaeTeacher`.
- Initial project setup has been pushed to the remote `main` branch.
- Flyway now includes a test teacher seed account for local login verification.
- Login and signup screens use the shared Naenae dashboard CSS baseline.
- Minimal teacher signup is implemented with name, email, password, and password confirmation.
- The `WEB-INF/views` Thymeleaf resolver now reads from `classpath:/META-INF/resources/WEB-INF/views/` so bootRun/bootJar resolve the new auth templates instead of falling back to old `resources/templates` files.
- Dashboard teacher display text now uses `김지우 쌤`.
- Login/signup intro headline sizing was reduced to avoid awkward wrapping.
- Dashboard header now displays the authenticated teacher name and initial instead of hardcoded `김지우`.
- Dashboard header includes a POST `/logout` 나가기 button.
- Auth/dashboard templates and shared CSS were moved back to Spring Boot default resource locations for faster development refresh.
- Login intro description text size was reduced for better single-line fit.
- Teacher student management now supports manual student registration.
- Classes are registered separately through `/teacher/courses`.
- Student registration now selects only one registered class through a combo box.
- Students can be connected to multiple classes using the existing `courses` and `course_students` tables.
- Student list can be filtered by class.
- Student management sidebar now groups `반등록` and `학생등록` under a shared `학생관리` section.
- `출석관리` was also moved under the shared `학생관리` section.
- `반등록` and `학생등록` now support deletion from their list views.
- The student class-selector helper text was removed to keep the form aligned.

## Next Tasks

1. Restart the Spring Boot app because the new course controller/service changed web routes.
2. Verify `/teacher/courses` loads and can register multiple classes.
3. Verify `/teacher/students` loads after login and only shows registered classes in the selector.
4. Register a student with multiple selected classes and confirm the class filter works.
5. Decide next student-management fields before Excel upload design.
6. Wire dashboard cards to real service data instead of static placeholders.

## Completed This Session

- Confirmed Java 21 JDK exists at `C:/Users/ADMIN/.p2/pool/plugins/org.eclipse.justj.openjdk.hotspot.jre.full.win32.x86_64_21.0.10.v20260205-0638/jre`.
- Confirmed `build.gradle` uses `JavaLanguageVersion.of(21)`.
- Restored missing files from the Gradle 8.10.2 zip in `.gradle/wrapper/dists`.
- Added `gradlew.bat` for Windows PowerShell usage.
- Updated `gradlew` and `gradlew.bat` to use a project-local `GRADLE_USER_HOME`.
- Verified the project builds successfully with Java 21 using:
  `gradle.bat build -PbuildDir=%TEMP%/naenae-build-output --no-daemon`
- Reorganized Java packages into `com.naenae.teacher`, `com.naenae.student`, and `com.naenae.common`.
- Moved teacher profile, student profile, and common user packages out of the old nested `teacher.teacher`, `teacher.student`, and `teacher.user` structure.
- Renamed dashboard controllers to `TeacherDashboardController` and `StudentDashboardController`.
- Added `TeacherDashboardService` and `TeacherDashboard` model.
- Added Eclipse Buildship classpath/project metadata and the Gradle `eclipse` plugin.
- Verified `gradle build` succeeds after the package reorganization.
- Replaced Lombok-generated constructors with explicit constructors in `SecurityConfig`, `CustomUserDetailsService`, and `TeacherDashboardController`.
- Verified `compileJava` succeeds after fixing the `SecurityConfig` final field error.
- Added explicit getters to `User` for `email`, `passwordHash`, `role`, and related fields used by `CustomUserDetails`.
- Verified `compileJava` succeeds after fixing the `CustomUserDetails#getPassword()` error.
- Added `src/main/webapp/WEB-INF/views/teacher/dashboard.html` based on the provided NaenaeTeacher dashboard UI.
- Added separated dashboard CSS at `src/main/webapp/assets/css/naenae-dashboard.css`.
- Added a `WEB-INF/views` Thymeleaf template resolver in `WebConfig`.
- Added `/assets/**` resource handling and allowed it through Spring Security.
- Updated Gradle `processResources` so `src/main/webapp` is included in boot jar resources.
- Verified `bootJar` includes the dashboard HTML and CSS under `META-INF/resources`.
- Verified `gradle build` succeeds after the UI view changes.
- Initialized local Git repository.
- Created initial commit `b2f260e Initial NaenaeTeacher project setup`.
- Added remote `origin` and pushed `main` to GitHub.
- Added `V2__seed_test_teacher.sql` with a BCrypt-hashed local teacher login.
- Verified `gradle build` succeeds after adding the seed migration.
- Added `WEB-INF/views/auth/login.html` and `WEB-INF/views/auth/signup.html`.
- Updated the auth controller with GET/POST `/teacher/signup`.
- Added `TeacherSignupService` to create `User` and `Teacher` records with BCrypt password hashing.
- Added server-side password confirmation validation to teacher signup.
- Fixed the custom Thymeleaf resolver path so `/teacher/login` uses the new shared-CSS auth screen.
- Updated the dashboard profile and welcome copy from `김지우 선생님` to `김지우 쌤`.
- Reduced `.auth-intro h1` font sizes in the shared dashboard CSS.
- Wired `TeacherDashboardController` to expose authenticated teacher display name and initial.
- Replaced hardcoded dashboard teacher copy with Thymeleaf model values.
- Added dashboard logout form/button in the top-right header.
- Moved active templates to `src/main/resources/templates` and active CSS to `src/main/resources/static/assets/css`.
- Removed duplicate active auth/dashboard files from `src/main/webapp`.
- Added DevTools and `bootRun sourceResources` configuration for easier local UI iteration.
- Reduced `.auth-intro p` font size to keep the login intro copy more compact.
- Added `TeacherStudentController` and `TeacherStudentService`.
- Added `CourseStudentRepository`, student list models, and course option model.
- Added entity factory methods for `Student`, `Course`, and `CourseStudent`.
- Added `/teacher/students` list/filter/register screen.
- Added `/teacher/courses` class registration screen.
- Changed student registration to choose from already registered classes only.
- Later simplified student registration to a single class combo box to match the 1:1 requirement.
- Added deletion actions for both classes and students.
- Grouped `반등록` and `학생등록` in the sidebar under `학생관리`.
- Moved `출석관리` under the shared `학생관리` section.
- Extended shared CSS for student forms, filters, tables, and class chips.
- Verified `gradle build` succeeds after student management implementation.
- Added entity factory methods and `UserRepository.existsByEmail`.
- Allowed `/teacher/signup` through Spring Security.
- Extended `naenae-dashboard.css` with shared auth screen styles.
- Verified `gradle build` succeeds and boot jar includes the auth templates/CSS.

## This Update

- Added `학생 학습관리` under student management and split it into a list view and a student detail view.
- Student memo text now reuses the existing `memo_summary` column and supports 4000 characters.
- Added midterm/final score storage per year and a simple score graph on the student detail page.
- The student list now shows only name, class, and school, and names open the detail page.
- The score graph was changed from bars to a line chart.
- Student list sorting stays class name first, then student name.
- Teacher branding now renders as the logged-in name plus `쌤` on the teacher menus.
- Dashboard attendance summaries now use live present / late / absent counts and attendance rate.
- Attendance management is being moved from checkbox save flow to per-row status selection with auto-save.
- Added a `모두출석` bulk action on the attendance screen to mark all currently loaded students as present.
- Simplified the attendance bulk action label to `전체출석` and tightened its button styling to avoid layout breakage.
- Moved the bulk attendance button next to the query controls so it no longer inherits the date navigation button sizing.
- Added `student_academic_records` and a Flyway migration for student score history.
- Bulk template download/upload buttons and the student bulk action now use the shared dashboard button styling.
- `gradle build` passed after the student learning detail restructuring and score history migration.
- The student learning score graph was compacted into a smaller line chart so it fits as a light yearly summary without overflowing the detail card.
- Added DB-backed today word tables for 3 level bands: lower elementary, upper elementary, and middle school.
- Seeded today word data at startup and wired the teacher dashboard to show all 3 daily selections.
- Added a student dashboard route that shows the daily word matched to the student grade band, with a fallback to course title when grade is not stored.
- Added role-based login redirect so student accounts can land on the student dashboard.
- Fixed the corrupted `WordLevel` source file used by today word routing.
- Removed the UTF-8 BOM from `WordLevel.java` after the Java compiler rejected the hidden BOM character.
- Scanned the main Java and resource text files for BOM markers and cleaned the ones that had hidden BOM bytes: `TodayWordService.java`, `teacher/courses.html`, and `teacher/students.html`.
- On PC_1, PostgreSQL connection refused was resolved by starting Docker Compose PostgreSQL with `docker compose up -d`.
- Verified container `naenae-teacher-postgres` is accepting connections on port `5432`.
- Verified `compileJava` succeeds when run outside the managed sandbox after Gradle probe-file cleanup.
- Started the Spring Boot app on port `8081` and verified `http://localhost:8081/api/health` returns `ok`.
- Fixed dashboard today-word selection creation by changing the selection lookup methods from read-only transactions to writable transactions.
- Rewrote `TodayWordService.java` with ASCII-safe string handling for Korean grade markers to avoid repeated source encoding corruption.
- Verified `compileJava` succeeds after the today-word transaction and encoding fix.
- Removed Java-side hardcoded/generated today-word seed data from `TodayWordService`.
- Added Flyway migration `V6__seed_temporary_today_words.sql` to seed temporary DB today-word data when `today_words` is empty.
- Today-word service now only reads words from DB and creates daily selections in `today_word_selections`.
- Verified `compileJava` succeeds after removing Java today-word seed logic.
- Restarted the Spring Boot app on port `8081` and verified Flyway schema version `6` is applied.
- Verified DB today-word counts are 1000 rows each for `LOWER_ELEMENTARY`, `UPPER_ELEMENTARY`, and `MIDDLE_SCHOOL`.
- Verified `http://localhost:8081/api/health` returns `ok` after the restart.
- Updated the teacher dashboard today-word card so the three level words render as a vertical list that uses each row's width efficiently.
- Added length-based today-word font scaling plus wrapping so long DB words and sentences stay inside their rows.
- Removed the student learning management card from the teacher dashboard body.
- Moved the board card up into the freed dashboard position and changed quick actions to span a full dashboard row.
- Added DB-backed today sentence support with `today_sentences` and `today_sentence_selections`.
- Added Flyway migration `V7__create_and_seed_today_sentences.sql` with 1000 quote-style study sentences each for lower elementary, upper elementary, and middle school.
- Added today sentence domain, selection entity, repositories, service, and dashboard view model.
- Wired the teacher dashboard to show 3 daily sentences, one per level band.
- Verified `compileJava` succeeds after adding today sentence support.
- Restarted the Spring Boot app on port `8081` and verified Flyway schema version `7` is applied.
- Verified DB today-sentence counts are 1000 rows each for `LOWER_ELEMENTARY`, `UPPER_ELEMENTARY`, and `MIDDLE_SCHOOL`.
- Verified `http://localhost:8081/api/health` returns `ok` after the today sentence migration.
- Added nullable Korean meaning columns with Flyway `V8__add_korean_meanings_to_today_english.sql`: `today_words.meaning_ko` and `today_sentences.meaning_ko`.
- Added `meaningKo` fields and getters to `TodayWord` and `TodaySentence`.
- Verified `compileJava` succeeds after adding Korean meaning columns to the domain model.
- Restarted the Spring Boot app on port `8081` and verified Flyway schema version `8` is applied.
- Verified both `meaning_ko` columns exist as nullable `text` columns in PostgreSQL.
- Replaced the student learning score line graph with a compact score table chart grouped by year, midterm, and final exam.
- Added `StudentLearningScoreTableRow` and service-side grouping so the score display no longer stretches two points across the full chart area.
- Rewrote the student learning detail template with clean Korean labels after the previous file had corrupted Korean text.
- Added fixed-size table chart CSS for the score display and verified `compileJava` succeeds.
- Fixed a corrupted `TeacherDashboardController.java` source file that would break the next server restart.
- Stopped duplicate stale Spring Boot application processes and restarted a single fixed server on port `8081`.
- Verified `http://localhost:8081/api/health` returns `ok`.
- Verified `/teacher/dashboard` and `/teacher/students/status` resolve to secured routes and redirect to `/teacher/login` when not authenticated, instead of falling through to static-resource 404.

## Next Resume Point

Log in again if the browser session was reset, then visually confirm the dashboard and student learning score table chart. Later replace temporary today word/sentence seed data with curated content including Korean meanings and export a PostgreSQL dump from PC_1 for PC_2.

## Important Notes

- Do not implement full business features yet.
- Initial goal is project foundation.
- Keep structure clean for future teacher/student role separation.

## 2026-07-08 pc_2 Local VS Code and Docker Bring-up

- Confirmed the repository remote is `origin -> https://github.com/Eunji-ji/NaenaeTeacher.git` on branch `main`.
- Confirmed `pc_2` uses Java 21 from `C:\workSp\java`.
- The project wrapper reads `org.gradle.java.home` from project `gradle.properties`, so `pc_2` keeps a local-only `gradle.properties` value of `C:/workSp/java`.
- Marked `gradle.properties` as local-only with `git update-index --skip-worktree gradle.properties` so the PC-specific Java path is not committed.
- Marked VS Code/Eclipse-generated local IDE changes as local-only with `skip-worktree`: `.classpath`, `.project`, `.settings/org.eclipse.buildship.core.prefs`, and `.settings/org.eclipse.jdt.core.prefs`.
- Added `.settings/org.eclipse.jdt.apt.core.prefs` to local `.git/info/exclude` so it is not committed.
- Docker Desktop initially could not start the engine because WSL 2 was not installed/enabled.
- Enabled the required Windows features for WSL and Virtual Machine Platform, rebooted, then started Docker Desktop.
- Verified Docker Desktop Linux engine is running with Docker 29.6.1 and WSL2 backend.
- Started PostgreSQL with `docker compose up -d`; container `naenae-teacher-postgres` is running on host port `5432`.
- Verified PostgreSQL readiness with `pg_isready -U naenae -d naenae_teacher`.
- Started the Spring Boot app with `gradlew.bat bootRun`.
- Verified `http://localhost:8080/api/health` returns `{"status":"ok"}` and `/` redirects to `/teacher/login`.
- Next local startup on `pc_2`: start Docker Desktop, run `docker compose up -d`, then run `$env:PORT='8081'; .\gradlew.bat bootRun` from `C:\workSp\NaenaeTeacher`.

## Next Resume Point

- On another PC, do not reuse this PC's local Java path. Make sure that PC has its own valid Java 21 setup before running Gradle.
- Continue normal development from `main` after pulling the latest commit.
## 2026-07-08 pc_2 Port Change

- Switched the local Spring Boot runtime from port `8080` to port `8081` on `pc_2`.
- Stopped the existing process listening on `8080`.
- Restarted the app with `PORT=8081`.
- Verified `http://localhost:8081/api/health` returns `{"status":"ok"}`.
- Verified `http://localhost:8080/api/health` no longer responds.

## 2026-07-15 학생 성적 변화량 정렬 수정

- 학생 성적을 연도 오름차순으로 정렬한 뒤 같은 연도에서는 중간고사, 기말고사 순으로 명시적으로 정렬하도록 수정했다.
- 시험 종류 문자열의 알파벳순 정렬 때문에 기말고사가 중간고사보다 먼저 배치되어 변화량의 부호가 반대로 계산되던 문제를 해결했다.
- 성적 변화량과 차트가 `2025 중간 → 2025 기말 → 2026 중간 → 2026 기말` 순서를 동일하게 사용하도록 했다.
## 2026-07-15 과제 상세조회·공통 페이징·파일 처리 개선

- 과제 목록의 행을 클릭하면 교사 소유권이 확인된 과제 상세조회 화면으로 이동하도록 구현했다.
- 과제 상세조회에서 등록한 첨부파일의 파일명과 크기를 표시하고 다운로드할 수 있도록 구현했다.
- 첨부파일 조회 시 과제 ID, 첨부파일 ID, 로그인한 교사의 ID를 함께 검증해 다른 교사의 파일에 접근할 수 없도록 했다.
- 과제와 단어시험 목록을 등록일시 내림차순으로 조회하고 페이지당 10개씩 표시하도록 변경했다.
- `PaginationSupport`, `PageView`, 공통 Thymeleaf 페이징 fragment를 추가해 다른 게시판에서도 재사용할 수 있도록 했다.
- 과제 파일 저장 경로를 `app.storage.assignment-dir`로 분리하고 `pc1`, `pc2`, `prod` 프로필별 설정 파일에서 각각 관리하도록 했다.
- 공통 `LocalFileStorage`와 `FileDownloadResponseFactory`를 추가해 파일명 정규화, 저장소 경로 이탈 방지, 저장, 다운로드 응답 생성을 재사용하도록 했다.
- 공통 `ExcelFileService`를 추가하고 학생 엑셀 일괄등록의 템플릿 생성, 헤더 검증, 행 읽기 로직을 공통 처리로 교체했다.
- 업로드 파일이 Git에 포함되지 않도록 `/uploads/`를 `.gitignore`에 추가했다.
- 공통 페이징, 파일 경로 보안, 엑셀 생성·읽기, 단어시험·과제 목록 및 과제 상세 템플릿 렌더링 테스트를 추가했다.
## 2026-07-15 과제 목록 첨부파일 개수 표시 개선

- 과제 조회 목록에서 각 과제의 첨부파일 개수를 `첨부파일 N개` 배지로 명확하게 표시하도록 개선했다.
## 2026-07-15 과제 수정·삭제 기능 추가

- 과제 조회 목록에 단어시험과 동일한 수정·삭제 버튼을 추가했다.
- 로그인한 교사가 소유한 과제만 수정하거나 삭제할 수 있도록 기존 교사 소유권 검증을 동일하게 적용했다.
- 과제 제목, 게시 기간, 내용, 대상 반을 수정하고 기존 첨부파일을 유지한 채 새 첨부파일을 추가할 수 있도록 구현했다.
- 수정 시 기존 첨부파일을 포함해 최대 5개, 파일당 10MB 제한을 서버와 화면에서 함께 검증하도록 했다.
- 대상 반 연결은 기존 연결을 유지하면서 변경된 반만 추가·제거해 고유키 충돌을 방지했다.
- 과제 삭제 트랜잭션이 커밋된 뒤 실제 첨부파일을 공통 파일 저장소에서 정리하도록 구현했다.
- 과제 상세 화면에도 수정 버튼을 추가하고 등록·수정 공용 폼에서 기존 첨부파일을 다운로드할 수 있도록 했다.
- 목록 버튼, 수정 폼 기존값, 첨부파일 표시와 실제 파일 삭제 테스트를 추가하고 전체 테스트 통과를 확인했다.
## 2026-07-15 과제 화면 사이드바 메뉴 복원

- 과제 목록·상세·수정 화면에서 오늘의 영어, 알림장, 게시판, 설정 메뉴가 누락되던 문제를 수정했다.
- 전체 교사 메뉴를 `teacher-sidebar` 공통 Thymeleaf fragment로 분리해 과제 관련 화면과 단어시험 목록이 동일한 사이드바를 사용하도록 변경했다.
- 과제 및 단어시험 목록 템플릿 테스트에서 전체 하단 메뉴가 렌더링되는지 검증하도록 보강했다.
## 2026-07-15 반별 알림장 기능 추가

- 교사용 알림장 목록, 상세조회, 등록, 수정, 삭제 기능을 추가하고 최신 등록일 역순 공통 페이징을 적용했다.
- 알림 대상을 전체 학생 또는 교사가 소유한 여러 반으로 선택해 저장할 수 있도록 알림장과 반의 다대다 연결 구조를 추가했다.
- 등록·수정 화면에서 전체와 반별 다중 선택을 함께 제공하고, 잘못된 반 ID나 대상 미선택을 서버에서 검증하도록 했다.
- 알림장 첨부파일을 최대 5개, 파일당 10MB로 등록하고 교사·학생 상세 화면에서 권한을 확인한 뒤 다운로드할 수 있도록 구현했다.
- 알림장 파일 경로를 `app.storage.notice-dir`로 분리하고 `pc1`, `pc2`, `prod` 프로필별 환경변수 `NOTICE_STORAGE_DIR`로 관리하도록 했다.
- 학생 알림장 메뉴와 목록·상세 화면을 추가하고, 학생 담당 선생님의 전체 알림 또는 실제 소속 반 알림만 조회되도록 제한했다.
- 학생 대시보드에 본인에게 공개된 최근 알림 5건을 표시하고 학생용 공통 사이드바를 추가했다.
- 기본 서버 포트를 8081로 변경했다.
- 교사·학생 알림장 목록, 등록·수정 폼, 상세, 학생 대시보드 템플릿 렌더링 테스트를 추가하고 전체 테스트 통과를 확인했다.
## 2026-07-15 Notice list design refresh

- Refined the teacher notice list with a soft summary hero, total notice count, and a cleaner card-based layout.
- Improved visual hierarchy for target courses, registration time, attachment count, and row actions while keeping full-card detail navigation.
- Added responsive layouts for tablet and mobile widths and a clearer empty state.
- Reconfirmed that teacher and student notices use the shared 10-item pagination and newest-first ordering.
- Verified pagination links and the redesigned notice template with the full test suite.
## 2026-07-15 Shared community board

- Added a shared academy-scoped board for teacher and student accounts with newest-first 10-item pagination.
- Added post registration, detail, edit, and delete flows with author ownership checks and teacher moderation delete permission.
- Added V13 tables for posts, attachments, and comments while keeping every post scoped to its teacher academy.
- Added attachment upload and authorized download using the common file service and profile-specific `BOARD_STORAGE_DIR` paths.
- Added view counts, comment counts, attachment indicators, and author labels in the `[course] name` format; multi-course students show all course names.
- Added comment registration and deletion with author or teacher moderation permission.
- Built a classic website-style board table with title, author, created date/time, and view-count columns plus responsive detail, editor, and comment layouts.
- Added the board to the student sidebar and verified list pagination, editor, file, detail, and comment templates with automated tests.
## 2026-07-15 Notice hero responsive typography

- Reduced the notice-list hero headline size on desktop and tablet layouts.
- Added explicit 18px headline and 12px description sizes for mobile layouts so the copy scales down cleanly on narrow screens.
## 2026-07-15 Shared mobile navigation

- Added a shared hamburger menu trigger to all teacher and student dashboard pages.
- Replaced the hidden mobile sidebar with an accessible off-canvas menu that includes a visible close button.
- Added backdrop-tap, menu-link, Escape-key, and desktop-resize close behavior with focus and ARIA state handling.
- Reduced mobile header typography and spacing for narrow screens.
## 2026-07-15 Mobile typography and logout icon

- Replaced text logout buttons with a compact door-shaped icon while preserving accessible labels and tooltips.
- Reduced mobile header, card, list, metadata, action, detail, and comment typography at widths up to 480px.
- Kept form controls at 16px on mobile to avoid unwanted browser input zoom.
## 2026-07-15 Dashboard attendance-rate denominator fix

- Changed the dashboard attendance-rate denominator from checked attendance records to all enrollments in the teacher’s active courses.
- Students in unchecked courses now remain in the denominator and therefore no longer allow a partially checked academy to display 100% attendance.
- Counted multi-course students once per course because each course has its own attendance target and record.
- Limited status counts to active courses and added regression tests for partially checked and empty-course cases.
## 2026-07-15 Assignment statuses and profile management

- Expanded assignment statuses to in-progress, scheduled, and completed with a safe V14 migration from OPEN/CLOSED values.
- Added assignment status radio controls, priority ordering, newest-first ordering within each status, and an in-progress-only checkbox filter that persists through pagination.
- Added student assignment list/detail/download routes that expose only in-progress assignments linked to the student’s courses.
- Added recent in-progress assignments to the student dashboard and replaced the teacher dashboard placeholder with the real in-progress assignment count.
- Removed the unused settings menu from shared and legacy teacher sidebars.
- Added V15 user nickname and profile-image columns, backfilled existing nicknames from names, and initialized new signup nicknames from the entered name.
- Added a teacher my-page for nickname and JPG/PNG/WEBP profile-image updates with profile storage paths separated by PC1, PC2, and production profiles.
- Linked the dashboard profile and shared sidebar profile to my-page and applied nicknames to board author labels.

## 2026-07-15 File picker styling

- Replaced the default visible browser file picker styling with a rounded pastel upload control shared by profile-image and spreadsheet-upload forms.
- Added hover, keyboard-focus, and mobile sizing states while keeping existing drag-and-drop attachment zones unchanged.

## 2026-07-16 Dashboard notice and board integration

- Replaced the teacher dashboard's sample notice and board content with the latest notice and the three newest academy board posts.
- Added the latest class-scoped notices and recent academy board posts to the student dashboard, including content from prior dates when no notice was registered today.
- Added notice summaries, detail links, author/comment/view metadata, and empty states to the dashboard cards.
- Linked the teacher dashboard's upper-left profile brand to the same my-page route as the upper-right profile control.

## 2026-07-16 Today English Excel management and class menus

- Added a teacher Today English screen with an Excel template using the exact LEVEL1, LEVEL2, and LEVEL3 headers.
- Added replacement imports that accept different word counts per level column, skip blank cells, deduplicate words, and require at least one word per level.
- Scoped uploaded word pools and daily random selections by teacher so students only receive words uploaded by their own teacher.
- Removed the temporary generated word pool through Flyway V16 while leaving the separate Today Sentence data intact.
- Changed student dashboards to combine the student's level-specific random word with the existing level-specific Today Sentence.
- Added a Class Management navigation category below Assignment Management with Progress Management and Schedule placeholder pages.
- Added a management shortcut beside the teacher dashboard's Today Word card title that opens the Today English screen.

## 2026-07-16 Today English word meanings

- Changed the Today English workbook from three level columns to three named sheets: LEVEL1, LEVEL2, and LEVEL3.
- Standardized each sheet on the required Korean headers `단어` and `뜻`, with validation for missing sheets, headers, words, or meanings.
- Extended the shared Excel component with reusable multi-sheet template generation and parsing.
- Stored uploaded Korean meanings with each teacher-scoped word and displayed them on both teacher and student dashboards.

## 2026-07-16 Unified teacher profile navigation

- Removed dashboard-specific profile values that overwrote the shared nickname with the signup name.
- Replaced all remaining legacy teacher sidebars with the shared sidebar fragment.
- Unified the dashboard's upper-left, upper-right, and menu-page profiles on the latest saved nickname and profile image.

## 2026-07-16 Mobile menu trigger deduplication

- Removed the dashboard-only hardcoded hamburger button and kept the shared mobile navigation trigger as the single source.
- Made the mobile navigation initializer idempotent and able to reuse an existing trigger to prevent duplicate controls if scripts or markup overlap.

## 2026-07-16 Today English word query and deletion

- Converted Today English into a parent navigation category with Word Registration and Word Query submenus.
- Kept the existing level workbook upload as the Word Registration screen.
- Added a teacher-scoped word query screen with level filtering, shared pagination, word meanings, and mobile-responsive layout.
- Added individual word deletion and removed any daily selections referencing a deleted word so the dashboard can choose a replacement safely.

## 2026-07-16 Notice publish periods

- Added required notice publish start and end dates through Flyway V17, defaulting new registration forms to today.
- Added publish-period controls to notice registration and editing with validation that the end date is not before the start date.
- Limited teacher dashboard notices to currently active publish periods.
- Limited student dashboard notices to currently active periods while preserving whole-academy or enrolled-class visibility rules.

## 2026-07-16 Weekly class timetable

- Added a teacher-scoped Monday-to-Friday timetable with class, weekday, start/end time, and lesson title registration.
- Added overlap validation and course ownership checks so teachers cannot register conflicting lessons or use another teacher's class.
- Added a responsive weekly timetable layout with per-lesson deletion and an all-schedule deletion action protected by a confirmation dialog.
- Added today's registered lessons to the teacher dashboard with a shortcut to timetable management.
- Added Flyway V18 for timetable storage and automated coverage for registration, overlap prevention, teacher-scoped bulk deletion, dashboard output, and confirmation UI.
- Kept the class-name pill fitted to its text so a timetable with only one lesson does not stretch the rounded badge across the card.

## 2026-07-16 Class progress sticky notes

- Replaced the progress-management placeholder with a teacher-scoped sticky-note board.
- Added optional class and timetable selection, filtering timetable choices to the selected class in the browser.
- Added required progress memos with a 1,000-character limit and a live character counter.
- Displayed saved progress as responsive pastel sticky notes with optional `[class] [lesson]` labels, free-memo support, timestamps, and confirmed deletion.
- Preserved selected class and lesson labels as snapshots even if their original timetable is later removed.
- Added Flyway V19 and automated service/template coverage for optional selections, ownership validation, length validation, filtering metadata, and sticky-note rendering.
- Refined the progress hero into a compact lesson-log card, added dedicated styled select controls, and unified sticky notes in a soft light-wood palette.
- Changed the progress memo editor to a clean white surface with a neutral border and wood-tone focus state.
- Removed the schedule-count helper below the timetable selector so the class and timetable controls stay perfectly aligned.
- Renamed the board section to Class Progress List and added shared pagination at six newest-first notes per page.
- Assigned each new note a persistent random Windows-sticky-note color from yellow, light blue, light green, and light pink, including stable colors for existing notes through Flyway V20.

## 2026-07-16 Today English sentence management

- Added Sentence Registration and Sentence Query submenus under Today English.
- Added teacher-uploaded multi-sheet sentence workbooks using LEVEL1, LEVEL2, and LEVEL3 sheets with required `문장` and `뜻` columns.
- Removed all legacy generated sentence rows and scoped newly uploaded sentences and daily selections by teacher through Flyway V21.
- Added teacher-scoped sentence replacement, level filtering, shared pagination, deletion, and Korean meaning display on teacher and student dashboards.
- Improved the shared Excel template generator with colored bold headers, frozen header rows, filters, and wider configurable columns for both word and sentence templates.
- Reduced Korean sentence-meaning typography on teacher and student dashboards to keep Today Sentence cards compact, with an additional mobile reduction.

## 2026-07-16 Role selection and student login entry

- Replaced the root teacher-login redirect with the responsive Student/Teacher role-selection screen from the original UI template direction.
- Added a dedicated student login page and a shared authentication processing endpoint with role-aware login failure redirects.
- Routed unauthenticated student pages to the student login while keeping teacher pages on the teacher login.
- Returned both roles to the role-selection screen after logout and added portal-back links to both login screens.

## 2026-07-16 Database dump cleanup

- Removed the previous generic database dump path from the current Git tree.
- Kept only the validated dump created today under the dated filename `naenae_teacher_20260716.dump`.
- Updated the database restore guide to use the dated dump filename.

## 2026-07-21 Student signup entry

- Added a responsive signup link to the student login screen and a matching student signup page.
- Added a teacher-managed unique invitation code to My Page and normalized codes to uppercase.
- Required the teacher invitation code during student signup and used it to preserve the teacher-to-student 1:N relationship.
- Connected a signup to a matching unlinked student already registered by the inviting teacher, or created a new student under that teacher when no match exists.
- Added BCrypt password storage, duplicate-email checks, password confirmation, ambiguous-record protection, and public security access for the signup route.
- Kept the existing student record's teacher association so student data remains scoped to the correct teacher.
- Refined signup into an invitation-code-driven flow that loads the inviting teacher's active classes and then only unlinked students registered in the selected class.
- Replaced student email signup with a dedicated unique login ID while preserving existing teacher email logins through login-ID backfill in Flyway V23.
- Added a public login-ID availability check, mandatory client and server duplicate validation, and password confirmation feedback.
- Prevented signup with a student outside the selected teacher and class, and stopped signup from creating arbitrary student records.

## 2026-07-21 Student learning status menus

- Added My Learning Status as a student navigation category with Score Changes, Word Test Results, and Attendance Rate submenus.
- Added teacher-scoped student score history with chronological midterm/final ordering, latest score, average, and change from the immediately previous exam.
- Added paged word-test history limited to the student's enrolled classes, including period, class, word count, and current test status.
- Added paged attendance history with present, late, absent, excused counts and a recorded-attendance rate where present and late count as attendance.
- Added responsive desktop and mobile learning cards and automated service/template coverage.
- Removed the fixed year-order helper sentence from the student score trend panel.
- Realigned the student learning submenu with a full-width parent row, consistent indentation, a vertical hierarchy guide, and clear active-item markers on desktop and mobile navigation.

## 2026-07-21 Student profile and My Page

- Replaced the generic Student Space sidebar brand with the logged-in student's real name, student label, initial, and saved profile image.
- Made the student sidebar profile clickable from every student page and routed it to a dedicated responsive My Page.
- Added student profile-image registration and replacement using the shared profile storage property and common local file storage.
- Kept student identity fixed to the teacher-registered real name without a nickname field.
- Added image type and 5MB size validation with old-image cleanup after a successful transaction.

## 2026-07-21 Secure invitations and signup consent

- Replaced teacher-entered invitation codes with server-generated 24-character codes backed by `SecureRandom`.
- Added 30-day expiration, configurable signup limits, atomic use counting, and confirmed code reissuance that immediately invalidates the previous code.
- Invalidated legacy short invitation codes through Flyway V24 and automatically issue a secure code from teacher signup or My Page.
- Added per-client request limits to public student-signup lookup, login-ID availability, and signup actions.
- Added public responsive Terms of Service and Privacy Policy pages with operator and contact details managed through environment properties.
- Required separate terms and privacy consent for teacher and student signup, plus student age or guardian confirmation.
- Persisted legal document versions and consent timestamps through Flyway V25 for signup audit history.
- Added automated coverage for invitation expiry and consumption, signup throttling, consent persistence, and updated signup/My Page templates.

## 2026-07-21 Weekly test management

- Added Weekly Test Management under the teacher Assignment Management menu with a dedicated weekly registration flow.
- Generated test names automatically from the registration year, month, seven-day week ordinal, and required class name.
- Added optional remarks and up to five 10MB attachments using a profile-specific weekly-test storage property.
- Added newest-first teacher listings with a default two-month date range, preserved filters across shared pagination data, and clickable detail rows.
- Snapshotted active class members by the existing stable `students.id` key when each test is registered.
- Added teacher detail pages with test information, class, downloads, roster, and transactional 0-100 score entry, clearing, and updates.
- Added student Weekly Tests under My Learning Status, restricted to tests whose stored roster contains the authenticated student's mapped `student_id`.
- Added student list/detail/download views that expose only the logged-in student's score.
- Added Flyway V26 and automated service/template coverage for naming, roster identity, score validation, teacher UI, and student visibility.
- Guaranteed that active classes on the weekly-test registration form are displayed in ascending class-name order, independent of database collation, with regression coverage.
- Added the entered-student score average beside each test name in the teacher weekly-test list, with an explicit unregistered state when no score has been entered.
- Moved Attendance Rate to the first position under the student My Learning Status menu and changed the parent menu link to open Attendance Rate.

## 2026-07-21 Oracle Cloud production deployment preparation

- Rebuilt the production Dockerfile as a Java 21 multi-stage image running as a non-root user on port 8081 with configurable low-memory JVM options and an Actuator health check.
- Added a production Docker Compose stack with a loopback-only app port, private PostgreSQL 16 service, health-gated startup, persistent database/upload volumes, restart policies, and bounded JSON logs.
- Added a production environment template, Docker build exclusions, and explicit Git exclusions for production secrets and local backups.
- Hardened the production Spring profile with environment-only secrets, schema validation, Flyway ownership, disabled SQL detail logging, small HikariCP defaults, proxy header handling, and limited health/info Actuator exposure.
- Added an HTTPS Nginx reverse-proxy example targeting port 8081, deployment/restart/log/atomic database backup scripts, and a complete Oracle Cloud Ubuntu deployment runbook.

## 2026-07-21 Database snapshot refresh

- Created and validated a PostgreSQL 16 custom-format snapshot of the current database at Flyway schema V26.
- Replaced the previous dated snapshot with `db-transfer/naenae_teacher_20260721.dump` and updated the restore instructions.
