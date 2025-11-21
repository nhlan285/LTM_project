@echo off
REM ========================================
REM  Test Database Connection
REM ========================================

echo.
echo ========================================
echo  Testing Database Connection
echo ========================================
echo.

REM Check if Maven is installed
where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Maven is not installed or not in PATH!
    pause
    exit /b 1
)

echo [INFO] Compiling and testing DB connection...
echo.

REM Compile and run DBContext test
call mvn compile exec:java -Dexec.mainClass="com.common.DBContext"

echo.
pause
