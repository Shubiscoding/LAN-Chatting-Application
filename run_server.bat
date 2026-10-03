@echo off
echo ========================================
echo   Starting LAN Chat Server (port 5000)
echo ========================================
java -cp "out;lib/mysql-connector.jar" server.ServerSide
pause
