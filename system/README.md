# 部署到公网（Docker Compose）

一条命令拉起 MySQL + Spring Boot 后端 + Nginx 前端。公网只需暴露 **80** 端口。

## 架构

```
公网 → :80 nginx
         ├─ /       静态前端（SPA fallback）
         └─ /api/   → backend:8090
       backend     （仅 Docker 内网）
       mysql       （仅 Docker 内网，数据存卷）
```

## 前置：买一台服务器

- 阿里云 / 腾讯云**轻量应用服务器**，2核2G 起，系统选 **Ubuntu 22.04**。
- 安全组放行入站：**22**（SSH）、**80**（网站）。
- 记下公网 IP。

## 一、安装 Docker（服务器上执行）

```bash
curl -fsSL https://get.docker.com | sh
sudo systemctl enable --now docker
docker compose version   # 确认 compose 插件已装
```

## 二、上传代码

任选其一：

```bash
# 方式 A：服务器直接 clone（需仓库地址）
git clone <你的仓库地址> && cd <仓库>/system

# 方式 B：本地上传（在本地执行）
scp -r system root@<公网IP>:/root/
# 之后服务器上 cd /root/system
```

## 三、配置密钥

```bash
cd system
cp .env.example .env
# 编辑 .env，填入强密码和随机 JWT 密钥
# 生成随机密钥示例：
openssl rand -base64 32
```

`.env` 内容示例：

```
DB_PASSWORD=你的强数据库密码
JWT_SECRET=上面生成的随机字符串
```

> `.env` 已在 `.gitignore` 中，不会被提交。

## 四、启动

```bash
docker compose up -d --build
```

首次会构建前后端镜像并初始化数据库，约需几分钟。查看状态与日志：

```bash
docker compose ps
docker compose logs -f backend
```

三个容器都 `Up` 即可。MySQL 有健康检查，后端会等它就绪再启动。

## 五、访问

浏览器打开 `http://<公网IP>/` → 应看到登录页。

内置测试账号（来自 `init.sql`，**上线前请务必修改或删除**）：

| 账号 | 密码 | 角色 |
|------|------|------|
| admin | admin123 | 管理员 |
| zhangsan | 123456 | 普通用户 |

## 日常运维

```bash
docker compose logs -f              # 看日志
docker compose restart backend      # 重启单个服务
git pull && docker compose up -d --build   # 更新代码后重新部署
docker compose down                 # 停止（保留数据库卷）
docker compose down -v              # 停止并删除数据库数据（慎用）
```

数据保存在 `mysql-data` 卷中，`down` 不会丢；只有 `down -v` 才清空。

## 后续上域名 + HTTPS

架构无需改动。把域名解析到该 IP 后，把 nginx 换成带 TLS 的配置，或用 [Caddy](https://caddyserver.com/) 自动申请 Let's Encrypt 证书。

## 安全提醒

- 上线前**必须**：改 `.env` 中的 `DB_PASSWORD` 和 `JWT_SECRET`（不要沿用示例值）。
- 用户密码已用 **BCrypt** 哈希后存储于数据库（见后端 `PasswordUtil`），非明文。
- 内置测试账号 `admin/admin123`、`zhangsan/123456` 是**公开的默认口令**，公网部署前请修改密码或删除。
- 仅暴露 80 端口；后端 8090 与 MySQL 不对公网开放。
