# xiaoma
nginx：反向代理、托管 Vue3 前端 dist 静态资源
frontend（可选，也可直接 Nginx 托管静态文件）
springboot 后端 (JRE17)：RAG 主业务服务
mysql:8.0：业务数据、用户、知识库记录
redis:7：缓存、会话、向量检索缓存、限流
milvus:2.5：向量数据库
ollama：承载 Qwen Embedding、LLM 大模型
rerank-api：独立 Reranker 重排序服务（Qwen-Rerank）
全本地模型部署
需配备显存 8G 及以上 GPU 服务器，显卡性能越高，问答响应速度越快。后端对接本地推理服务，数据内网闭环，隐私安全。
方案 2：接入第三方大模型
通过管理后台可视化配置各类第三方模型接口，无需本地高性能显卡。
底层基础
系统自建向量数据库，支持通过自定义规则采集、处理知识库数据源，实现 RAG 检索增强问答。
uni-app（Vue3 + Vite）前端 + Spring Boot 3 后端：**邮箱验证码注册 / 登录 / 个人资料编辑**，一套前端代码跑 H5、PC（响应式居中布局）和微信小程序。

## 目录结构

```
xiaoma/
├── src/                 前端（uni-app）
│   ├── pages/           首页 / 登录 / 注册 / 我的（资料编辑）
│   ├── api/request.js   请求封装（BASE_URL 在此改）
│   ├── pages.json       路由 + tabBar
│   └── manifest.json    H5 / 微信小程序配置
└── server/              后端（Spring Boot 3.5 + JPA + MySQL 8 + JWT）
    ├── src/main/java/com/xiaoma/server/
    └── src/main/resources/application.yml
```

## 启动步骤

### 1. 准备 MySQL（一次性）

**本机已装好便携版 MySQL 8.0.28**（位于 `C:\Users\59373\tools\mysql-8.0.28-winx64\`，root 密码 `mhw`，库 `xiaoma` 已建）。以后这样启动/停止：

```bash
# 启动（新开一个终端，保持窗口开着）
"C:\Users\59373\tools\mysql-8.0.28-winx64\bin\mysqld.exe" --datadir="C:\Users\59373\tools\mysql-8.0.28-winx64\data" --port=3306 --console

# 停止（另开终端）
"C:\Users\59373\tools\mysql-8.0.28-winx64\bin\mysqladmin.exe" -u root -pmhw shutdown
```

> 也可以改用官方安装包（`winget install Oracle.MySQL`）或其他已有 MySQL，只要保证存在 `xiaoma` 库：
> ```sql
> CREATE DATABASE xiaoma DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
> ```

### 2. 启动后端

修改 `server/src/main/resources/application.yml` 中 `spring.datasource.password` 为你的 MySQL 密码（或设环境变量 `DB_PASSWORD`），然后：

```bash
cd server
./mvnw.cmd -s .mvn/settings.xml spring-boot:run
```

看到 `Started ServerApplication` 即成功，接口在 http://localhost:8080 。

> **邮箱验证码**：配置环境变量 `MAIL_USERNAME`（QQ 邮箱）和 `MAIL_PASSWORD`（SMTP 授权码，QQ 邮箱 → 设置 → 账号 → 开启 SMTP 获取）即可真实发信；**不配则验证码打印在后端控制台**（`[DEV] 邮箱 xxx 的验证码：123456`），方便本地调试。

### 3. 启动前端 H5（H5 + PC 浏览器通用）

```bash
npm install
npm run dev:h5
```

浏览器打开 http://localhost:5173 ：注册（验证码看后端控制台或邮箱）→ 登录 → 底部 tab「我的」→ 编辑姓名保存。

### 4. 微信小程序

```bash
npm run dev:mp-weixin   # 保持运行，watch 编译到 dist/dev/mp-weixin
```

微信开发者工具 → 导入项目 → 目录选 `dist/dev/mp-weixin`，AppID 选「测试号」。`manifest.json` 已配 `urlCheck:false`（导入后「详情 → 本地设置 → 不校验合法域名」应已勾选，若没有手动勾一次）。

> 真机预览需要把后端部署到带 https 域名的服务器并在小程序后台配置 request 合法域名，localhost 仅供开发者工具使用。

## Docker Compose 部署（推荐）

### 1. 准备环境变量

```bash
cp .env.example .env
```

编辑 `.env`，填入真实的邮箱、Milvus、API Key 等密码。

### 2. 构建全部产物

Windows：

```bash
scripts/build-all.bat
```

Mac/Linux：

```bash
./scripts/build-all.sh
```

产物会输出到：

- `backend-front/app.jar`
- `backend-admin/app.jar`
- `frontend-front/dist`
- `frontend-admin/dist`

### 3. 启动服务

```bash
docker compose up -d
```

### 4. 访问验证

- 用户端：`http://localhost:80`
- 管理后台：`http://localhost:81`

## 接口一览

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/auth/send-code` | 发送邮箱验证码（6 位，5 分钟有效，60s 重发间隔） |
| POST | `/api/auth/register` | 注册：email + code + password + name |
| POST | `/api/auth/login` | 登录，返回 JWT token |
| GET | `/api/user/profile` | 查询个人资料（需 Bearer token） |
| PUT | `/api/user/profile` | 修改姓名（需 Bearer token） |

统一返回 `{code, message, data}`：`code=0` 成功，`code=1` 业务错误，HTTP 401 未登录。
