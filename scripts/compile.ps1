$ErrorActionPreference = 'Stop'

$workspaceRoot = Split-Path -Path $PSScriptRoot -Parent
Set-Location (Join-Path $workspaceRoot 'src')

$javacPath = $null
$javacCommand = Get-Command javac -ErrorAction SilentlyContinue
if ($javacCommand) {
    $javacPath = $javacCommand.Source
}

if (-not $javacPath -and $env:JAVA_HOME) {
    $candidate = Join-Path $env:JAVA_HOME 'bin\javac.exe'
    if (Test-Path $candidate) {
        $javacPath = $candidate
    }
}

if (-not $javacPath) {
    $adoptium = Get-ChildItem 'C:\Program Files\Eclipse Adoptium' -Directory -ErrorAction SilentlyContinue |
        Sort-Object Name -Descending |
        Select-Object -First 1
    if ($adoptium) {
        $candidate = Join-Path $adoptium.FullName 'bin\javac.exe'
        if (Test-Path $candidate) {
            $javacPath = $candidate
        }
    }
}

if (-not $javacPath) {
    Write-Error 'javac not found. Install JDK 21 and try again.'
    exit 1
}

if (-not (Test-Path '..\bin')) {
    New-Item -ItemType Directory -Path '..\bin' | Out-Null
}

# Check for Gson library
$gsonJar = '..\lib\gson-2.10.1.jar'
if (-not (Test-Path $gsonJar)) {
    Write-Host "=================================================="
    Write-Host "Gson library not found!"
    Write-Host "=================================================="
    Write-Host ""
    Write-Host "Please download Gson manually:"
    Write-Host "  URL: https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar"
    Write-Host "  Save to: $gsonJar"
    Write-Host ""
    Write-Host "Alternative: Use curl if installed"
    Write-Host "  mkdir ..\lib 2>nul"
    Write-Host "  curl -L -o ..\lib\gson-2.10.1.jar https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar"
    Write-Host ""
    Write-Error "Gson library required to compile"
    exit 1
}

# Compile with Gson in classpath
$classpath = "..\lib\gson-2.10.1.jar"
Write-Host "Compiling with classpath: $classpath"

& $javacPath -encoding UTF-8 -cp $classpath -d ..\bin `
    main/*.java ui/*.java shop/*.java core/*.java story/*.java save/*.java online/*.java `
    shared/network/*.java shared/models/*.java `
    server/*.java server/core/*.java server/data/*.java `
    client/*.java client/storage/*.java

if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

Write-Host "Compilation successful!"
