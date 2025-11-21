@echo off
REM ========================================
REM  Build Script for Windows
REM ========================================

echo.
echo ========================================
echo  Building File Converter Project
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

echo [INFO] Maven found, starting build...
echo.

REM Clean and build project
call mvn clean package

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo  BUILD SUCCESS!
    echo ========================================
    echo.
    echo WAR file created at: target\file-converter.war
    echo.
    echo Next steps:
    echo 1. Copy target\file-converter.war to Tomcat's webapps folder
    echo 2. Run start-server.bat to start the Conversion Server
    echo 3. Access http://localhost:8080/file-converter/
    echo.
) else (
    echo.
    echo ========================================
    echo  BUILD FAILED!
    echo ========================================
    echo Please check the error messages above.
    echo.
)

pause
