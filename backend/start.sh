#!/bin/bash

# 设置时区为北京时间
echo "设置容器时区为Asia/Shanghai..."
ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
echo "Asia/Shanghai" > /etc/timezone
date

# 检查jar文件是否存在
if [ ! -f /app/app.jar ]; then
  echo "Error: JAR file not found at /app/app.jar"
  echo "Please make sure you have mounted the JAR file correctly."
  exit 1
fi

# 检查文件权限
if [ ! -r /app/app.jar ]; then
  echo "Warning: JAR file is not readable, fixing permissions..."
  chmod +r /app/app.jar
fi

# 确保日志目录存在，并具有写入权限
LOG_DIR="/app/logs"
echo "检查日志目录: $LOG_DIR"

if [ ! -d "$LOG_DIR" ]; then
  echo "创建日志目录 $LOG_DIR..."
  mkdir -p "$LOG_DIR"
  if [ $? -ne 0 ]; then
    echo "错误：无法创建日志目录，尝试使用sudo"
    sudo mkdir -p "$LOG_DIR"
  fi
fi

# 列出日志目录内容
echo "日志目录内容:"
ls -la "$LOG_DIR"

# 设置日志目录权限
chmod -R 777 "$LOG_DIR"
echo "已设置日志目录权限为777 (rwxrwxrwx)"

# 创建测试文件确认写入权限
echo "测试日志目录写入权限..."
touch "$LOG_DIR/test_file.txt"
if [ $? -eq 0 ]; then
  echo "成功：可以写入日志目录"
  echo "这是一个测试文件，确认可以写入日志目录" > "$LOG_DIR/test_file.txt"
else
  echo "错误：无法写入日志目录，请检查挂载和权限"
fi

# 添加日志配置到JVM参数
LOGGING_OPTS="-Dlogging.file.path=$LOG_DIR -Dlogging.file.name=application.log -Duser.timezone=Asia/Shanghai"
LOGGING_OPTS="$LOGGING_OPTS -Dlogging.level.root=INFO -Dlogging.level.com.example=DEBUG"
JAVA_OPTS="$JAVA_OPTS $LOGGING_OPTS"

# 打印出启动参数以便调试
echo "Starting application with: java ${JAVA_OPTS} -jar app.jar"

# 使用传入的JVM参数启动应用
exec java ${JAVA_OPTS} -jar app.jar 