# Canteen Management System

A Spring Boot + MongoDB app for managing canteens, workers, attendance, salaries, and meal-wise food logs with daily summaries.

## Run locally on port 8080

There are two ways to run: using embedded MongoDB (dev) or MongoDB Atlas (prod).

### A) Embedded MongoDB (dev)

This is easiest for a quick local run. It uses an embedded Mongo instance and listens on 8080 by default.

```powershell
# Windows PowerShell
Remove-Item Env:SPRING_PROFILES_ACTIVE -ErrorAction SilentlyContinue
Remove-Item Env:SPRING_DATA_MONGODB_URI -ErrorAction SilentlyContinue
Remove-Item Env:PORT -ErrorAction SilentlyContinue

# Start the app on http://localhost:8080
./mvnw.cmd -DskipTests spring-boot:run
```

### B) MongoDB Atlas (prod)

Run against your Atlas cluster (recommended for real data). Ensure your IP is whitelisted and credentials are valid.

```powershell
# Windows PowerShell
# Make sure port 8080 is free (optional)
$c = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue | Select-Object -First 1
if ($c) { Stop-Process -Id $c.OwningProcess -Force }

# Use prod profile and your Atlas URI; DB name is case-sensitive (canteenDB)
$env:SPRING_PROFILES_ACTIVE = 'prod'
$env:SPRING_DATA_MONGODB_URI = 'mongodb+srv://<user>:<pass>@<cluster-host>/canteenDB?retryWrites=true&w=majority'

# Start the app on http://localhost:8080
./mvnw.cmd -DskipTests spring-boot:run
```

Note: The default DB name is set to `canteenDB` in `src/main/resources/application.properties`. If you use a URI with a path (…/canteenDB), that takes precedence.

## Key endpoints and pages

- UI entry: `http://localhost:8080`
- Login: `http://localhost:8080/login`
- Canteen hub: `/canteen/{canteenId}`
  - Food Log: `/canteen/{canteenId}/food`
  - Attendance: `/canteen/{canteenId}/attendance`
  - Salary: `/canteen/{canteenId}/salary`
- API (selected):
  - `GET /api/canteens`
  - `GET /api/canteens/{id}`
  - `POST /api/canteens/{id}/foodlogs` with `{ mealType, platesProduced, platesSold }`
  - `GET /api/canteens/{id}/foodlogs` (today)
  - `GET /api/canteens/{id}/foodlogs/stats`
  - `GET /api/canteens/{id}/foodlogs/summary?date=YYYY-MM-DD`

## Data model notes

- Per-meal entries are stored in `food_logs`.
- Daily totals (produced, sold, revenue) are stored in collection `"food logs"` (with a space), one document per canteen per date.
- MealType values: `MORNING`, `AFTERNOON`, `NIGHT` (UI shows “Evening” but sends `NIGHT`).

## Troubleshooting

- Port already in use: free port 8080 (Windows PowerShell):
  ```powershell
  $c = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue | Select-Object -First 1
  if ($c) { Stop-Process -Id $c.OwningProcess -Force }
  ```
- Atlas case mismatch: ensure DB name is exactly `canteenDB` in your URI.
- CSRF: Tokens are auto-included via meta tags; requests from the UI include the header.

## Build & test

```powershell
./mvnw.cmd -DskipTests package   # compile fast
./mvnw.cmd test                  # run unit tests
```

## License

This project is for educational/demo purposes.

---

## Project overview (for interviews/clients)

Spring Boot + MongoDB canteen management app with a clean Thymeleaf UI. It manages canteens, workers, food logs (with daily summaries), attendance, and salary views. The app is containerized with Docker and deploys smoothly to Render using environment variables and health checks.

### Core features
- Canteens: list, open, edit, delete (card-based UI)
- Workers: register/manage workers per canteen
- Food Log: capture meal entries (produced/sold) and read fast daily summaries
- Attendance and Salary: per-canteen status pages with consistent navigation
- UI polish: brand logo, consistent header/back button, no hover jitter, accessible contrast

