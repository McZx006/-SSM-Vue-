# PowerShell script to fix user passwords in database
$sqlFile = "D:\桌面文件\我的文件\课程代码\软件开发框架\ssmown\src\main\resources\sql\fix_passwords.sql"

# Try common MySQL installation paths
$mysqlPaths = @(
    "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe",
    "C:\Program Files\MySQL\MySQL Server 5.7\bin\mysql.exe",
    "C:\xampp\mysql\bin\mysql.exe",
    "C:\wamp\bin\mysql\mysql8.0.27\bin\mysql.exe"
)

$mysqlExe = $null
foreach ($path in $mysqlPaths) {
    if (Test-Path $path) {
        $mysqlExe = $path
        break
    }
}

if ($mysqlExe) {
    Write-Host "Found MySQL at: $mysqlExe"
    Get-Content $sqlFile | & $mysqlExe -u root -p123456 account_dashboard
    Write-Host "Password update completed!"
} else {
    Write-Host "MySQL executable not found. Please run the SQL manually:"
    Write-Host "SQL file location: $sqlFile"
}
