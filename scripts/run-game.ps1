$ErrorActionPreference = 'Stop'

$workspaceRoot = Split-Path -Path $PSScriptRoot -Parent
Set-Location (Join-Path $workspaceRoot 'src')

$javaPath = $null
$javaCommand = Get-Command java -ErrorAction SilentlyContinue
if ($javaCommand -and ($javaCommand.Source -notlike '*java8path*')) {
    $javaPath = $javaCommand.Source
}

if (-not $javaPath -and $env:JAVA_HOME) {
    $candidate = Join-Path $env:JAVA_HOME 'bin\java.exe'
    if (Test-Path $candidate) {
        $javaPath = $candidate
    }
}

if (-not $javaPath) {
    $adoptium = Get-ChildItem 'C:\Program Files\Eclipse Adoptium' -Directory -ErrorAction SilentlyContinue |
        Sort-Object Name -Descending |
        Select-Object -First 1
    if ($adoptium) {
        $candidate = Join-Path $adoptium.FullName 'bin\java.exe'
        if (Test-Path $candidate) {
            $javaPath = $candidate
        }
    }
}

if (-not $javaPath) {
    Write-Error 'Java runtime not found.'
    exit 1
}

# Set classpath with Gson library
$classpath = "$workspaceRoot\bin;$workspaceRoot\lib\gson-2.10.1.jar"

& $javaPath -cp $classpath main.GameWindow
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}
