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

function Warn($message) {
    Write-Host "[WARN] $message" -ForegroundColor Yellow
}

$root = Split-Path -Parent $PSScriptRoot
$frontend = Join-Path $root "frontend"
$miniapp = Join-Path $root "miniapp-prototype"
$war = Join-Path $root "target\account-dashboard.war"
$ideaMaven = "D:\IDEA\IntelliJ IDEA 2024.3.5\plugins\maven\lib\maven3\bin\mvn.cmd"

Step "Project root"
Write-Host $root

Step "Mini program JavaScript syntax"
if (Test-Path $miniapp) {
    $jsFiles = Get-ChildItem -Path $miniapp -Recurse -Filter *.js
    foreach ($file in $jsFiles) {
        node --check $file.FullName | Out-Null
    }
    Ok "Checked $($jsFiles.Count) JavaScript files"
} else {
    Warn "miniapp-prototype directory not found"
}

Step "Frontend production build"
Push-Location $frontend
try {
    cmd /c npm run build
    Ok "Frontend build passed"
} finally {
    Pop-Location
}

Step "Backend Maven package"
if (Test-Path $ideaMaven) {
    & $ideaMaven clean package -DskipTests
} else {
    mvn clean package -DskipTests
}

if (Test-Path $war) {
    Ok "WAR generated: $war"
} else {
    throw "WAR file was not generated: $war"
}

Step "Optional localhost checks"
try {
    $frontendStatus = (Invoke-WebRequest -Uri "http://localhost:3000/" -UseBasicParsing -TimeoutSec 3).StatusCode
    Ok "Frontend localhost status: $frontendStatus"
} catch {
    Warn "Frontend dev server is not reachable on http://localhost:3000/. Start it with: cd frontend; cmd /c npm run dev"
}

try {
    $backendStatus = (Invoke-WebRequest -Uri "http://localhost:8080/account-dashboard/api/auth/check" -UseBasicParsing -TimeoutSec 3).StatusCode
    Ok "Backend auth check status: $backendStatus"
} catch {
    $statusCode = $null
    if ($_.Exception.Response) {
        $statusCode = [int]$_.Exception.Response.StatusCode
    }

    if ($statusCode -eq 401) {
        Ok "Backend auth check status: 401 (normal when not logged in)"
    } else {
        Warn "Backend is not reachable on http://localhost:8080/account-dashboard/. Start Tomcat in IDEA and deploy account-dashboard.war"
    }
}

Step "Verification finished"
Ok "Source checks completed"
Write-Host ""
Write-Host "Tip: after Tomcat is started, run scripts\api-smoke-test.ps1 to verify login, bills, categories and statistics APIs." -ForegroundColor DarkCyan
