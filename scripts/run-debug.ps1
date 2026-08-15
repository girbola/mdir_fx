[CmdletBinding()]
param(
    [switch]$SkipTests
)

$ErrorActionPreference = 'Stop'

function Write-Step([string]$Message) {
    Write-Host "[debug] $Message"
}

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot '..')
$targetDir = Join-Path $repoRoot 'target'
$classesDir = Join-Path $targetDir 'classes'
$moduleDepsDir = Join-Path $targetDir 'module-deps'
$buildArgs = @('-Pdebug-modulepath', 'package')
if ($SkipTests) { $buildArgs += '-DskipTests' }

foreach ($cmd in @('mvn', 'java')) {
    if (-not (Get-Command $cmd -ErrorAction SilentlyContinue)) {
        throw "Required command not found: $cmd"
    }
}

Write-Step "Building project with Maven (debug-modulepath profile)"
& mvn @buildArgs
if ($LASTEXITCODE -ne 0) {
    throw "Maven build failed with exit code $LASTEXITCODE"
}

if (-not (Test-Path $classesDir)) {
    throw "Compiled classes directory not found: $classesDir"
}
if (-not (Test-Path $moduleDepsDir)) {
    throw "Module dependency directory not found: $moduleDepsDir"
}

$modulePath = "$classesDir;$moduleDepsDir"
Write-Step "Launching module com.girbola/com.girbola.Launcher"
& java '--enable-native-access=javafx.graphics' '--enable-native-access=ALL-UNNAMED' '--module-path' $modulePath '-m' 'com.girbola/com.girbola.Launcher'
exit $LASTEXITCODE

