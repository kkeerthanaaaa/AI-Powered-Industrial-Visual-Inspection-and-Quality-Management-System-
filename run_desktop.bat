@echo off
REM =============================================================================
REM ABC Precision Manufacturing Pvt. Ltd.
REM Industrial Visual Inspection and Quality Management System
REM Desktop Swing GUI Launcher
REM =============================================================================

echo -------------------------------------------------------------------------------
echo Starting Industrial Inspection Desktop Application...
echo Company: ABC Precision Manufacturing Pvt. Ltd.
echo -------------------------------------------------------------------------------

REM Check Java in PATH or standard JDK install
where java >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    set "JAVA_EXE=C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot\bin\java.exe"
) else (
    set "JAVA_EXE=java"
)

where javac >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    set "JAVAC_EXE=C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot\bin\javac.exe"
) else (
    set "JAVAC_EXE=javac"
)

REM Ensure output directories exist
if not exist "uploads\inspections" mkdir "uploads\inspections"
if not exist "reports\export" mkdir "reports\export"
if not exist "backups" mkdir "backups"
if not exist "target\classes" mkdir "target\classes"

echo Compiling Java classes...
powershell -NoProfile -Command "Get-ChildItem -Path 'src\main\java' -Filter '*.java' -Recurse | Select-Object -ExpandProperty FullName | Out-File -Encoding ascii sources.txt"
"%JAVAC_EXE%" -encoding UTF-8 -cp "lib\javax.servlet-api-4.0.1.jar;lib\mysql-connector-j-8.3.0.jar" -d "target\classes" @sources.txt
if exist sources.txt del sources.txt

"%JAVA_EXE%" -cp "target\classes;lib\javax.servlet-api-4.0.1.jar;lib\mysql-connector-j-8.3.0.jar" com.industrialinspection.Main

pause
