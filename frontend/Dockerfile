# 构建阶段
FROM node:18-alpine AS build
WORKDIR /app

# 复制package.json和package-lock.json
COPY package*.json ./

# 安装依赖
RUN npm ci --only=production

# 复制源代码
COPY . .

# 设置环境变量
ENV NODE_ENV=production
ENV VITE_API_BASE_URL=http://localhost:8092

# 构建应用
RUN npm run build

# 生产环境
FROM nginx:stable-alpine
WORKDIR /usr/share/nginx/html

# 安装curl用于健康检查
RUN apk add --no-cache curl

# 从构建阶段复制构建产物
COPY --from=build /app/dist .

# 复制Nginx配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 创建日志目录
RUN mkdir -p /var/log/nginx

# 暴露端口
EXPOSE 3003

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:3003/chat_gpt_demo/ || exit 1

# 启动Nginx
CMD ["nginx", "-g", "daemon off;"] 