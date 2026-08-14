@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

set "PROJECT_ROOT=%~dp0.."

echo ============================================
echo  Starting all xiaoma services
echo ============================================
echo.

cd /d "%PROJECT_ROOT%"

start "front-backend (8081)" cmd /c "scripts\start-front-backend.bat"
start "admin-backend (8082)" cmd /c "scripts\start-admin-backend.bat"
start "front-frontend (5173)" cmd /c "scripts\start-front-frontend.bat"
start "admin-frontend (5174)" cmd /c "scripts\start-admin-frontend.bat"

echo.
echo All services are starting in separate windows.
echo.
pause
