# Flux — Build, Flash, and Deployment Guide

## 0. Universal 1-Command Automation (Recommended)

Flux includes a universal automation engine that enables you to build, test, deploy to hardware, and mirror your physical Android phone screen without opening Android Studio.

Run from repository root (`c:\project\Flux`):

```cmd
automate.bat
```
or in PowerShell:
```powershell
.\automate.ps1
```

### Automation Modes
| Command | Functionality |
|:---|:---|
| `automate.bat` | Builds Android APK, installs onto connected phone via ADB, starts `MainActivity`, and launches `scrcpy` live mirroring. |
| `automate.bat -Esp32` | Builds and flashes PlatformIO firmware to the ESP32 on `COM10`. |
| `automate.bat -All` | Flashes ESP32 AND builds/deploys Android companion app with live desktop mirroring in one step. |
| `automate.bat -Mirror` | Launches `scrcpy` screen mirroring immediately. |
| `automate.bat -Test` | Runs Kotlin compilation check and unit tests (`compileDebugKotlin testDebugUnitTest`). |

---

## 1. Real-Time Screen Mirroring (`scrcpy`)

View and control your connected Android phone on your Windows laptop with zero latency.

- **Desktop Shortcut:** Double-click **`Flux Screen Mirror`** on your Desktop.
- **Terminal:** Run `scrcpy` from any terminal or command prompt.
- **Wireless Mirroring (Wire-Free):**
  1. With phone connected via USB:
     ```powershell
     adb tcpip 5555
     ```
  2. Unplug the USB cable and mirror over local Wi-Fi:
     ```powershell
     scrcpy --tcpip=<phone-ip-address>:5555
     ```

### Key Shortcuts in Mirror Window
| Action | Shortcut / Click |
|:---|:---|
| **Tap / Select** | Left Click |
| **Back Button** | Right Click |
| **Home Button** | Middle Click / `Ctrl` + `H` |
| **Turn Phone Display Off (Save Battery)** | `Ctrl` + `O` |
| **Paste from PC to Phone** | `Ctrl` + `Shift` + `V` |
| **Toggle Fullscreen** | `Ctrl` + `F` |

---

## 2. Firmware Build & Flash (ESP32)

The firmware is developed with PlatformIO on the Arduino framework targeting the `esp32dev` environment.

### 2.1 Prerequisites
- [PlatformIO Core (CLI)](https://platformio.org/install/cli) or PlatformIO IDE extension for VS Code / Cursor.
- **USB-to-UART Bridge Drivers:**
  - For boards with Silicon Labs CP2102/CP2104: Download the official **CP210x Universal Windows Driver**.
  - Right-click `silabser.inf` and select **Install**, or run as Administrator:
    ```powershell
    pnputil /add-driver silabser.inf /install
    ```
  - Verify in Windows Device Manager: Ports (COM & LPT) should list `Silicon Labs CP210x USB to UART Bridge (COMx)` with Status OK (e.g. `COM10`).
- High-quality Micro-USB data cable (ensure it carries data lines, not charging only).

### 2.2 Flashing to Hardware
From `c:\project\Flux\esp32`:

```powershell
# 1. Identify active serial port
& "$env:USERPROFILE\.platformio\penv\Scripts\pio.exe" device list

# 2. Upload binary to target ESP32 board (COM10)
& "$env:USERPROFILE\.platformio\penv\Scripts\pio.exe" run --target upload -d . --upload-port COM10

# 3. Launch serial monitor at 115200 baud
& "$env:USERPROFILE\.platformio\penv\Scripts\pio.exe" device monitor --baud 115200 -d .
```

> [!TIP]
> **Bootloader Sync Tip:** If `esptool.py` displays `Connecting........_____.....`, press and hold the physical **BOOT / FLASH** button on your ESP32 development board for 2 seconds until the flashing percentage begins.

---

## 3. Android Companion Application Build (CLI Without Android Studio)

The mobile application is built using native **Kotlin & Jetpack Compose** with Android Gradle Plugin (AGP 8.9.1).

### 3.1 Prerequisites
- **Java Development Kit (JDK):** JDK 17 (installed at `C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot`).
- **Android SDK:** Configured at `C:\Android\Sdk` with `platform-tools` (ADB).

### 3.2 Compilation & Packaging
From `c:\project\Flux\androidApp`:

```powershell
# 1. Compile debug Kotlin sources
.\gradlew.bat compileDebugKotlin --no-daemon

# 2. Run unit tests
.\gradlew.bat testDebugUnitTest --no-daemon

# 3. Assemble debug APK
.\gradlew.bat assembleDebug --no-daemon

# 4. Install onto connected Android device via ADB
adb install -r app\build\outputs\apk\debug\app-debug.apk

# 5. Launch the app on phone
adb shell am start -n com.flux.app/com.example.MainActivity
```

---

## 4. Supabase Backend Deployment

### 4.1 Applying Database Migration
From the project root:
```bash
# Apply schema migration to remote Supabase project
supabase db push
```
Alternatively, copy the SQL migration script from `supabase/migrations/20261023_flux_telemetry_schema.sql` and execute it in the **Supabase Dashboard SQL Editor**.

### 4.2 Deploying Alert Edge Function
```bash
# Deploy function to Supabase
supabase functions deploy flux-alert-evaluator

# Configure server-side secrets
supabase secrets set ONESIGNAL_APP_ID="your-onesignal-app-id"
supabase secrets set ONESIGNAL_REST_API_KEY="your-onesignal-rest-api-key"
```
