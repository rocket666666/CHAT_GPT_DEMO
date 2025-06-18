# Docker运维常用命令

## 容器服务管理

### 启动所有服务
```bash
# 启动所有服务
docker-compose up -d

# 启动单个服务
docker-compose up -d backend
docker-compose up -d frontend
```

### 停止服务
```bash
# 停止所有服务
docker-compose down

# 停止单个服务
docker-compose stop backend
docker-compose stop frontend
```

### 重启服务
```bash
# 重启所有服务
docker-compose restart

# 重启单个服务
docker-compose restart backend
docker-compose restart frontend
```

### 重建服务（修改Dockerfile后使用）
```bash
# 重建并启动服务
docker-compose up -d --build backend
docker-compose up -d --build frontend
```

## 日志查看

### 查看实时日志
```bash
# 查看所有服务的日志
docker-compose logs -f

# 查看指定服务的日志
docker-compose logs -f backend
docker-compose logs -f frontend

# 查看最近100行日志
docker-compose logs -f --tail=100 backend
```

### 查看历史日志
```bash
# 不跟踪，仅显示现有日志
docker-compose logs backend
```

## 容器状态监控

### 检查容器状态
```bash
# 列出所有容器状态
docker ps

# 列出所有容器(包括已停止的)
docker ps -a
```

### 检查容器资源使用情况
```bash
# 查看所有容器资源使用
docker stats

# 查看特定容器资源使用
docker stats chat-sales-backend
docker stats chat-sales-frontend
```

### 检查容器运行状态详情
```bash
# 查看容器详细信息
docker inspect chat-sales-backend
docker inspect chat-sales-frontend

# 检查容器健康状态
docker inspect --format='{{.State.Health.Status}}' chat-sales-backend
```

## 网络排查

### 检查网络配置
```bash
# 列出所有网络
docker network ls

# 检查应用网络详情
docker network inspect app-network
```

### 容器内部网络测试
```bash
# 进入容器交互式终端
docker exec -it chat-sales-backend /bin/bash

# 从容器内测试网络连接
docker exec -it chat-sales-backend ping host.docker.internal
docker exec -it chat-sales-backend curl -v http://host.docker.internal:3306
```

## 数据库连接测试

### 检查MySQL连接
```bash
# 进入容器后测试MySQL连接
docker exec -it chat-sales-backend /bin/bash
# 然后在容器内执行
mysql -h host.docker.internal -u nify_test -p

# 或使用一行命令执行
docker exec -it chat-sales-backend mysql -h host.docker.internal -u nify_test -p -e "SHOW DATABASES;"
```

## 系统维护

### 清理未使用的资源
```bash
# 清理未使用的镜像
docker image prune -a

# 清理未使用的卷
docker volume prune

# 清理所有未使用资源
docker system prune
```

### 备份和恢复

#### 备份容器文件
```bash
# 从容器复制文件到主机
docker cp chat-sales-backend:/app/logs/application.log ./backup/
```

#### 备份容器数据卷
```bash
# 创建数据卷备份
docker run --rm -v chat-sales_app-data:/source:ro -v $(pwd):/backup alpine tar -czf /backup/app-data-backup.tar.gz -C /source .
```

## Windows系统特定命令

### PowerShell环境中使用
```powershell
# 查看容器日志
docker logs chat-sales-backend

# 使用PowerShell持续查看日志
docker logs -f chat-sales-backend

# 检查防火墙规则
Get-NetFirewallRule | Where-Object {$_.DisplayName -like "*docker*"}
```

### CMD环境中使用
```cmd
:: 查看端口占用情况
netstat -ano | findstr "8089"
netstat -ano | findstr "3000"

:: 结束占用端口的进程
taskkill /F /PID <进程ID>
```

## 故障排除

### 容器无法启动
```bash
# 检查启动错误
docker-compose logs backend

# 检查有无端口冲突
netstat -ano | findstr "8089"  # Windows
lsof -i :8089  # Linux/Mac
```

### 应用无响应
```bash
# 重启应用
docker-compose restart backend

# 检查应用日志和状态
docker logs --tail 100 chat-sales-backend
docker stats chat-sales-backend
```

### 前端页面无法访问
```bash
# 检查Nginx配置
docker exec -it chat-sales-frontend cat /etc/nginx/conf.d/default.conf

# 检查前端资源是否正确挂载
docker exec -it chat-sales-frontend ls -la /usr/share/nginx/html
```

## 定时任务管理

### 查看定时任务日志
```bash
# 查看定时任务相关日志
docker-compose logs -f backend | grep "数据同步任务"

# 查看最近100行与定时任务相关的日志
docker-compose logs -f --tail=100 backend | grep "数据同步"
```

### 手动触发数据同步
```bash
# 通过API手动触发数据同步（需要验证接口是否存在）
curl -X POST "http://localhost:8089/api/sync/manual" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 修改定时任务配置
```bash
# 需要修改DataSyncScheduler.java中的cron表达式，然后重新构建

# 每小时执行一次（0分时）
# @Scheduled(cron = "0 0 * * * ?")

# 每15分钟执行一次
# @Scheduled(cron = "0 0/15 * * * ?")

# 每天凌晨2点执行一次
# @Scheduled(cron = "0 0 2 * * ?")
```

### 重启定时任务
```bash
# 重启后端服务以应用定时任务更改
docker-compose restart backend

# 查看服务是否正常启动
docker-compose logs -f backend | grep "Started"
``` 