### Architecture
- Backend: Spring Boot 3 (Java 21), Maven wrapper
- Persistence: Spring Data MongoDB
  - Dev: Embedded Mongo (Flapdoodle) for quick local runs
  - Prod: Atlas via `SPRING_DATA_MONGODB_URI`
- Views: Thymeleaf templates + light vanilla JS for interactions
- Config: `server.port=${PORT:8080}`; prod disables embedded Mongo, expects Atlas URI
- Health: Spring Actuator `/actuator/health`
- Container: Multi-stage Dockerfile; small JRE runtime image

### Data model (high level)
- Documents: Worker, Canteen, FoodLogEntry
- Aggregation: `DailyFoodSummary` stores one doc per canteen/date for fast dashboard reads
- Typical query keys: canteenId, date, workerId, mealType

Paths to explore:
- Templates: `src/main/resources/templates/*.html`
- Static: `src/main/resources/static/{style.css,app.js,img/}`
- Domain: `src/main/java/.../{controller,service,repository,model}`

---

## Deploy with Docker (Ubuntu or any Docker host)

Build image
```bash
docker build -t anna-canteen:latest .
```

Run with MongoDB Atlas
```bash
docker run -d --name anna-canteen \
  -p 8080:8080 \
  -e PORT=8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SPRING_DATA_MONGODB_URI="mongodb+srv://<user>:<pass>@<cluster>/canteenDB?retryWrites=true&w=majority" \
  anna-canteen:latest
```

Optional: docker-compose (app + local Mongo)
```yaml
version: "3.8"
services:
  mongo:
    image: mongo:7
    ports: ["27017:27017"]
    environment:
      MONGO_INITDB_DATABASE: canteenDB
    volumes:
      - mongo_data:/data/db

  app:
    build: .
    image: anna-canteen:latest
    depends_on: [mongo]
    ports: ["8080:8080"]
    environment:
      PORT: 8080
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATA_MONGODB_URI: "mongodb://mongo:27017/canteenDB"

volumes:
  mongo_data:
```

Health check
```bash
curl -s http://localhost:8080/actuator/health
```

---

## Deploy to Render (manual)

Create Web Service → connect GitHub → select this repo (main)
- Runtime: Docker
- Root directory: `.`
- Dockerfile path: `Dockerfile`
- Health check path: `/actuator/health`
- Env vars:
  - `SPRING_PROFILES_ACTIVE=prod`
  - `SPRING_DATA_MONGODB_URI=mongodb+srv://<user>:<pass>@<cluster>/canteenDB?retryWrites=true&w=majority`

Render injects `PORT`; the app already binds to it.

---

## Real challenges faced and fixes

- PowerShell env vars not applied correctly
  - Fix: Set `$env:VAR='value'` in the same command/session; prefer Docker `-e` in prod.

- Thymeleaf/static caching hid UI changes and images
  - Fix: Place assets under `static/` and use `th:src="@{/img/logo.jpg}"`; temporarily disabled caches in prod while polishing; hard-refresh/restart.

- Hover jitter and inconsistent buttons
  - Fix: Removed transform-on-hover; standardized `.btn` and `.back-btn` styles; unified header.

- MongoDB Atlas connectivity (timeouts/auth)
  - Fix: Full URI with DB path (…/canteenDB), URL-encode passwords, allow Render egress IP (or temporary `0.0.0.0/0`).

- Render Blueprint error: `field root not found in type file.Service`
  - Fix: Changed `root` → `rootDir` in `render.yaml`.

- Port binding in PaaS
  - Fix: Dockerfile runs `-Dserver.port=${PORT}`; `application.properties` has `server.port=${PORT:8080}`.

---

## Demo script (5–7 minutes)

1) Home/Canteens
   - Show brand header and logo, consistent back button, soft background.
   - Open a canteen card.

2) Food Log
   - Add a quick meal entry; point out meal types and produced/sold.
   - Show the daily summary section updating/available.

3) Attendance and Salary
   - Navigate via the header/back button; highlight consistent UX.

4) Health and Ops
   - Show `/actuator/health` for readiness.
   - Mention Docker image and Render deployment with two env vars.

5) Close with architecture
   - Spring Boot + Mongo; embedded Mongo for dev vs Atlas for prod; DailyFoodSummary for fast reads.
