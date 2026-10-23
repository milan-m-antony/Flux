# Flux — Build, Flash, and Deployment Guide

## 1. Firmware Build & Flash (ESP32)

The firmware is developed with PlatformIO on the Arduino framework targeting the `esp32dev` environment.

### 1.1 Prerequisites
- [PlatformIO Core (CLI)](https://platformio.org/install/cli) or PlatformIO IDE extension for VS Code / Cursor.
- USB-to-UART Bridge Drivers (Silicon Labs CP210x or WCH CH340/CH341 depending on dev board).
- High-quality Micro-USB data cable (ensure it carries data lines, not charging only).

### 1.2 Build Commands

From the firmware directory (`c:\projects\Flux\NaTrack\esp32`):

```bash
# 1. Inspect project environment and libraries
pio project config

# 2. Compile firmware binary
pio run -e esp32dev

# 3. List connected serial devices and COM ports
pio device list
```

### 1.3 Flashing to Hardware

```bash
# 4. Upload binary to target ESP32 board
pio run -e esp32dev -t upload --upload-port COM3

# 5. Launch serial monitor at 115200 baud
pio device monitor -b 115200
```

> [!TIP]
> **Bootloader Sync Tip:** If `esptool.py` displays `Connecting........_____.....`, press and hold the physical **BOOT / FLASH** button on your ESP32 development board for 2 seconds until the flashing percentage begins.

---

## 2. Android Companion Application Build

The mobile application is built using native **Kotlin & Jetpack Compose** with Android Gradle Plugin (AGP 8.9.1).

### 2.1 Prerequisites
- **Java Development Kit (JDK):** JDK 17 is required. Set `org.gradle.java.home=C:/Users/milan/.jdks/jbr-17.0.14` in `androidApp/gradle.properties`.
- **Android SDK:** Configured in `androidApp/local.properties`:
  ```properties
  sdk.dir=C\:\\Users\\milan\\AppData\\Local\\Android\\Sdk
  ```

### 2.2 Compilation & Packaging

From the Android project directory (`c:\projects\Flux\NaTrack\androidApp`):

```powershell
# 1. Compile all debug Kotlin and Java sources
.\gradlew.bat compileDebugSources --no-daemon

# 2. Assemble debug APK
.\gradlew.bat assembleDebug --no-daemon

# 3. Output APK location
# androidApp/app/build/outputs/apk/debug/app-debug.apk

# 4. Install onto connected Android device via ADB
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 3. Supabase Backend Deployment

### 3.1 Applying Database Migration
From the project root:
```bash
# Apply schema migration to remote Supabase project
supabase db push
```
Alternatively, copy the SQL migration script from `supabase/migrations/20261023_flux_telemetry_schema.sql` and execute it in the **Supabase Dashboard SQL Editor**.

### 3.2 Deploying Alert Edge Function
```bash
# Deploy function to Supabase
supabase functions deploy flux-alert-evaluator

# Configure server-side secrets
supabase secrets set ONESIGNAL_APP_ID="your-onesignal-app-id"
supabase secrets set ONESIGNAL_REST_API_KEY="your-onesignal-rest-api-key"
```
