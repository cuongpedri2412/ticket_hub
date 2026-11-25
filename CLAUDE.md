# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

TicketHub is an event-ticketing platform (seat selection, timed seat holds, payment, QR tickets, check-in, flash sale) built as an 8-week Java Spring Boot **learning project**. Stack: Java 21, Spring Boot 3.5, MariaDB 11.4, Flyway, Spring Data JPA, JUnit 5, Testcontainers. Only the Event feature exists so far; the README's roadmap table lists the planned features per week (Venue/Seat, Booking, Payment, Auth/JWT, Ticket QR, Notification, Report/Flash sale with Redis).

All user-facing text (error messages, validation messages, comments, README) is in **Vietnamese** — keep new messages and comments in Vietnamese. Comments marked `Câu hỏi tự học` / `Bài tập` are deliberate study questions for the learner; don't remove them or answer them in code unless asked.

## Commands

Run from `tickethub/`. On Windows use `mvnw.cmd` instead of `./mvnw`.

```bash
docker compose up -d                 # start MariaDB (localhost:3306, db/user/pass: tickethub)
./mvnw spring-boot:run               # run app on :8080
./mvnw test                          # all tests
./mvnw test -Dtest=EventTest         # single test class
./mvnw test -Dtest=EventTest#methodName   # single test method
./mvnw clean package                 # build jar
```

- Swagger UI: http://localhost:8080/swagger-ui.html · Health: http://localhost:8080/actuator/health
- DB connection is overridable via `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` env vars.
- `EventApiIntegrationTest` uses Testcontainers (`mariadb:11.4`) and is **silently skipped if Docker isn't running** (`disabledWithoutDocker = true`). Testcontainers is pinned to 1.21.4+ because Docker Desktop 29+ requires it.

## Architecture

Layered architecture in `com.tickethub`, one-way dependency: `controller → service → repository → DB`. Controllers never call repositories. New features add one class per layer (e.g. `BookingController`, `BookingService`, `BookingRepository`, `Booking` entity, `BookingRequest`/`BookingResponse` DTOs in `dto/request` and `dto/response`).

Key cross-cutting design (spread across several files):

- **Rich domain model.** Entities have no public setters and a `protected` no-arg constructor for JPA. Business rules and state transitions live in entity methods (`Event.publish(now)`, `cancel()`, `updateDetails(...)`), which throw `BusinessException`. Services only orchestrate: load, call the entity method, return a DTO — they don't re-validate rules and rely on Hibernate dirty checking rather than calling `save()` on updates. Services are `@Transactional(readOnly = true)` at class level with `@Transactional` on write methods.
- **Time.** Never call `Instant.now()` directly; inject the `Clock` bean (`ClockConfig`) and pass `now` into entity methods so tests can use `Clock.fixed`. All timestamps are `Instant`, stored as UTC in `DATETIME(6)` columns (Hibernate `jdbc.time_zone: UTC`, MariaDB container also set to UTC).
- **Entities** extend `BaseEntity` (IDENTITY id + `createdAt`/`updatedAt` filled by JPA auditing via `JpaAuditingConfig`). `Event` uses `@Version` optimistic locking; conflicts become HTTP 409 `CONCURRENT_MODIFICATION`.
- **Errors.** Add new codes to the `ErrorCode` enum (each carries its `HttpStatus` and default message) and `throw new BusinessException(ErrorCode.X[, message])`; `ResourceNotFoundException` for missing ids. `GlobalExceptionHandler` maps everything to the uniform `ErrorResponse` JSON (`code`, `message`, `path`, `timestamp`, `errors[]`). Don't try/catch in controllers.
- **API.** Controllers return DTOs only (never entities), with `EventResponse.from(entity)`-style mappers and `PageResponse.from(page, mapper)` for paging. Routes are under `/api/v1/...`; create returns 201 with a `Location` header. Pageable max size is 100.
- **Schema.** Managed exclusively by Flyway (`src/main/resources/db/migration`); Hibernate runs with `ddl-auto: validate`, so entity mappings must match the migrations exactly. Never edit an existing migration — add a new `V{n}__description.sql`. `open-in-view` is disabled, so lazy associations must be loaded inside the service transaction.

## Tests

Three tiers, mirroring the layers:
- Entity unit tests (`EventTest`) — pure JUnit, no Spring.
- Web slice tests (`EventControllerTest`) — `@WebMvcTest` with the service replaced by `@MockitoBean`; covers validation and error mapping.
- Integration tests (`EventApiIntegrationTest`) — `@SpringBootTest` + MockMvc against real MariaDB via `@ServiceConnection`; also verifies Flyway migrations match entities. Use far-future dates (e.g. 2099) so time-based rules don't fail.
