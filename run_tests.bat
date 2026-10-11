@echo off
REM =============================================================================
REM Automated Test Suite Launcher
REM ABC Precision Manufacturing Pvt. Ltd.
REM =============================================================================

echo -------------------------------------------------------------------------------
echo Running Industrial Inspection System Automated Verification Suite...
echo -------------------------------------------------------------------------------

where java >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    set "JAVA_EXE=C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot\bin\java.exe"
) else (
    set "JAVA_EXE=java"
)

"%JAVA_EXE%" -cp "target\classes;lib\javax.servlet-api-4.0.1.jar;lib\mysql-connector-j-8.3.0.jar" com.industrialinspection.SystemTestSuite

pause
