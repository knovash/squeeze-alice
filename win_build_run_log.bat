@echo off
setlocal enabledelayedexpansion

echo ===== Environment Check =====

where mvn >nul 2>&1
if errorlevel 1 (
    echo Error: Maven not found in PATH. Please install Maven and add it to PATH.
    pause
    exit /b 1
)

where java >nul 2>&1
if errorlevel 1 (
    echo Error: Java not found in PATH. Please install JDK/JRE.
    pause
    exit /b 1
)

echo ===== Maven Build =====
call mvn clean package
if errorlevel 1 (
    echo Build failed with errors.
    pause
    exit /b 1
)


if not exist "target\squeeze-alice-1.0.jar" (
    echo Error: target\squeeze-alice-1.0.jar not found. Build may have failed.
    pause
    exit /b 1
)

echo ===== Starting Application =====
echo Starting server at: http://localhost:8010
echo Press Ctrl+C to stop the application
echo.

java -jar "target\squeeze-alice-1.0.jar"

pause