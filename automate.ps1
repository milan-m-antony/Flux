<#
.SYNOPSIS
    Flux Universal Single-Command Automation Script
.DESCRIPTION
    Automates building, flashing, deploying, and testing for the Flux platform:
    - Android APK build & deployment without Android Studio
    - Physical Android device installation & launch via ADB
    - ESP32 firmware compilation and flashing via PlatformIO
    - Compilation verification and unit testing
.EXAMPLE
    .\automate.ps1              # Builds, deploys Android app, and launches it
    .\automate.ps1 -Esp32       # Builds & flashes ESP32 firmware
    .\automate.ps1 -All         # Flashes ESP32 AND builds/deploys Android app
    .\automate.ps1 -Test        # Runs Kotlin compilation checks and unit tests
    .\automate.ps1 -DeviceId "DEVICE_ID"  # Deploys to a specific Android device
#>

[CmdletBinding()]
param (
    [switch]$Android,
    [switch]$Esp32,
    [switch]$All,
    [switch]$Test,
    [switch]$Monitor,
    [string]$Port = "COM10",
    [string]$DeviceId
)

$ErrorActionPreference = "Stop"
$fluxRoot = $PSScriptRoot

# Auto-configure JDK and Android SDK if not already in session
if (-not $env:JAVA_HOME) {
    $javaCandidates = @(
        "C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot",
        "C:\Program Files\Eclipse Adoptium\jdk-17*",
        "C:\Program Files\Java\jdk-17*"
    )
    foreach ($cand in $javaCandidates) {
        $found = Get-Item $cand -ErrorAction SilentlyContinue | Select-Object -First 1
        if ($found) {
            $env:JAVA_HOME = $found.FullName
            $env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
            break
        }
    }
}

if (-not $env:ANDROID_HOME) {
    if (Test-Path "C:\Android\Sdk") {
        $env:ANDROID_HOME = "C:\Android\Sdk"
        $env:PATH = "$env:ANDROID_HOME\platform-tools;$env:PATH"
    }
}

# Default to Android build & run if no specific target is provided
if (-not ($Android -or $Esp32 -or $All -or $Test)) {
    $Android = $true
}

function Write-FluxHeader {
    Write-Host ""
    Write-Host "========================================================" -ForegroundColor DarkCyan
    Write-Host "               FLUX AUTOMATION ENGINE                  " -ForegroundColor Cyan
    Write-Host "   Smart Telemetry, Edge Firmware & Companion App      " -ForegroundColor DarkGray
    Write-Host "========================================================" -ForegroundColor DarkCyan
    Write-Host ""
}

function Write-FluxStep([string]$message) {
    Write-Host "[+] $message" -ForegroundColor Green
}

function Write-FluxWarn([string]$message) {
    Write-Host "[!] $message" -ForegroundColor Yellow
}

function Write-FluxError([string]$message) {
    Write-Host "[x] $message" -ForegroundColor Red
}

function Find-Adb {
    $candidates = @(
        "C:\Android\Sdk\platform-tools\adb.exe",
        "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe",
        (Get-Command adb -ErrorAction SilentlyContinue | Select-Object -ExpandProperty Source -ErrorAction SilentlyContinue)
    )
    foreach ($cand in $candidates) {
        if ($cand -and (Test-Path $cand)) { return $cand }
    }
    return "adb.exe"
}

function Find-Pio {
    $candidates = @(
        "$env:USERPROFILE\.platformio\penv\Scripts\pio.exe",
        (Get-Command pio -ErrorAction SilentlyContinue | Select-Object -ExpandProperty Source -ErrorAction SilentlyContinue)
    )
    foreach ($cand in $candidates) {
        if ($cand -and (Test-Path $cand)) { return $cand }
    }
    return "pio.exe"
}

Write-FluxHeader

# --- TEST MODE ---
if ($Test) {
    Write-FluxStep "Running compilation check & unit tests..."
    Set-Location "$fluxRoot\androidApp"
    try {
        & ".\gradlew.bat" compileDebugKotlin testDebugUnitTest --no-daemon
        if ($LASTEXITCODE -ne 0) {
            Write-FluxError "Tests or compilation failed with exit code $LASTEXITCODE"
            exit $LASTEXITCODE
        }
        Write-Host ""
        Write-FluxStep "All compilation and unit tests PASSED!"
    } finally {
        Set-Location $fluxRoot
    }
    exit 0
}

# --- ESP32 FLASHING ---
if ($Esp32 -or $All) {
    Write-FluxStep "Compiling & Uploading ESP32 Firmware..."
    $pio = Find-Pio
    if (-not (Test-Path $pio) -and -not (Get-Command $pio -ErrorAction SilentlyContinue)) {
        Write-FluxError "PlatformIO executable not found at '$pio'. Ensure PlatformIO is installed."
        exit 1
    }
    Set-Location "$fluxRoot\esp32"
    try {
        & $pio run --target upload -d . --upload-port $Port
        Write-FluxStep "ESP32 firmware successfully uploaded to $Port!"
        if ($Monitor) {
            Write-FluxStep "Starting serial monitor on $Port (115200 baud)..."
            & $pio device monitor --baud 115200 --port $Port -d .
        }
    } finally {
        Set-Location $fluxRoot
    }
    if (-not $All) { exit 0 }
}

# --- ANDROID BUILD, DEPLOY & LAUNCH ---
if ($Android -or $All) {
    $adb = Find-Adb
    Write-FluxStep "Checking connected Android devices via ADB..."
    
    $devicesOutput = & $adb devices
    $deviceLines = @($devicesOutput | Where-Object { $_ -match "\s+device$" })
    
    if ($deviceLines.Count -eq 0) {
        Write-FluxWarn "No connected Android devices detected with USB debugging authorized."
        Write-FluxWarn "Output from adb devices:"
        $devicesOutput | ForEach-Object { Write-Host "   $_" -ForegroundColor DarkGray }
        Write-FluxWarn "Please connect your phone, enable USB Debugging, and re-run."
        exit 1
    }

    $targetDevice = if ($DeviceId) { $DeviceId } else { ($deviceLines[0] -split "\s+")[0].Trim() }
    Write-FluxStep "Target Device: $targetDevice"

    Write-FluxStep "Building Android Debug APK (assembleDebug)..."
    Set-Location "$fluxRoot\androidApp"
    try {
        & ".\gradlew.bat" assembleDebug --no-daemon
        if ($LASTEXITCODE -ne 0) {
            Write-FluxError "Gradle build failed with exit code $LASTEXITCODE"
            exit $LASTEXITCODE
        }
    } finally {
        Set-Location $fluxRoot
    }

    $apkPath = "$fluxRoot\androidApp\app\build\outputs\apk\debug\app-debug.apk"
    if (-not (Test-Path $apkPath)) {
        Write-FluxError "APK file not found at $apkPath"
        exit 1
    }

    Write-FluxStep "Installing APK onto $targetDevice..."
    & $adb -s $targetDevice install -r $apkPath
    if ($LASTEXITCODE -ne 0) {
        Write-FluxError "Failed to install APK onto device."
        exit $LASTEXITCODE
    }

    Write-FluxStep "Launching Flux application on device..."
    & $adb -s $targetDevice shell am start -n com.flux.app/com.example.MainActivity

    Write-Host ""
    Write-FluxStep "SUCCESS! Flux application is deployed and running on $targetDevice."
    Write-Host ""
}
