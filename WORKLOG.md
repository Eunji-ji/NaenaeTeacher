# NaenaeTeacher Worklog

## Current Goal

Implement teacher-side student management with multi-class registration.

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

## Next Resume Point

Restart the Spring Boot app and verify `/teacher/students` manual registration and class filtering.

## Important Notes

- Do not implement full business features yet.
- Initial goal is project foundation.
- Keep structure clean for future teacher/student role separation.
