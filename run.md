# Flux System Build & Run Guide

Run commands from the Flux project root (`c:\projects\Flux`).

## 1. ESP32 Firmware

### Build (Compile Check)
```powershell
& "$env:USERPROFILE\.platformio\penv\Scripts\pio.exe" run -d esp32
```

### Upload to ESP32 (Flash)
```powershell
& "$env:USERPROFILE\.platformio\penv\Scripts\pio.exe" run --target upload -d esp32
```

### Serial Monitor (115200 baud)
```powershell
& "$env:USERPROFILE\.platformio\penv\Scripts\pio.exe" device monitor --baud 115200 -d esp32
```

---

## 2. Android Application

From `c:\projects\Flux\androidApp`:

### Compile Debug Sources
```powershell
cd androidApp
.\gradlew.bat compileDebugSources --no-daemon
```

### Build Debug APK
```powershell
cd androidApp
.\gradlew.bat assembleDebug --no-daemon
```

### Install onto Device via ADB
```powershell
adb install -r androidApp\app\build\outputs\apk\debug\app-debug.apk
```