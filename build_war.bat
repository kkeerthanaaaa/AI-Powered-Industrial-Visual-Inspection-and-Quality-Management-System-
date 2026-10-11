@echo off
REM =============================================================================
REM Compile & Build WAR Archive for Apache Tomcat
REM ABC Precision Manufacturing Pvt. Ltd.
REM =============================================================================

echo -------------------------------------------------------------------------------
echo Compiling and Packaging IndustrialInspectionSystem.war...
echo -------------------------------------------------------------------------------

where javac >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    set "JAVAC_EXE=C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot\bin\javac.exe"
    set "JAR_EXE=C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot\bin\jar.exe"
) else (
    set "JAVAC_EXE=javac"
    set "JAR_EXE=jar"
)

if not exist "target\classes" mkdir "target\classes"
if not exist "target\IndustrialInspectionSystem\WEB-INF\classes" mkdir "target\IndustrialInspectionSystem\WEB-INF\classes"
if not exist "target\IndustrialInspectionSystem\WEB-INF\lib" mkdir "target\IndustrialInspectionSystem\WEB-INF\lib"

powershell -NoProfile -Command "Get-ChildItem -Path 'src\main\java' -Filter '*.java' -Recurse | Select-Object -ExpandProperty FullName | Out-File -Encoding ascii sources.txt"
"%JAVAC_EXE%" -encoding UTF-8 -cp "lib\javax.servlet-api-4.0.1.jar;lib\mysql-connector-j-8.3.0.jar" -d "target\classes" @sources.txt
if exist sources.txt del sources.txt
if %ERRORLEVEL% NEQ 0 (
    echo Compilation failed!
    pause
    exit /b %ERRORLEVEL%
)

echo [2/3] Assembling Webapp layout...
xcopy /E /I /Y "src\main\webapp\*" "target\IndustrialInspectionSystem\" >nul
xcopy /E /I /Y "target\classes\*" "target\IndustrialInspectionSystem\WEB-INF\classes\" >nul
copy /Y "src\main\resources\db.properties" "target\IndustrialInspectionSystem\WEB-INF\classes\" >nul
copy /Y "lib\mysql-connector-j-8.3.0.jar" "target\IndustrialInspectionSystem\WEB-INF\lib\" >nul

echo [3/3] Generating target\IndustrialInspectionSystem.war...
"%JAR_EXE%" -cf "target\IndustrialInspectionSystem.war" -C "target\IndustrialInspectionSystem" .
if %ERRORLEVEL% NEQ 0 (
    echo WAR packaging failed!
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo ===============================================================================
echo Build Successful! 
echo Deploy target\IndustrialInspectionSystem.war to Apache Tomcat webapps/
echo ===============================================================================
pause
