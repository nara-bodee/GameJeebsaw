@echo off
REM Simple Gson download script using curl
REM Usage: Run this from the GameJeebsaw folder before compiling

setlocal enabledelayedexpansion

echo ========================================
echo DEV BIRD Gson Setup
echo ========================================
echo.

set "LIB_DIR=lib"
set "GSON_JAR=%LIB_DIR%\gson-2.10.1.jar"
set "GSON_URL=https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar"

REM Create lib directory
if not exist "%LIB_DIR%" (
    echo Creating lib directory...
    mkdir "%LIB_DIR%" || (
        echo ERROR: Could not create lib directory
        exit /b 1
    )
)

REM Check if already downloaded
if exist "%GSON_JAR%" (
    echo.
    echo Gson already downloaded:
    echo   %GSON_JAR%
    echo.
    echo Ready to compile!
    goto :done
)

echo.
echo Downloading Gson library...
echo From: %GSON_URL%
echo To:   %GSON_JAR%
echo.

REM Try using curl (Windows 10+ has curl)
where curl >nul 2>&1
if %ERRORLEVEL% equ 0 (
    echo Using curl...
    curl -L -o "%GSON_JAR%" "%GSON_URL%"
    if !ERRORLEVEL! equ 0 (
        echo.
        echo SUCCESS: Gson downloaded successfully!
        echo Ready to compile!
        goto :done
    ) else (
        echo.
        echo WARNING: curl failed
        echo.
    )
)

REM If curl failed or not available, try PowerShell
echo Trying PowerShell...
powershell -NoProfile -Command "try { [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri '%GSON_URL%' -OutFile '%GSON_JAR%' } catch { exit 1 }"

if %ERRORLEVEL% equ 0 (
    echo.
    echo SUCCESS: Gson downloaded successfully!
    echo Ready to compile!
    goto :done
)

REM If both failed, provide manual instructions
echo.
echo ERROR: Automatic download failed
echo.
echo Please download Gson manually:
echo   1. Open URL: %GSON_URL%
echo   2. Save file as: %GSON_JAR%
echo.
echo Then run compile script:
echo   powershell -NoProfile -ExecutionPolicy Bypass -File scripts/compile.ps1
echo.
exit /b 1

:done
echo.
echo Next step: Compile with scripts\compile.ps1
echo.
