@echo off
echo ========================================
echo   Starting LAN Chat Client (localhost)
echo ========================================
java -cp "out;lib/mysql-connector.jar" client.ClientSide
pause
