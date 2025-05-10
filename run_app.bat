@echo off
echo Running QuickRide application...

REM Check if Maven is installed
where mvn >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo Maven is not installed or not in your PATH.
    echo Please install Maven and add it to your PATH.
    echo Download Maven from: https://maven.apache.org/download.cgi
    pause
    exit /b 1
)

REM First, use Maven to compile the project
echo Compiling the project...
mvn clean compile
if %ERRORLEVEL% neq 0 (
    echo Failed to compile the project.
    pause
    exit /b 1
)

REM Then run with proper module path
echo Starting the application...
mvn javafx:run
if %ERRORLEVEL% neq 0 (
    echo Failed to run the application.
    echo Press any key to close this window...
    pause
    exit /b 1
)

echo Application closed.
echo Press any key to close this window...
pause 