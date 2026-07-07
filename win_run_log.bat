@echo off
setlocal enabledelayedexpansion

echo ===== Environment Check =====


where java >nul 2>&1
if errorlevel 1 (
    echo Error: Java not found in PATH. Please install JDK/JRE.
    pause
    exit /b 1
)


set JAR_FILE=target\squeeze-alice-1.0.jar
if not exist "%JAR_FILE%" (
    echo Error: %JAR_FILE% not found. Build may have failed.
    pause
    exit /b 1
)

echo ===== Starting Application =====
echo Starting server at: http://localhost:8010
echo Press Ctrl+C to stop the application
echo.

java -jar "%JAR_FILE%"

echo.
echo ===== Application Stopped =====
pause