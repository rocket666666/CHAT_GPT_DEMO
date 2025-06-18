# 使用预构建文件的Docker部署指南

## Linux系统部署指南

### 准备工作

1. 确保Linux服务器已安装Docker和Docker Compose:
   ```bash
   # 安装Docker (Ubuntu/Debian)
   sudo apt update
   sudo apt install docker.io
   
   # 安装Docker Compose
   sudo curl -L "https://github.com/docker/compose/releases/download/v2.21.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
   sudo chmod +x /usr/local/bin/docker-compose
   ```

2. 创建部署目录结构:
   ```bash
   mkdir -p chat-sales/{backend,frontend,documents,logs}
   ```

### 上传文件

通过SCP或SFTP将以下文件上传到Linux服务器:

1. 后端部分:
   - `chat-sales-0.0.1-SNAPSHOT.jar` → 上传到 `chat-sales/backend/` 目录
   - `start.sh` → 上传到 `chat-sales/backend/` 目录
   - `Dockerfile.simple` → 上传到 `chat-sales/backend/` 目录
   
2. 前端部分:
   - 构建好的前端 `dist` 目录 → 上传到 `chat-sales/frontend/` 目录
   - `nginx.conf` → 上传到 `chat-sales/frontend/` 目录
   - `Dockerfile.simple` → 上传到 `chat-sales/frontend/` 目录

3. 配置文件和用户数据:
   - 将用户数据 CSV 文件上传到 `chat-sales/documents/` 目录

4. docker-compose.yml → 上传到 `chat-sales/` 目录的根目录

### 构建前端（本地操作）

在上传前，您需要在本地构建前端项目:

```bash
# 进入前端目录
cd frontend

# 安装依赖包
npm install

# 安装可能缺少的依赖
npm install @element-plus/icons-vue
npm install -D terser

# 构建生产版本
npm run build

# 检查dist目录是否生成
ls -la dist/

# 这会生成 dist 目录，将整个目录上传到服务器
```

### 设置文件权限

```bash
# 确保脚本有执行权限
chmod +x chat-sales/backend/start.sh

# 确保JAR文件有读取权限
chmod +r chat-sales/backend/chat-sales-0.0.1-SNAPSHOT.jar
```

### 外部数据库配置

本应用需要MySQL数据库支持，但Docker配置中不包含MySQL服务，您需要：

1. 确保MySQL服务器已安装并运行在主机上
2. 在主机上创建数据库`ds_test`和对应用户
   ```sql
   CREATE DATABASE ds_test;
   CREATE USER 'nify_test'@'%' IDENTIFIED BY 'nify_test';
   GRANT ALL PRIVILEGES ON ds_test.* TO 'nify_test'@'%';
   FLUSH PRIVILEGES;
   ```
