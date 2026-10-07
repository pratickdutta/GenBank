@echo off
setlocal

set JAVA_HOME=C:\Program Files\Java\jdk-21
set PROJECT=d:\GenBank\MiniBankingSystem
set H2_JAR=%USERPROFILE%\.m2\repository\com\h2database\h2\2.2.224\h2-2.2.224.jar
set GSON_JAR=%USERPROFILE%\.m2\repository\com\google\code\gson\gson\2.10.1\gson-2.10.1.jar
set CLASSES=%PROJECT%\target\classes
set CP=%CLASSES%;%H2_JAR%;%GSON_JAR%

echo.
echo  Compiling GenBank...
echo.

:: Gather all Java sources
dir /s /b "%PROJECT%\src\*.java" > "%PROJECT%\target\sources.txt"

:: Compile
"%JAVA_HOME%\bin\javac.exe" -d "%CLASSES%" -cp "%CP%" "@%PROJECT%\target\sources.txt"
if errorlevel 1 (
    echo [ERROR] Compilation failed. See above for errors.
    pause
    exit /b 1
)

:: Copy resources
copy /y "%PROJECT%\src\db.properties" "%CLASSES%\db.properties" >nul
xcopy /s /y "%PROJECT%\web\*" "%CLASSES%\web\" >nul 2>&1

echo.
echo  [OK] Compiled successfully!
echo.

:: Start server
echo  Starting GenBank server...
echo  Open your browser at: http://localhost:8080
echo  Press Ctrl+C to stop the server.
echo.

"%JAVA_HOME%\bin\java.exe" -cp "%CP%" WebServer

pause
