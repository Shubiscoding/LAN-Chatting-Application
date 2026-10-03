@echo off
echo ========================================
echo   Compiling LAN Chat Application...
echo ========================================
javac -d out -cp "lib/mysql-connector.jar" src/shared/*.java src/database/*.java src/filemanagement/*.java src/server/*.java src/client/*.java src/ui/*.java
if %ERRORLEVEL% EQU 0 (
    echo [OK] Compilation successful!
) else (
    echo [FAIL] Compilation failed.
)
pause
