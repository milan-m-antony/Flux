# Flux — Smart Telemetry System: Initial Project Audit

**Document Version:** 1.0.0  
**Date:** 2026-10-23  
**Status:** Baseline Verified  

---

## 1. Executive Summary

This audit establishes the baseline technical configuration of the original **NaTrack** project before initiating the architectural and functional transformation into **Flux — Smart Telemetry System**. All findings documented herein were verified by direct inspection of source code, configuration files, and baseline build executions.

---

## 2. Repository Structure

The project resides at `c:\projects\Flux\NaTrack` with two primary system tiers:

```
NaTrack/
├── esp32/                        # ESP32 C++ Firmware (PlatformIO)
│   ├── platformio.ini           # Build environment, board target & libraries
│   ├── include/                 # System headers & pin definitions
│   │   ├── NaTrackConfig.hpp
│   │   ├── DisplayManagerConfig.hpp
│   │   └── DeviceState.hpp
│   ├── src/                     # Main entry & application coordinator
│   │   ├── main.cpp
│   │   └── app/
│   │       ├── DeviceApp.hpp
│   │       └── DeviceApp.cpp
│   └── lib/                     # Modular driver libraries
│       ├── BatteryManager/      # Battery voltage & ADC monitoring
│       ├── BluetoothManager/    # BLE GATT server & characteristic streaming
│       ├── ButtonManager/       # 3-button debounced input engine
│       ├── DisplayManager/      # SSD1306 OLED, ScreenManager & Widgets
│       ├── PowerManager/        # Power sequencing
│       ├── SensorManager/       # DS18B20 OneWire temperature driver
│       ├── SettingsManager/     # On-device runtime settings
│       └── Utils/               # System diagnostics & serial reporter
│
└── androidApp/                  # Native Android Application (Kotlin / Jetpack Compose)
    ├── build.gradle.kts         # Root Gradle build script
    ├── settings.gradle.kts      # Project module settings
    ├── gradle/
    │   └── libs.versions.toml   # Dependency version catalog
    └── app/
        ├── build.gradle.kts     # Module dependencies & SDK configs
        ├── src/main/
        │   ├── AndroidManifest.xml
        │   └── java/com/example/
        │       ├── MainActivity.kt
        │       ├── MainViewModel.kt
        │       ├── MyFirebaseMessagingService.kt
        │       ├── ble/         # BLE scanner, connection manager, parser
        │       ├── data/        # Room Database, DataStore, repositories
        │       ├── ui/          # Compose screens, components & Neo-Brutalist theme
        │       └── util/        # Biometrics & utilities
```

---

## 3. Hardware & Firmware Audit

### 3.1 Target Microcontroller
- **Board Target**: `esp32dev` (Espressif ESP32 Dev Module / ESP32-WROOM-32)
- **Clock**: 240 MHz Tensilica Xtensa Dual-Core
- **RAM**: 320 KB SRAM
- **Flash Memory**: 4 MB SPI Flash
- **Framework**: Arduino Core on ESP-IDF (`framework-arduinoespressif32`)
- **Build System**: PlatformIO Core (`pio run -d esp32`)

### 3.2 Sensor Subsystem (Temperature)
- **Sensor Model**: Maxim/Dallas **DS18B20** digital waterproof temperature probe.
- **Protocol**: 1-Wire protocol using `paulstoffregen/OneWire` and `milesburton/DallasTemperature`.
- **Pin Assignment**: **GPIO 4** (`TemperatureSensorPin = 4U` in `NaTrackConfig.hpp`).
- **Read Interval**: Non-blocking `millis()` sampling (1000 ms periodic reading cycle).
- **Diagnostics**: ROM address discovery, disconnect detection, CRC/diagnostic validation.

### 3.3 OLED Display Subsystem
- **Display Controller**: Solomon Systech **SSD1306**
- **Dimensions**: 128 × 64 pixels monochrome OLED (0.96 inch)
- **Communication Protocol**: I2C (Wire library)
- **I2C Pin Assignments**:
  - **SDA**: **GPIO 21** (`DisplayManagerConfig::I2CSdaPin = 21U`)
  - **SCL**: **GPIO 22** (`DisplayManagerConfig::I2CSclPin = 22U`)
- **I2C Addresses**: Primary `0x3C`, fallback `0x3D`
- **Libraries**: `adafruit/Adafruit SSD1306` + `adafruit/Adafruit GFX Library`
- **Refresh & Transitions**: Non-blocking horizontal sliding transitions (128px slide) with dirty-state change detection to eliminate screen tearing and flickering.

### 3.4 Physical Button Input
- **Button Count**: 3 tactile push buttons.
- **Pin Assignments**:
  - **Left Button**: **GPIO 16**
  - **Ok / Select Button**: **GPIO 17**
  - **Right Button**: **GPIO 18**
- **Electrical Configuration**: Internal pull-up (`INPUT_PULLUP`), active LOW (`digitalRead(pin) == LOW`).
- **Input Filtering**:
  - Debounce threshold: **25 ms**
  - Long-press threshold: **800 ms** (used for return-to-home navigation).

### 3.5 Battery & Power Monitoring
- **Battery Target**: 3.7V 18650 Li-ion cell charged via TP4056 IC.
- **Voltage Sensing**: Analog voltage divider connected to ADC pin (`GPIO 34`).
- **Voltage Range**: 3.0V (0%) to 4.2V (100%).

