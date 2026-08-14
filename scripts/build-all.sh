#!/usr/bin/env bash
set -e

echo "[INFO] 开始构建全部服务..."

# 构建前台后端
echo "[INFO] 构建 front-service/backend..."
cd front-service/backend
../../mvnw -s ../../.mvn/settings.xml clean package -DskipTests
cd ../..
mkdir -p backend-front
cp front-service/backend/target/xiaoma-front-server-0.0.1.jar backend-front/app.jar

# 构建后台后端
echo "[INFO] 构建 admin-service/backend..."
cd admin-service/backend
../../mvnw -s ../../.mvn/settings.xml clean package -DskipTests
cd ../..
mkdir -p backend-admin
cp admin-service/backend/target/xiaoma-admin-server-0.0.1.jar backend-admin/app.jar

# 构建用户端前端
echo "[INFO] 构建 front-service/frontend..."
cd front-service/frontend
npm install
npm run build:h5
cd ../..
mkdir -p frontend-front
cp -r front-service/frontend/dist/build/h5/* frontend-front/dist/

# 构建管理后台前端
echo "[INFO] 构建 admin-service/frontend..."
cd admin-service/frontend
npm install
npm run build
cd ../..
mkdir -p frontend-admin
cp -r admin-service/frontend/dist/* frontend-admin/dist/

echo "[INFO] 全部构建完成，产物已复制到："
echo "  - backend-front/app.jar"
echo "  - backend-admin/app.jar"
echo "  - frontend-front/dist"
echo "  - frontend-admin/dist"
