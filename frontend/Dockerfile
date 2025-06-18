FROM node:16 AS build
WORKDIR /app

# 复制package.json和package-lock.json
COPY package*.json ./

# 安装依赖
RUN npm ci

# 复制源代码
COPY . .

# 构建应用
RUN npm run build

# 生产环境
FROM nginx:stable-alpine
WORKDIR /usr/share/nginx/html

# 从构建阶段复制构建产物
COPY --from=build /app/dist .

# 复制Nginx配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 暴露端口
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"] 