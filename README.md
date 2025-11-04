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