### 3.6 Bluetooth Low Energy (BLE) Protocol
- **Mode**: BLE Peripheral / GATT Server (ESP32 BLE library).
- **Advertising Name**: `"NaTrack"` (to be extended for Flux compatibility).
- **MTU**: 512 bytes (`BLEDevice::setMTU(512)`).
- **Service UUID**: `6E400011-B5A3-F393-E0A9-E50E24DCCA9E`
- **GATT Characteristics**:
  | Characteristic | UUID | Properties | Format / Payload |
  | :--- | :--- | :--- | :--- |
  | Temperature | `6E400002-B5A3-F393-E0A9-E50E24DCCA9E` | READ, NOTIFY | Float (Celsius, 4 bytes) |
  | Battery % | `6E400003-B5A3-F393-E0A9-E50E24DCCA9E` | READ, NOTIFY | Integer (0–100) |
  | Battery Voltage | `6E400004-B5A3-F393-E0A9-E50E24DCCA9E` | READ, NOTIFY | ASCII Float (e.g. `"4.02"`) |
  | Sodium ($Na^+$) | `6E400005-B5A3-F393-E0A9-E50E24DCCA9E` | READ, NOTIFY | String (`"---"` or int) |
  | Status | `6E400006-B5A3-F393-E0A9-E50E24DCCA9E` | READ, NOTIFY | String description |
  | Firmware | `6E400007-B5A3-F393-E0A9-E50E24DCCA9E` | READ, NOTIFY | String (e.g. `"v1.0.0"`) |
  | Device Name | `6E400008-B5A3-F393-E0A9-E50E24DCCA9E` | READ, NOTIFY | String |
  | System JSON | `6E400009-B5A3-F393-E0A9-E50E24DCCA9E` | READ, NOTIFY | JSON Object string |

- **Current JSON Telemetry Schema**:
  ```json
  {
    "temperature": 24.5,
    "battery": 87,
    "batteryVoltage": 4.02,
    "charging": false,
    "sodium": "---",
    "status": "Waiting For Sensor",
    "firmware": "v1.0.0",
    "device": "NaTrack"
  }
  ```

---

## 4. Android Application Audit

### 4.1 Framework & Environment
- **Language**: 100% Kotlin
- **UI Toolkit**: Jetpack Compose with Material Design 3
- **Kotlin Version**: 2.2.10
- **Android Gradle Plugin (AGP)**: 8.9.1
- **Target SDK**: 35 (Android 15)
- **Compile SDK**: 36
- **Min SDK**: 24 (Android 7.0)
- **Application ID**: `com.natrack.app`

### 4.2 Architecture & Components
- **MVVM Architecture**: Reactive UI driven by `MainViewModel` with `StateFlow` and Coroutines.
- **Local Database (Room)**:
  - `AppDatabase` storing `Reading` (sensor history), `CalibrationHistory`, and `NotificationEntity`.
- **Local Preferences (DataStore)**:
  - `SettingsRepository` for user thresholds, theme, units, and Bluetooth MAC bindings.
- **BLE Management**:
  - `BLEScanner`: Continuous BLE advertisement scanning with runtime permissions.
  - `BLEConnectionManager`: GATT connection, service discovery, MTU negotiation, characteristic change listeners.
  - `BLEParser`: JSON deserialization of characteristic `6E400009` with unknown-key tolerance.
- **UI Design System**:
  - Neo-Brutalist tactile theme with custom `neoShadow` modifiers, dark surface palettes, and custom Bezier canvas graphs.

---

## 5. Baseline Build Verification

### 5.1 ESP32 Firmware Build
- **Command**:
  ```powershell
  & "$env:USERPROFILE\.platformio\penv\Scripts\pio.exe" run -d esp32
  ```
- **Exit Code**: `0`
- **Build Status**: **SUCCESS**
- **Duration**: 195.94 seconds
- **Resource Utilization**:
  - RAM: `40,152` bytes / `327,680` bytes (**12.3%**)
  - Flash: `1,177,541` bytes / `1,310,720` bytes (**89.8%**)
- **Artifacts Created**:
  - `.pio/build/esp32dev/firmware.elf`
  - `.pio/build/esp32dev/firmware.bin`
  - `.pio/build/esp32dev/bootloader.bin`
  - `.pio/build/esp32dev/partitions.bin`

### 5.2 Android Application Build
- **Status**: Verification ongoing via Gradle wrapper.

---

## 6. Critical Non-Negotiable Constraints Confirmed

1. **Working Hardware Unaltered**:
   - DS18B20 OneWire pin (GPIO 4) preserved.
   - SSD1306 I2C pins (GPIO 21, 22) and addresses (0x3C, 0x3D) preserved.
   - Button pins (GPIO 16, 17, 18) preserved.
   - BLE Service UUID (`6E400011-...`) and Characteristic UUIDs preserved.
2. **Local Monitoring Independence**:
   - Local sensor sampling and OLED rendering remain strictly non-blocking and fully operational with zero network or cloud dependency.
3. **Firebase Isolation**:
   - Zero additional telemetry writes will be directed to Firebase Realtime Database or Firestore, protecting active quota for the user's other project.
4. **Cloud Migration Target**:
   - **Supabase** will serve as the exclusive telemetry, historical, and device-state store.
   - **OneSignal** will serve as the push notification delivery provider.
