@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

set "PORT=5174"
set "SERVICE=admin-service frontend"
set "PROJECT_ROOT=%~dp0.."

echo ============================================
echo  Starting %SERVICE%
echo  Port: %PORT%
echo  Bind: 0.0.0.0:%PORT% (via --host)
echo ============================================
echo.

echo [1/4] Killing process occupying port %PORT%...
powershell -NoProfile -Command "Get-NetTCPConnection -LocalPort %PORT% -ErrorAction SilentlyContinue | Select-Object -Property OwningProcess -Unique | ForEach-Object { Write-Host ('    Killing PID ' + $_.OwningProcess); Stop-Process -Id $_.OwningProcess -Force -ErrorAction SilentlyContinue }"

echo [2/4] Waiting for port release...
timeout /t 1 /nobreak >nul

echo [3/4] Checking dependencies...
cd /d "%PROJECT_ROOT%\admin-service\frontend"
if not exist "node_modules\" (
    echo     node_modules not found, running npm install...
    call npm install
)

echo [4/4] Starting %SERVICE%...
call npm run dev -- --host

echo.
echo %SERVICE% stopped.
pause
