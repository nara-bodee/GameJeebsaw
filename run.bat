@echo off
chcp 65001 >nul
cd /d "%~dp0src"
if not exist "..\bin" mkdir "..\bin"
echo ════════════════════════════════════════
echo   เกมจีบสาว 7 Days - Compiler & Runner
echo ════════════════════════════════════════
echo.
echo กำลังคอมไพล์...
javac -encoding UTF-8 -d ..\bin main\*.java ui\*.java shop\*.java core\*.java story\*.java save\*.java online\*.java
if errorlevel 1 (
    echo.
    echo ❌ เกิดข้อผิดพลาดในการคอมไพล์
    pause
    exit /b 1
)
echo ✓ คอมไพล์สำเร็จ
echo.
echo กำลังรันเกม...
java -cp ..\bin main.GameWindow
pause
