# 测试登录API脚本
Write-Host "=== 测试登录API ===" -ForegroundColor Cyan

# 设置请求URL
$url = "http://localhost:8080/ssmown_war/api/auth/login"

# 准备请求体
$body = @{
    username = "testuser"
    password = "123456"
} | ConvertTo-Json

Write-Host "`n请求URL: $url" -ForegroundColor Yellow
Write-Host "请求体: $body" -ForegroundColor Yellow

try {
    # 发送POST请求
    Write-Host "`n发送请求..." -ForegroundColor Green
    $response = Invoke-RestMethod -Uri $url -Method POST -Body $body -ContentType "application/json; charset=utf-8"
    
    Write-Host "`n✅ 请求成功!" -ForegroundColor Green
    Write-Host "响应内容:" -ForegroundColor Cyan
    $response | ConvertTo-Json -Depth 10
    
} catch {
    Write-Host "`n❌ 请求失败!" -ForegroundColor Red
    Write-Host "错误信息: $($_.Exception.Message)" -ForegroundColor Red
    
    if ($_.Exception.Response) {
        Write-Host "`nHTTP状态码: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
        
        # 读取响应流
        try {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $responseBody = $reader.ReadToEnd()
            $reader.Close()
            
            Write-Host "`n服务器响应内容:" -ForegroundColor Yellow
            Write-Host $responseBody -ForegroundColor White
        } catch {
            Write-Host "无法读取响应内容" -ForegroundColor Gray
        }
    }
}

Write-Host "`n=== 测试完成 ===" -ForegroundColor Cyan
