# NaenaeTeacher Codex Instructions

## Project Overview

NaenaeTeacher is a responsive web application for academy teachers and students.

The project is built with:

- Java 21
- Spring Boot
- Gradle
- PostgreSQL
- Thymeleaf
- Spring Security
- Spring Data JPA
- Flyway
- Bootstrap 5

The first MVP focuses on the teacher-side dashboard and teacher management features.
The student-side pages will be expanded later, but role separation must be considered from the beginning.

## URL Structure

Use the following structure:

- /teacher/**
- /student/**
- /api/teacher/**
- /api/student/**
- /api/auth/**

## Roles

The service must support these roles:

- TEACHER
- STUDENT
- ADMIN

Teacher pages and APIs must only be accessible by TEACHER.
Student pages and APIs must only be accessible by STUDENT.

## Dashboard Features

Both teacher and student dashboards must include:

- Today’s English word
- Today’s English sentence
- Notice board
- Board posts
- Attendance status

Teacher dashboard should also include:

- Today’s attendance summary
- Today’s class schedule
- Student learning status summary
- Quick actions:
  - Add student
  - Add assignment
  - Write notice
  - Check attendance
  - Register today’s English

Student dashboard should also include:

- Today’s assignment
- My learning progress
- Teacher encouragement message
- My attendance status

## UI Direction

The UI must be responsive.

Design tone:

- Clean
- Modern
- Soft
- Friendly
- Slightly cute
- Pastel color accents
- Rounded dashboard cards
- Small smile character or emoji points

Do not make the UI childish.
It should feel suitable for both academy teachers and students.

## Security Rules

- Never store plain text passwords.
- Use BCrypt for password hashing.
- Never hardcode DB credentials, JWT secrets, or API keys.
- Use environment variables.
- Student data must always be scoped by teacher_id.
- A teacher must never access another teacher’s students.

## Development Rules

- Keep the MVP small.
- Do not over-engineer.
- Prefer simple Spring Boot MVC structure first.
- Use Flyway for DB schema.
- Use Thymeleaf + Bootstrap 5 for the first MVP UI.
- Write code in a way that can later be expanded to REST API or React if needed.

## Verification

After each meaningful change, check:

- Project builds successfully.
- Application starts.
- Existing routes still work.
- Security rules are not broken.
- Flyway migration still applies cleanly.