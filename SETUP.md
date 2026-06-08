# 智能健身管理平台 — 环境搭建指南

只需安装 **Docker Desktop**，无需安装 Java / Node / MySQL。

---

## 一、安装 Docker Desktop

1. 下载：https://www.docker.com/products/docker-desktop/
2. 安装后重启电脑
3. 打开 Docker Desktop，等待左下角显示 **Engine running**

> Windows 用户需开启 WSL2（Docker Desktop 安装时会提示）

---

## 二、克隆项目

```powershell
git clone https://github.com/Irisyu0504/fitness.git
cd fitness
```

---

## 三、配置环境变量

```powershell
# 复制模板
cp .env.example .env
```

编辑 `.env` 文件，填入以下内容：

```env
# MySQL
MYSQL_ROOT_PASSWORD=1234
MYSQL_DATABASE=fitnessmanagedb

# 后端（数据库密码保持和上面一致）
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/fitnessmanagedb?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=GMT%2B8&allowPublicKeyRetrieval=true
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=1234
JWT_SECRET=SmartFitnessManagementPlatformSecretKeyForWebProjectDefaultFallback
DEEPSEEK_API_KEY=你的DeepSeek API Key
```

> `DEEPSEEK_API_KEY` 用于 AI 健身助手功能，没有的话 AI 聊天不可用，其他功能正常。

---

## 四、启动项目

```powershell
docker-compose up --build
```

首次启动需要下载镜像和构建，大约 5-10 分钟（取决于网速）。看到以下日志说明启动成功：

```
fitness-backend  | Started FitnessBackendApplication in x seconds
fitness-frontend | ...
fitness-mysql    | ready for connections
```

---

## 五、访问

| 服务 | 地址 |
|------|------|
| 前端页面 | http://localhost |
| 后端 API | http://localhost:8080 |
| 数据库 | localhost:3307 |

---

## 六、常用命令

```powershell
# 后台启动（不占用终端）
docker-compose up --build -d

# 查看日志
docker-compose logs -f

# 停止项目
docker-compose down

# 停止并删除数据库数据（重新初始化）
docker-compose down -v
```

---

## 七、常见问题

### 端口被占用
报错 `port is already allocated`，修改 `docker-compose.yml` 中的端口映射：
```yaml
ports:
  - "3308:3306"   # MySQL 改成 3308
  - "8081:8080"   # 后端改 8081
  - "81:80"       # 前端改 81
```

### Docker Desktop 启动失败
- 确认 BIOS 中已开启虚拟化（VT-x / AMD-V）
- 确认已安装 WSL2：管理员 PowerShell 运行 `wsl --install`

### 后端启动报数据库连接错误
- 等 MySQL 完全启动后再访问（healthcheck 会自动等待，一般 30 秒左右）
- 检查 `.env` 中密码是否一致
