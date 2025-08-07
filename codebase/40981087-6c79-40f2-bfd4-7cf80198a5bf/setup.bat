@echo off
echo Setting up TestVWA Development Environment...

REM Check if running as administrator
net session >nul 2>&1
if %errorLevel% == 0 (
    echo Running as Administrator - Good!
) else (
    echo ERROR: This script needs to be run as Administrator
    echo Right-click on Command Prompt and select "Run as Administrator"
    pause
    exit /b 1
)

echo.
echo Installing Java 17 and Maven using Chocolatey...
choco install openjdk17 maven -y

echo.
echo Refreshing environment variables...
refreshenv

echo.
echo Setup complete! You can now build and run the application.
echo.
echo To build: mvn clean compile
echo To run:   mvn spring-boot:run
echo.
pause
