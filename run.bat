@echo off
chcp 65001 >nul
set "JDK17_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot"
set "JAVA_EXE=java"
set "JAVAC_EXE=javac"
if exist "%JDK17_HOME%\bin\java.exe" set "JAVA_EXE=%JDK17_HOME%\bin\java.exe"
if exist "%JDK17_HOME%\bin\javac.exe" set "JAVAC_EXE=%JDK17_HOME%\bin\javac.exe"

cd /d "%~dp0src"
if not exist "..\bin" mkdir "..\bin"
echo ════════════════════════════════════════
echo   เกมจีบสาว 7 Days - Compiler ^& Runner
echo ════════════════════════════════════════
echo.
echo กำลังคอมไพล์...
"%JAVAC_EXE%" -encoding UTF-8 -d ..\bin main\*.java ui\*.java shop\*.java core\*.java story\*.java save\*.java online\*.java
if errorlevel 1 (
    echo.
    echo ❌ เกิดข้อผิดพลาดในการคอมไพล์
    pause
    exit /b 1
)
echo ✓ คอมไพล์สำเร็จ
echo.
echo กำลังรันเกม...
"%JAVA_EXE%" -cp ..\bin main.MainFrame
pause
