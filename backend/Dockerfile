# 多阶段构建
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app

# 复制pom.xml和源代码
COPY pom.xml .
COPY src ./src

# 构建应用
RUN mvn clean package -DskipTests

# 运行阶段
FROM openjdk:17-jdk-slim
WORKDIR /app

# 安装时区数据
RUN apt-get update && apt-get install -y tzdata && rm -rf /var/lib/apt/lists/*

# 从构建阶段复制构建好的JAR文件
COPY --from=build /app/target/*.jar app.jar

# 复制数据文件目录
COPY documents /app/documents

# 创建日志目录
RUN mkdir -p /app/logs

# 设置环境变量
ENV SPRING_PROFILES_ACTIVE=docker
ENV SERVER_PORT=8092
ENV JAVA_OPTS="-Xms512m -Xmx1024m -Dfile.encoding=UTF-8 -Duser.timezone=Asia/Shanghai"

# 暴露应用端口
EXPOSE 8092

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8092/chat_gpt_demo/actuator/health || exit 1

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"] 