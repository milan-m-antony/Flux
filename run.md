# Flux — Run, Build & Deployment Guide

Quick-reference commands to build, flash, install, and test the entire Flux ecosystem.

---

## 1. Universal Automation (`automate.bat` / `automate.ps1`)

A single master pipeline handles all builds, firmware flashing, device deployment, and tests with zero Android Studio requirement.

```powershell
# From the workspace root:
.\automate.bat
```

> In PowerShell, executable files in the current folder require `.\` at the start (e.g. `.\automate.bat`). `c:\project\Flux` has also been added to your User `PATH` so any newly opened terminal will recognize `automate` from anywhere.

### Automation Modes & Flags
| Command (PowerShell) | Action |
|:---|:---|
| `.\automate.bat` (or `.\automate.ps1`) | **Full Android Pipeline**: Builds APK, installs onto phone via ADB, and starts Flux app. |
| `.\automate.bat -Esp32` | Compiles and flashes ESP32 firmware to `COM10`. |
| `.\automate.bat -Esp32 -Monitor` | Flashes ESP32 firmware and immediately attaches the 115200 baud serial monitor. |
| `.\automate.bat -All` | Flashes ESP32, builds & installs the Android APK, and launches the app. |
| `.\automate.bat -DeviceId <id>` | Installs APK and launches onto a specific connected Android device ID. |
| `.\automate.bat -Test` | Runs Kotlin compilation checks and unit tests (`compileDebugKotlin testDebugUnitTest`). |

---

## 2. Manual Component Commands

### 2.1 Android Companion App (CLI)
From `c:\project\Flux\androidApp`:

```powershell
# Compile check
.\gradlew.bat compileDebugKotlin --no-daemon

# Run unit tests
.\gradlew.bat testDebugUnitTest --no-daemon

# Build Debug APK
.\gradlew.bat assembleDebug --no-daemon

# Install onto physical phone
adb install -r app\build\outputs\apk\debug\app-debug.apk

# Launch Flux MainActivity
adb shell am start -n com.flux.app/com.example.MainActivity
```

### 2.2 ESP32 Firmware (PlatformIO)
From `c:\project\Flux\esp32`:

```powershell
# Compile check
& "$env:USERPROFILE\.platformio\penv\Scripts\pio.exe" run -d .

# Flash over USB (COM10)
& "$env:USERPROFILE\.platformio\penv\Scripts\pio.exe" run --target upload -d . --upload-port COM10

# Serial Monitor (115200 baud)
& "$env:USERPROFILE\.platformio\penv\Scripts\pio.exe" device monitor --baud 115200 -d .
```
