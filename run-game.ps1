param(
    [switch]$CompileOnly
)

$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot

function Fail-And-Exit {
    param(
        [string]$Message,
        [int]$Code = 1
    )

    Write-Host "[ERROR] $Message" -ForegroundColor Red
    exit $Code
}

function Resolve-MavenCommand {
    $fromPath = Get-Command mvn -ErrorAction SilentlyContinue
    if ($fromPath) {
        return $fromPath.Source
    }

    $candidates = @(
        (Join-Path $env:USERPROFILE "scoop\shims\mvn.cmd"),
        (Join-Path $env:USERPROFILE "scoop\apps\maven\current\bin\mvn.cmd")
    )

    foreach ($candidate in $candidates) {
        if (Test-Path $candidate) {
            return $candidate
        }
    }

    return $null
}

$mavenCmd = Resolve-MavenCommand
if (-not $mavenCmd) {
    Fail-And-Exit "Maven is not available. Install Maven first, then retry."
}

Write-Host "[1/2] Compiling project with Maven..." -ForegroundColor Cyan
& $mavenCmd -q -DskipTests clean compile
if ($LASTEXITCODE -ne 0) {
    Fail-And-Exit "Maven compile failed." $LASTEXITCODE
}

if ($CompileOnly) {
    Write-Host "Compile succeeded." -ForegroundColor Green
    exit 0
}

Write-Host "[2/2] Launching game..." -ForegroundColor Cyan
& $mavenCmd exec:java
exit $LASTEXITCODE
