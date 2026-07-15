param(
    [string]$BaseUrl = "http://localhost:8080/account-dashboard/api",
    [string]$Username = "testuser",
    [string]$Password = "123456"
)

$ErrorActionPreference = "Stop"
$OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

function Step($message) {
    Write-Host ""
    Write-Host "==> $message" -ForegroundColor Cyan
}

function Ok($message) {
    Write-Host "[OK] $message" -ForegroundColor Green
}

function Assert-Code($result, $name) {
    if ($null -eq $result) {
        throw "$name returned empty response"
    }
    if ($result.code -ne 200) {
        throw "$name failed. code=$($result.code), message=$($result.message)"
    }
    Ok "$name returned code 200"
}

$session = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$jsonHeader = @{ "Content-Type" = "application/json" }

Step "Backend availability"
try {
    Invoke-WebRequest -Uri "$BaseUrl/auth/check" -UseBasicParsing -TimeoutSec 5 -WebSession $session | Out-Null
    Ok "Backend responded"
} catch {
    $statusCode = $null
    if ($_.Exception.Response) {
        $statusCode = [int]$_.Exception.Response.StatusCode
    }
    if ($statusCode -eq 401) {
        Ok "Backend responded with 401 before login, authentication interceptor is active"
    } else {
        throw "Backend is not reachable at $BaseUrl. Start Tomcat and deploy account-dashboard.war first."
    }
}

Step "Login"
$loginBody = @{
    username = $Username
    password = $Password
} | ConvertTo-Json
$login = Invoke-RestMethod -Uri "$BaseUrl/auth/login" -Method Post -Body $loginBody -Headers $jsonHeader -WebSession $session
Assert-Code $login "POST /auth/login"

Step "Auth check"
$check = Invoke-RestMethod -Uri "$BaseUrl/auth/check" -Method Get -WebSession $session
Assert-Code $check "GET /auth/check"

Step "Categories"
$categories = Invoke-RestMethod -Uri "$BaseUrl/categories" -Method Get -WebSession $session
Assert-Code $categories "GET /categories"
if (($categories.data | Measure-Object).Count -eq 0) {
    throw "GET /categories returned no categories"
}
Ok "Category count: $(($categories.data | Measure-Object).Count)"

Step "Bills"
$bills = Invoke-RestMethod -Uri "$BaseUrl/bills?pageNum=1&pageSize=5" -Method Get -WebSession $session
Assert-Code $bills "GET /bills"

Step "Dashboard statistics"
$month = Get-Date -Format "yyyy-MM"
$year = Get-Date -Format "yyyy"
$dashboard = Invoke-RestMethod -Uri "$BaseUrl/statistics/dashboard?month=$month&year=$year" -Method Get -WebSession $session
Assert-Code $dashboard "GET /statistics/dashboard"

Step "Monthly statistics"
$monthly = Invoke-RestMethod -Uri "$BaseUrl/statistics/monthly?year=$year" -Method Get -WebSession $session
Assert-Code $monthly "GET /statistics/monthly"

Step "Category statistics"
$categoryStats = Invoke-RestMethod -Uri "$BaseUrl/statistics/category?month=$month&type=expense" -Method Get -WebSession $session
Assert-Code $categoryStats "GET /statistics/category"

Step "Smoke test finished"
Ok "Core API flow passed"
