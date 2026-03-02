@echo off
REM Download Gson library from Maven Central
REM This script downloads Gson 2.10.1 JAR file

setlocal enabledelayedexpansion

set "LIB_DIR=%~dp0lib"
set "GSON_JAR=%LIB_DIR%\gson-2.10.1.jar"
set "GSON_URL=https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar"

echo.
echo ========================================
echo Downloading Gson Library
echo ========================================
echo.

if exist "%GSON_JAR%" (
    echo Gson already downloaded at: %GSON_JAR%
    goto :done
)

echo Downloading from: %GSON_URL%
echo Destination: %GSON_JAR%
echo.

powershell -Command "& {(New-Object System.Net.ServicePointManager).SecurityProtocol = [System.Net.SecurityProtocolType]::Tls12; (New-Object System.Net.WebClient).DownloadFile('%GSON_URL%', '%GSON_JAR%')}"

if errorlevel 1 (
    echo.
    echo ERROR: Failed to download Gson
    echo.
    echo Manual download: %GSON_URL%
    echo Save to: %GSON_JAR%
    exit /b 1
)

echo.
echo SUCCESS: Gson downloaded successfully
echo.

:done
echo Gson JAR location: %GSON_JAR%
