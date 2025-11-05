# Canteen Management System — One‑Pager

A production‑ready Spring Boot + MongoDB app to manage canteens, workers, food logs, attendance, and salaries. Clean Thymeleaf UI, Dockerized runtime, and Render deployment.

## Snapshot
- Stack: Spring Boot 3 (Java 21), Spring Data MongoDB, Thymeleaf, Maven Wrapper, Docker
- Dev vs Prod DB: Embedded Mongo (dev), MongoDB Atlas (prod via `SPRING_DATA_MONGODB_URI`)
- Health: `/actuator/health`
- Port: `server.port=${PORT:8080}` (works locally and on PaaS)
- Repo: https://github.com/Maheshdonthala/Anna-Canteen

## Core Features
- Canteens: list, open, edit, delete (card-style UI)
- Workers: register/manage per canteen
- Food Log: capture meal entries (produced/sold) and fast daily summaries
- Attendance & Salary: per-canteen views with consistent navigation
- UX polish: brand logo, unified header/back button, no hover jitter, accessible contrast

## Architecture
- Controllers → Services → Repositories (Spring Data)
- Thymeleaf views with light vanilla JS (`static/app.js`)
- Daily aggregation model: `DailyFoodSummary` for quick dashboard reads
- Config split:
  - `application.properties` (dev): embedded Mongo; `PORT` fallback
  - `application-prod.properties` (prod): excludes embedded Mongo; expects `SPRING_DATA_MONGODB_URI`
- Docker: multi-stage build → small JRE image; `ENTRYPOINT` binds to `PORT`

## Data Model (high level)
- Documents: Worker, Canteen, FoodLogEntry
- Aggregates: DailyFoodSummary (per date × canteen)
- Common keys: canteenId, date, workerId, mealType

## Deploy (quick)
- Docker (Atlas):
  - Build: `docker build -t anna-canteen:latest .`
  - Run: `docker run -d -p 8080:8080 -e PORT=8080 -e SPRING_PROFILES_ACTIVE=prod -e SPRING_DATA_MONGODB_URI="mongodb+srv://<user>:<pass>@<cluster>/canteenDB?retryWrites=true&w=majority" anna-canteen:latest`
- Render (manual): Runtime Docker; Health `/actuator/health`; Env: `SPRING_PROFILES_ACTIVE=prod`, `SPRING_DATA_MONGODB_URI=<atlas-uri>`

## Real Challenges → Solutions
- PowerShell env not applied → Set `$env:VAR` correctly; use Docker `-e` for prod
- UI not updating / logo missing → Place assets under `static/`; `th:src="@{/img/logo.jpg}"`; temporarily disable caches; hard refresh
- Hover jitter / inconsistent buttons → Removed transform on hover; standardized `.btn` & `.back-btn` styles
- Atlas connection errors → Full URI with `/canteenDB`, URL‑encode passwords, allow Render egress IPs (or temporary `0.0.0.0/0`)
- Render Blueprint error (`root`) → Use `rootDir` in `render.yaml`
- Port binding on PaaS → `-Dserver.port=${PORT}` and `server.port=${PORT:8080}`

## 5‑Minute Demo Flow
1) Open home/canteens → show header/logo, cards; open a canteen
2) Food Log → add entry (produced/sold); show daily summary
3) Attendance & Salary → consistent header/back UX
4) Ops → show `/actuator/health`; mention Docker + Render env vars
5) Wrap → architecture split (dev vs prod DB, Daily summaries)

