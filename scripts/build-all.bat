@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo [INFO] 开始构建全部服务...

:: 构建前台后端
echo [INFO] 构建 front-service/backend...
cd front-service/backend
call ..\..\mvnw.cmd -s ..\..\.mvn\settings.xml clean package -DskipTests
cd ..\..
if not exist backend-front mkdir backend-front
copy /Y front-service\backend\target\xiaoma-front-server-0.0.1.jar backend-front\app.jar

:: 构建后台后端
echo [INFO] 构建 admin-service/backend...
cd admin-service/backend
call ..\..\mvnw.cmd -s ..\..\.mvn\settings.xml clean package -DskipTests
cd ..\..
if not exist backend-admin mkdir backend-admin
copy /Y admin-service\backend\target\xiaoma-admin-server-0.0.1.jar backend-admin\app.jar

:: 构建用户端前端
echo [INFO] 构建 front-service/frontend...
cd front-service/frontend
call npm install
call npm run build:h5
cd ..\..
if not exist frontend-front mkdir frontend-front
xcopy /E /I /Y front-service\frontend\dist\build\h5 frontend-front\dist

:: 构建管理后台前端
echo [INFO] 构建 admin-service/frontend...
cd admin-service/frontend
call npm install
call npm run build
cd ..\..
if not exist frontend-admin mkdir frontend-admin
xcopy /E /I /Y admin-service\frontend\dist frontend-admin\dist

echo [INFO] 全部构建完成，产物已复制到：
echo   - backend-front/app.jar
echo   - backend-admin/app.jar
echo   - frontend-front/dist
echo   - frontend-admin/dist
