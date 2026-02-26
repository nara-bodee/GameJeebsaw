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

& $javacPath -encoding UTF-8 -d ..\bin main/*.java ui/*.java shop/*.java core/*.java story/*.java save/*.java
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}
