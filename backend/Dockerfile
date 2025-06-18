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

# 从构建阶段复制构建好的JAR文件
COPY --from=build /app/target/*.jar app.jar

# 复制数据文件目录
COPY documents /app/documents

# 设置环境变量
ENV SPRING_PROFILES_ACTIVE=docker
ENV JAVA_OPTS=""

# 暴露应用端口
EXPOSE 8089

# 使用启动脚本，根据环境变量设置JVM参数
COPY start.sh /app/start.sh
RUN chmod +x /app/start.sh
ENTRYPOINT ["/app/start.sh"] 