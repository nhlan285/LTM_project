@echo off
REM ========================================
REM  Start Conversion Server (Module B)
REM ========================================

echo.
echo ========================================
echo  Starting File Conversion Server
echo  Port: 9999
echo  Workers: 3 threads
echo ========================================
echo.

REM Check if Maven is installed
where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Maven is not installed or not in PATH!
    echo Please install Maven from: https://maven.apache.org/download.cgi
    pause
    exit /b 1
)

echo [INFO] Starting server...
echo [INFO] Press Ctrl+C to stop the server
echo.

REM Run the server using Maven
call mvn exec:java -Dexec.mainClass="com.server.core.ServerMain"

pause
