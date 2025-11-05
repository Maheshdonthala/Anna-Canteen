$ErrorActionPreference = 'Stop'
# NOTE: Do not hardcode secrets here. Ensure SPRING_DATA_MONGODB_URI is set in your environment
# before running this script, for example:
#   $env:SPRING_DATA_MONGODB_URI = 'mongodb+srv://<user>:<pass>@<cluster>/canteenDB?retryWrites=true&w=majority'
if (-not $env:SPRING_DATA_MONGODB_URI) {
    Write-Error "SPRING_DATA_MONGODB_URI is not set. Please set it in this PowerShell session or use Docker/Render env vars."
    exit 1
}
Write-Output "Starting Spring Boot (mvnw spring-boot:run) as background process..."
$proc = Start-Process -FilePath '.\mvnw.cmd' -ArgumentList 'spring-boot:run' -NoNewWindow -PassThru -WorkingDirectory (Get-Location)
Write-Output "PID: $($proc.Id)"

# wait for port 8080
$started = $false
for ($i=0; $i -lt 60; $i++) {
    $r = Test-NetConnection -ComputerName 'localhost' -Port 8080
    if ($r.TcpTestSucceeded) { $started = $true; break }
    Start-Sleep -Seconds 2
}
if (-not $started) {
    Write-Error "Application did not start within timeout, check logs."
    exit 1
}
Write-Output "Application is listening on port 8080. Running worker tests..."

# run the test script
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\test_workers.ps1

Write-Output "Tests completed. Leaving Spring Boot process running (PID $($proc.Id))."
