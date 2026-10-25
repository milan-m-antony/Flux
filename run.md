# Flux Universal Build, Run & Automation Guide

Run all commands from the Flux repository root (`c:\project\Flux`).

---

## 1. Universal Single-Command Automation (Recommended)

You do **NOT** need Android Studio or complex manual steps. Use the root automation engine to build, install, launch, and mirror your physical Android phone in **one single command**:

### 1-Click Launch on Connected Device

**In PowerShell (recommended):**
```powershell
.\automate.bat
```
*(or `.\automate.ps1`)*

**In Command Prompt (cmd.exe):**
```cmd
automate.bat
```

> [!NOTE]
> In PowerShell, executable files in the current folder require `.\` at the start (e.g. `.\automate.bat`). `c:\project\Flux` has also been added to your User `PATH` so any newly opened terminal will recognize `automate` from anywhere.

### Automation Modes & Flags
| Command (PowerShell) | Action |
|:---|:---|
| `.\automate.bat` (or `.\automate.ps1`) | **Full Pipeline**: Builds APK, installs onto phone via ADB, starts Flux app, and launches `scrcpy` screen mirror on your desktop. |
| `.\automate.bat -Esp32` | Compiles and flashes ESP32 firmware to `COM10`. |
| `.\automate.bat -Esp32 -Monitor` | Flashes ESP32 firmware and immediately attaches the 115200 baud serial monitor. |
| `.\automate.bat -All` | Flashes ESP32, builds & installs the Android APK, launches the app, and opens `scrcpy`. |
| `.\automate.bat -Mirror` | Instantly opens the `scrcpy` screen mirror for your connected phone. |
| `.\automate.bat -Test` | Runs Kotlin compilation checks and unit tests (`compileDebugKotlin testDebugUnitTest`). |
| `.\automate.bat -NoMirror` | Builds, installs, and launches the app on phone without starting the desktop mirror window. |

---

## 2. Real-Time Screen Mirroring (`scrcpy`)

View and control your connected Android device on your laptop with zero latency.

- **Desktop Shortcut:** Double-click **`Flux Screen Mirror`** on your Desktop.
- **Terminal:** Simply run `scrcpy` in any terminal or command prompt.
- **Wireless Mirroring (Wire-Free):**
  1. While phone is plugged into USB, enable TCP/IP mode:
     ```powershell
     adb tcpip 5555
     ```
  2. Unplug the USB cable and connect over your local Wi-Fi:
     ```powershell
     scrcpy --tcpip=<phone-ip-address>:5555
     ```

### Shortcuts in Mirror Window
- **Left-Click:** Tap / Touch
- **Right-Click:** Back button (turns screen on if asleep)
- **Middle-Click:** Home screen
- **Ctrl + O:** Turn off phone screen to save battery while continuing to view & control on laptop
- **Ctrl + Shift + V:** Paste PC clipboard to phone
- **Ctrl + F:** Fullscreen toggle

---

## 3. Manual Component Commands

### 3.1 Android Companion App (CLI)
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

### 3.2 ESP32 Firmware (PlatformIO)
From `c:\project\Flux\esp32`:

```powershell
# Compile check
& "$env:USERPROFILE\.platformio\penv\Scripts\pio.exe" run -d .

# Flash over USB (COM10)
& "$env:USERPROFILE\.platformio\penv\Scripts\pio.exe" run --target upload -d . --upload-port COM10

# Serial Monitor (115200 baud)
& "$env:USERPROFILE\.platformio\penv\Scripts\pio.exe" device monitor --baud 115200 -d .
```
