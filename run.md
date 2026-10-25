# Flux System Build & Run Guide

Run commands from the Flux project root (`c:\projects\Flux`).

## 1. ESP32 Firmware

### Driver Setup (Silicon Labs CP210x)
If Device Manager reports an unrecognized USB device (`CM_PROB_FAILED_INSTALL`):
1. Download Silicon Labs CP210x Universal Windows Driver.
2. Right-click `silabser.inf` and click **Install**, or run:
   ```powershell
   pnputil /add-driver silabser.inf /install
   ```
3. Device will register under Ports as `Silicon Labs CP210x USB to UART Bridge (COM10)`.

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
