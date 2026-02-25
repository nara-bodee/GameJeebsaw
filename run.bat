@echo off
chcp 65001 >nul
cd /d "%~dp0src"
echo ════════════════════════════════════════
echo   เกมจีบสาว 7 Days - Compiler & Runner
echo ════════════════════════════════════════
echo.
echo กำลังคอมไพล์...
javac -encoding UTF-8 main\*.java ui\*.java shop\*.java core\*.java story\*.java
if errorlevel 1 (
    echo.
    echo ❌ เกิดข้อผิดพลาดในการคอมไพล์
    pause
    exit /b 1
)
echo ✓ คอมไพล์สำเร็จ
echo.
echo กำลังรันเกม...
java main.GameWindow
pause