3. 如果主机名不是`host.docker.internal`，需修改`application-docker.yml`中的数据库连接：
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://YOUR_HOST_IP:3306/ds_test?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
   ```

### 启动服务

1. 进入项目根目录:
   ```bash
   cd chat-sales
   ```

2. 首次构建并启动所有服务:
   ```bash
   sudo docker-compose up -d
   ```

3. 查看日志:
   ```bash
   sudo docker-compose logs -f
   ```

### 更新应用

#### 更新后端

如果需要更新后端JAR文件，只需:

1. 上传新的JAR文件，替换原来的文件
2. 重启后端容器:
   ```bash
   sudo docker-compose restart backend
   ```

#### 更新前端

如果需要更新前端代码，只需:

1. 在本地构建新版本的前端应用
2. 上传新的 `dist` 目录内容，替换服务器上的文件
3. 重启前端容器:
   ```bash
   sudo docker-compose restart frontend
   ```

### 故障排除

1. 如果遇到权限问题，检查文件权限:
   ```bash
   chmod +x backend/start.sh
   chmod +r backend/chat-sales-0.0.1-SNAPSHOT.jar
   ```
2. 如果遇到端口冲突，修改docker-compose.yml中的端口映射
3. 如果前端没有正确显示，检查 `dist` 目录中是否包含了所有必要的文件，以及 nginx.conf 配置是否正确。
4. 如果遇到数据库连接问题：
   - 确认MySQL服务已启动且能接受远程连接
   - 验证用户名密码和数据库名称是否正确
   - 检查防火墙是否允许Docker容器访问MySQL端口
   - 尝试使用具体IP地址替换`host.docker.internal`

## Windows系统部署指南

### 准备工作

1. 确保Windows服务器已安装Docker Desktop和Docker Compose
   - 从[Docker官网](https://www.docker.com/products/docker-desktop/)下载并安装Docker Desktop

2. 创建部署目录结构:
   ```cmd
   mkdir chat-sales
   cd chat-sales
   mkdir backend frontend documents logs
   ```

### 上传文件

只需将以下文件上传到Windows服务器:

1. 后端部分:
   - `chat-sales-0.0.1-SNAPSHOT.jar` → 放入 `chat-sales\backend\` 目录
   - `start.sh` → 放入 `chat-sales\backend\` 目录
   - `Dockerfile.simple` → 放入 `chat-sales\backend\` 目录
   
2. 前端部分:
   - 构建好的前端 `dist` 目录 → 放入 `chat-sales\frontend\` 目录
   - `nginx.conf` → 放入 `chat-sales\frontend\` 目录
   - `Dockerfile.simple` → 放入 `chat-sales\frontend\` 目录

3. 配置文件和用户数据:
   - 用户数据CSV文件 → 放入 `chat-sales\documents\` 目录

4. docker-compose.yml → 放入 `chat-sales\` 根目录

### 外部数据库配置

Windows系统下需要配置外部MySQL数据库：

1. 确保MySQL服务已安装并运行在Windows主机上
2. 在MySQL中创建数据库和用户：
   ```sql
   CREATE DATABASE ds_test;
   CREATE USER 'nify_test'@'%' IDENTIFIED BY 'nify_test';
   GRANT ALL PRIVILEGES ON ds_test.* TO 'nify_test'@'%';
   FLUSH PRIVILEGES;
   ```
3. 修改防火墙设置，允许Docker容器访问MySQL端口(3306)
4. 如果Docker无法通过`host.docker.internal`访问主机，请修改`application-docker.yml`：
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://YOUR_WINDOWS_IP:3306/ds_test?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
   ```

### 构建前端（本地操作）

在上传前，您需要在本地构建前端项目:

```bash
# 进入前端目录
cd frontend

# 安装依赖包
npm install

# 安装可能缺少的依赖
npm install @element-plus/icons-vue
npm install -D terser

# 构建生产版本
npm run build

# 检查dist目录是否生成
dir dist

# 这会生成 dist 目录，将整个目录上传到服务器
```

### 启动服务

1. 打开PowerShell或命令提示符，进入项目根目录:
   ```cmd
   cd path\to\chat-sales
   ```

2. 首次构建并启动所有服务:
   ```cmd
   docker-compose up -d
   ```

3. 查看日志:
   ```cmd
   docker-compose logs -f
   ```

### 更新应用

#### 更新后端

如需更新后端:
1. 用新的JAR文件替换 `backend\chat-sales-0.0.1-SNAPSHOT.jar`
2. 重启后端容器:
   ```cmd
   docker-compose restart backend
   ```

#### 更新前端

如需更新前端:
1. 在本地构建新版本前端
2. 用新的 `dist` 目录内容替换服务器上的文件
3. 重启前端容器:
   ```cmd
   docker-compose restart frontend
   ```

### 故障排除

1. 如遇Docker容器无法启动，检查Windows系统防火墙设置
2. 文件路径问题，确保使用Windows风格路径（反斜杠`\`）
3. 如前端无法正确显示，检查 `dist` 目录内容和 nginx.conf 配置
4. 如果遇到数据库连接问题：
   - 确认MySQL服务已启动且能接受远程连接
   - 验证用户名密码和数据库名称是否正确
   - 检查防火墙是否允许Docker容器访问MySQL端口
   - 尝试使用具体IP地址替换`host.docker.internal`