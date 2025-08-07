# TestVWA Application Verification Script
Write-Host "=== TestVWA Application Verification ===" -ForegroundColor Green
Write-Host ""

$baseUrl = "http://localhost:8080"

# Test endpoints
$endpoints = @(
    "/",
    "/login", 
    "/register",
    "/api/users",
    "/test/",
    "/h2-console"
)

foreach ($endpoint in $endpoints) {
    $url = $baseUrl + $endpoint
    try {
        $response = Invoke-WebRequest -Uri $url -Method GET -UseBasicParsing -TimeoutSec 10
        $status = $response.StatusCode
        Write-Host "OK $endpoint - Status: $status" -ForegroundColor Green
    }
    catch {
        $status = $_.Exception.Response.StatusCode.Value__
        if ($status -eq 401 -or $status -eq 403) {
            Write-Host "PROTECTED $endpoint - Status: $status (Protected)" -ForegroundColor Yellow
        }
        else {
            Write-Host "ERROR $endpoint - Status: $status" -ForegroundColor Red
        }
    }
}

Write-Host ""
Write-Host "=== Application URLs ===" -ForegroundColor Blue
Write-Host "Main Application: $baseUrl"
Write-Host "H2 Database Console: $baseUrl/h2-console"
Write-Host "Vulnerability Test Suite: $baseUrl/test/"
Write-Host "API Endpoints: $baseUrl/api/"
Write-Host ""
Write-Host "Application is ready for vulnerability scanning!" -ForegroundColor Green
