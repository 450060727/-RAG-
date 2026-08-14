@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

set "PORT=8081"
set "SERVICE=front-service backend"
set "PROJECT_ROOT=%~dp0.."

echo ============================================
echo  Starting %SERVICE%
echo  Port: %PORT%
echo  Bind: 0.0.0.0:%PORT%
echo ============================================
echo.

echo [1/4] Killing process occupying port %PORT%...
powershell -NoProfile -Command "Get-NetTCPConnection -LocalPort %PORT% -ErrorAction SilentlyContinue | Select-Object -Property OwningProcess -Unique | ForEach-Object { Write-Host ('    Killing PID ' + $_.OwningProcess); Stop-Process -Id $_.OwningProcess -Force -ErrorAction SilentlyContinue }"

echo [2/4] Waiting for port release...
timeout /t 1 /nobreak >nul

cd /d "%PROJECT_ROOT%"

echo [3/4] Ensuring shared-lib is installed...
call mvnw.cmd -s .mvn/settings.xml -pl shared-lib install -DskipTests

echo [4/4] Starting %SERVICE%...
call mvnw.cmd -s .mvn/settings.xml -f front-service/backend/pom.xml spring-boot:run -DskipTests -Dspring-boot.run.arguments="--server.address=0.0.0.0"

echo.
echo %SERVICE% stopped.
pause
