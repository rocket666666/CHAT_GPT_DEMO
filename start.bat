@echo off
echo 启动前端服务...

REM 创建日志目录
if not exist C:\Ai\chat_assistant\logs mkdir C:\Ai\chat_assistant\logs

REM 启动Docker容器
docker-compose down
docker-compose up -d

echo 服务已启动!
echo 请访问: http://localhost:3001/chat_sales/
pause 