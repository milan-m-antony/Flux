# Flux — Smart Telemetry System

> **High-Precision IoT Temperature Monitoring System with Local OLED Interface, BLE Streaming, Supabase Cloud Telemetry, and OneSignal Push Alerts.**

[![ESP32 Firmware](https://img.shields.io/badge/ESP32-PlatformIO%20Build%20Passed-brightgreen.svg)](docs/build-and-flash.md)
[![Android App](https://img.shields.io/badge/Android-Jetpack%20Compose-blue.svg)](docs/architecture.md)
[![Database](https://img.shields.io/badge/Backend-Supabase%20PostgreSQL-green.svg)](docs/supabase-setup.md)
[![Alerts](https://img.shields.io/badge/Notifications-OneSignal%20Push-orange.svg)](docs/notifications.md)

---

## 1. System Overview

**Flux** is an evolution of the NaTrack IoT project into a high-reliability, edge-to-cloud smart temperature telemetry platform. Engineered for real-world environmental monitoring, liquid probe tracking, and biological fermentation processes, Flux provides:

- **Autonomous Edge Monitoring:** Operates 100% offline if network connectivity is lost. An ESP32 reads a Dallas DS18B20 1-Wire probe and renders real-time metrics, animations, and session statistics on an SSD1306 OLED display with physical 3-button tactile navigation.
- **BLE Telemetry Streaming:** Bluetooth Low Energy GATT server streams single-packet JSON telemetry payloads (512-byte MTU) at 1.0 Hz, maintaining backward compatibility with legacy NaTrack UUIDs while adding Flux session metrics and alarms.
- **Native Android Companion App:** Jetpack Compose mobile dashboard featuring modern dark-mode ergonomics (`#020617` background, lime green and sky blue accents), Celsius/Fahrenheit conversion, profile switching, real-time Bézier curve trend graphs, and a 500-record bounded offline synchronization buffer.
- **Dedicated Supabase Cloud Backend:** PostgreSQL database with Row Level Security (RLS) policies and Logical Replication Realtime streaming for historical charting and latest state synchronization, completely replacing Firebase telemetry writes.
- **Zero-Leak Server-Side Push Alerts:** High/low threshold breaches trigger a PostgreSQL database trigger and a Supabase Edge Function (`flux-alert-evaluator`) which dispatches push notifications via OneSignal's REST API using secure server-side secrets with 15-minute anti-flap hysteresis.

---

## 2. Key Features

| Capability | Implementation Details |
| :--- | :--- |
| **Local OLED Display** | 8 interactive screens (Startup, Dashboard, Live Monitor, Environment, Liquid Probe, Fermentation, Stats & Timer, System Status) on SSD1306 (128×64) at 1.0 Hz. |
| **Physical Button Navigation** | 3 tactile buttons (Left, Select/OK, Right) with 50ms software debouncing and long-press (> 1.2s) return to Dashboard. |
| **Temperature Monitoring Profiles** | Software-calibrated modes for **Live Monitor**, **Environment** (comfort range), **Liquid Probe** (thermal stability), and **Fermentation** (yeast window & elapsed timer). |
| **Session Analytics & Timer** | Monotonic session runtime (`HH:MM:SS`), minimum, maximum, and running average temperature with one-touch reset. |
| **Unit Presentation** | Display in **°C** or **°F** ($F = C \times 9/5 + 32$); raw telemetry and cloud database records remain strictly in Celsius. |
| **BLE Protocol** | Service `6E400011-B5A3-F393-E0A9-E50E24DCCA9E`, Notify Char `6E400009`, Command Char `6E400002` with 512 MTU. |
| **Cloud Telemetry** | Uploads to Supabase at a default 5.0s rate; bounded offline queue buffers up to 500 readings when disconnected and auto-flushes on reconnection. |
| **Push Notifications** | Targeted OneSignal push alerts on upper/lower critical breaches and sensor disconnection. |

---

## 3. Hardware Architecture & Pinout

```
                      +-------------------+
                      |   5V Micro-USB    |
                      +---------+---------+
                                |
                   +------------v------------+
                   |  ESP32-WROOM-32 Dev Kit  |
                   +----+-------+-------+----+
                        |       |       |
       +----------------+       |       +----------------+
       |                        |                        |
+------v------+          +------v------+          +------v------+
|  SSD1306    |          |   DS18B20   |          |  3x Buttons |
|  128x64 OLED|          | Temp Probe  |          | Navigation  |
|  (I2C)      |          |  (1-Wire)   |          |  (Digital)  |
+-------------+          +-------------+          +-------------+
```

| Component | ESP32 GPIO | Mode / Interface | Wiring Note |
| :--- | :---: | :--- | :--- |
| **DS18B20 1-Wire Probe** | **GPIO 4** | Bidirectional 1-Wire | Requires **4.7kΩ pull-up** between Data (DQ) and 3.3V |
| **SSD1306 OLED (SDA)** | **GPIO 21** | I2C Data (`Wire.h`) | Default I2C Address `0x3C` |
| **SSD1306 OLED (SCL)** | **GPIO 22** | I2C Clock (`Wire.h`) | Powered via 3.3V rail |
| **Button 1 (Left / Prev)** | **GPIO 16** | `INPUT_PULLUP` (Active LOW) | Connected to GND when pressed |
| **Button 2 (Select / OK)** | **GPIO 17** | `INPUT_PULLUP` (Active LOW) | Short-press action; hold > 1.2s for Dashboard return |
| **Button 3 (Right / Next)**| **GPIO 18** | `INPUT_PULLUP` (Active LOW) | Connected to GND when pressed |

*Full hardware documentation available in [`docs/hardware.md`](docs/hardware.md).*

---

## 4. Software Repository Structure

```
Flux/
├── esp32/                               # PlatformIO ESP32 Firmware Project
│   ├── platformio.ini                   # Target: esp32dev, Arduino framework
│   ├── include/                         # Configuration and state headers
│   │   ├── DeviceState.hpp              # Profiles, TempUnit, session stats, alarms
│   │   ├── FluxConfig.hpp               # Hardware GPIO assignments & constants
│   │   └── DisplayManagerConfig.hpp     # OLED dimensions & timing
│   ├── lib/
│   │   ├── SensorManager/               # DS18B20 1-Wire non-blocking acquisition
│   │   ├── DisplayManager/              # SSD1306 8-screen carousel & 3-button logic
│   │   └── BluetoothManager/            # BLE GATT server & JSON framing
│   └── src/
│       └── main.cpp                     # Non-blocking cooperative scheduler
│
├── androidApp/                          # Native Android Companion Application
│   ├── app/src/main/java/com/example/
│   │   ├── ble/                         # BLE scanner, connection manager, parser
│   │   ├── data/                        # SupabaseRepository & SettingsRepository
│   │   ├── ui/                          # Jetpack Compose screens
│   │   │   ├── HomeScreen.kt            # Flux hero dashboard & Bézier chart
│   │   │   ├── FluxLiveMonitorScreen.kt # Live gauge and critical alarm view
│   │   │   ├── FluxEnvironmentScreen.kt # Ambient room & comfort monitoring
│   │   │   ├── FluxLiquidScreen.kt      # Submerged liquid probe tracker
│   │   │   ├── FluxFermentationScreen.kt# Yeast fermentation profile & elapsed timer
│   │   │   ├── FluxStatsTimerScreen.kt  # Session analytics & stats reset
│   │   │   ├── FluxAlertSettingsScreen.kt# Safety thresholds & push toggles
│   │   │   └── FluxDiagnosticsScreen.kt# Hardware, BLE, and Supabase diagnostics
│   │   └── MainViewModel.kt             # Central reactive state & cloud coordinator
│   └── gradle.properties                # Configured for JDK 17
│
├── supabase/                            # Cloud Backend & Edge Functions
│   ├── migrations/
│   │   └── 20261023_flux_telemetry_schema.sql # 5-table schema, RLS & triggers
│   └── functions/
│       └── flux-alert-evaluator/index.ts       # Secure OneSignal push dispatch
│
├── docs/                                # Technical Documentation Suite
│   ├── initial-audit.md                 # Baseline repository inspection report
│   ├── architecture.md                  # Detailed system architecture & Mermaid diagrams
│   ├── hardware.md                      # Hardware BOM, wiring diagrams, and limits
│   ├── oled-navigation.md               # OLED screen catalog & 3-button interaction
│   ├── bluetooth-protocol.md            # BLE GATT UUIDs, JSON specs, and commands
│   ├── supabase-setup.md                # Schema, RLS, Realtime, and 10-day quotas
│   ├── notifications.md                 # OneSignal alert logic and anti-flap window
│   ├── build-and-flash.md               # Firmware and Android compilation guide
│   └── testing.md                       # Test matrix and verification results
└── .env.example                         # Environment variable template
```

---

## 5. Quick Start Guide

### 5.1 Building & Flashing Firmware
```bash
cd esp32
pio run -e esp32dev -t upload --upload-port COM3
pio device monitor -b 115200
```

### 5.2 Compiling Android Application
```powershell
cd androidApp
.\gradlew.bat assembleDebug --no-daemon
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### 5.3 Deploying Supabase & Edge Functions
```bash
# 1. Apply database migration
supabase db push

# 2. Deploy OneSignal push dispatch function
supabase functions deploy flux-alert-evaluator

# 3. Configure server-side secrets (never expose in client!)
supabase secrets set ONESIGNAL_APP_ID="your_onesignal_app_id"
supabase secrets set ONESIGNAL_REST_API_KEY="your_onesignal_rest_api_key"
```

---

## 6. Ten-Day Quota & Cost Control

Operating at the default **5-second cloud upload rate** ($17,280\text{ readings/day}$):
- **10-Day Record Count:** 172,800 rows.
- **Estimated Storage:** ~34.5 MB (Under **6.9%** of Supabase's 500 MB Free Tier limit).
- **Realtime Messages:** ~172,800 messages (Under **6.9%** of the 2,500,000 monthly Free Tier allocation).
- **Data Retention:** Telemetry older than 30 days is automatically pruned via scheduled background tasks to maintain database footprint under 100 MB.

---

## 7. Implementation Verification Status

| Component | Status | Details |
| :--- | :---: | :--- |
| **ESP32 Firmware** | **Verified & Built** | Compiles cleanly via PlatformIO (`firmware.bin` generated; RAM 12.3%, Flash 90.1%). |
| **OLED Interface** | **Verified & Built** | 8 screens with 3-button debounced navigation and 1.8s startup animation. |
| **Android UI & Navigation** | **Verified & Built** | Complete Flux Jetpack Compose dark UI with 7 dedicated screens and Bézier trend chart. |
| **BLE Protocol** | **Verified & Built** | 512 MTU JSON streaming; backward compatible with legacy NaTrack UUIDs. |
| **Supabase Cloud Backend** | **Verified by Design** | Migration script and trigger ready; zero writes to Firebase. |
| **OneSignal Push Pipeline** | **Verified by Design** | Server-side Edge Function created with 15-minute anti-flap hysteresis. |
