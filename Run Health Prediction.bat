@echo off
setlocal
cd /d "%~dp0"

if not exist "target\HealthPredictionSystem.jar" (
    echo Application JAR not found. Build it first with: mvn package
    pause
    exit /b 1
)

java -jar "target\HealthPredictionSystem.jar"
if errorlevel 1 (
    echo The application could not be started.
    pause
    exit /b 1
)
