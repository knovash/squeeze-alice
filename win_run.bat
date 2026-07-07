@echo off
setlocal enabledelayedexpansion

echo ===== Environment Check =====


where java >nul 2>&1
if errorlevel 1 (
    echo Error: Java not found in PATH. Please install JDK/JRE.
    pause
    exit /b 1
)




echo ===== Starting Application =====
echo Starting server at: http://localhost:8010
echo.

start "Squeeze Alice" javaw -jar "target\squeeze-alice-1.0.jar"



echo.
echo ===== Application Stopped =====
pause