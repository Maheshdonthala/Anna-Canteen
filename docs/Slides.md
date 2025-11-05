---
marp: true
paginate: true
theme: default
class: lead
---

# Anna Canteen Management

Spring Boot + MongoDB + Thymeleaf

Dockerized • Render‑ready • Daily food summaries

---

## Problem → Solution

- Need to manage canteens, workers, meals, attendance, salaries
- Provide fast, clean UI for daily operations
- Solution: Spring Boot app with MongoDB
  - Server‑rendered UI (Thymeleaf)
  - Daily summary aggregation for quick reads

---

## Architecture

- Spring Boot 3 (Java 21), Maven wrapper
- Spring Data MongoDB (dev: embedded, prod: Atlas)
- Thymeleaf + light JS (`static/app.js`)
- Controllers → Services → Repositories
- Health: `/actuator/health`
- Docker multi‑stage, `-Dserver.port=${PORT}`

---

## Key Features

- Canteens: list/open/edit/delete (card UI)
- Workers: manage per canteen
- Food Log: meals (produced/sold) + daily summaries
- Attendance & Salary pages
- UX polish: brand logo, consistent header/back, no hover shift

---

## UI: Canteens Page

![Canteens page screenshot](img/canteens.png)

Tip: Save a screenshot as `docs/img/canteens.png` to render here. Include the header/logo and the cards.

---

## UI: Food Log Daily Summary

![Food Log daily summary screenshot](img/foodlog_summary.png)

Tip: Save a screenshot as `docs/img/foodlog_summary.png` highlighting quick entry and daily summary.

---

## Data Model

- Documents: Worker, Canteen, FoodLogEntry
- Aggregation: `DailyFoodSummary` (date × canteen)
- Common keys: canteenId, date, mealType, workerId

---

## DevOps & Deployment

- Local: `mvn spring-boot:run` (embedded Mongo)
- Docker: build & run with Atlas URI
- Render (manual): Docker runtime
  - Env: `SPRING_PROFILES_ACTIVE=prod`
  - Env: `SPRING_DATA_MONGODB_URI=.../canteenDB?retryWrites=true&w=majority`
  - Health: `/actuator/health`

---

## Challenges → Fixes

- PowerShell env issues → set `$env:VAR` correctly; Docker `-e`
- UI cache/logo issues → assets in `static/`, `th:src`, temp disable caches
- Hover jitter/buttons → remove transform; standardize `.btn` & `.back-btn`
- Atlas errors → DB in URI, URL‑encode pass, allow egress IP
- Render Blueprint `root` error → use `rootDir`
- Port binding → `server.port=${PORT:8080}` & `-Dserver.port=${PORT}`

---

## Demo (3–5 min)

1. Canteens page → open a canteen
2. Food Log → add entry, show daily summary
3. Attendance & Salary overview
4. `/actuator/health` + mention Docker/Render envs

---

## Thank you

Repo: github.com/Maheshdonthala/Anna-Canteen

Questions?
