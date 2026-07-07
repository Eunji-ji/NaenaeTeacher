# NaenaeTeacher Worklog

## Current Goal

Add a login-ready test teacher account and continue end-to-end dashboard verification.

## Current Status

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

## Next Tasks

1. Restart the Spring Boot app so Flyway applies `V2__seed_test_teacher.sql`.
2. Log in with `teacher@naenae.com` / `password1234`.
3. Verify successful redirect to `/teacher/dashboard`.
4. Move future teacher/student screens into `src/main/webapp/WEB-INF/views` using the new dashboard CSS/component style as the baseline.
5. Wire dashboard cards to real service data instead of static placeholders.
6. Optionally clean up the old invalid Java 18 registry entry warning if it appears during local Gradle runs.

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

## Next Resume Point

Restart the Spring Boot app and log in with `teacher@naenae.com` / `password1234`; verify that `/teacher/dashboard` renders with `/assets/css/naenae-dashboard.css`.

## Important Notes

- Do not implement full business features yet.
- Initial goal is project foundation.
- Keep structure clean for future teacher/student role separation.
