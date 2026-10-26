# Flux — Smart Telemetry System

> **High-Precision IoT Temperature Monitoring System with Local OLED Interface, BLE Streaming, Supabase Cloud Telemetry, and OneSignal Push Alerts.**

[![ESP32 Firmware](https://img.shields.io/badge/ESP32-PlatformIO%20Build%20Passed-brightgreen.svg)](docs/build-and-flash.md)
[![Android App](https://img.shields.io/badge/Android-Jetpack%20Compose-blue.svg)](docs/architecture.md)
[![Database](https://img.shields.io/badge/Backend-Supabase%20PostgreSQL-green.svg)](docs/supabase-setup.md)
[![Alerts](https://img.shields.io/badge/Notifications-OneSignal%20Push-orange.svg)](docs/notifications.md)

---

## 1. System Overview

**Flux** is a high-reliability, edge-to-cloud smart temperature telemetry platform. Engineered for real-world environmental monitoring, liquid probe tracking, and biological fermentation processes, Flux provides:

- **Autonomous Edge Monitoring:** Operates 100% offline if network connectivity is lost. An ESP32 reads a Dallas DS18B20 1-Wire probe and renders real-time metrics, animations, and session statistics on an SSD1306 OLED display with physical 3-button tactile navigation.
- **BLE Telemetry Streaming:** Bluetooth Low Energy GATT server streams single-packet JSON telemetry payloads (512-byte MTU) at 1.0 Hz with comprehensive Flux session metrics and alarms.
- **Native Android Companion App:** Jetpack Compose mobile dashboard featuring modern dark-mode ergonomics (`#020617` background, lime green and sky blue accents), Celsius/Fahrenheit conversion, profile switching, real-time Bézier curve trend graphs, and a durable Room-backed offline synchronization queue with bounded exponential backoff.
- **Dockerized Node.js/TypeScript Backend on Render:** Production-ready Express API running in a multi-stage Docker container on Render, providing authenticated endpoints (`/health`, `/ready`, `/api/v1/telemetry`, `/api/v1/devices/:deviceId/...`) with cryptographic Firebase ID token verification, device ownership authorization, rate limiting, and idempotency protection.
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
| **Audible Alert Buzzer** | Non-blocking acoustic alarm on **GPIO 25** with active-low module polarity support (`Config::BuzzerActiveLow = true`). Dedicated strictly to genuine active temperature-threshold alarms (repeating 3-pip burst). Complete silence during boot, navigation, and BLE connect/disconnect; instant silencing on normal temperature, sensor disconnect, read fault, or mute. |
| **BLE Protocol** | Service `6E400011-B5A3-F393-E0A9-E50E24DCCA9E`, Notify Char `6E400009`, Command Char `6E400002` with 512 MTU. |
| **Cloud Telemetry** | Durable Room-backed queue (`QueuedTelemetryEntity`) buffers up to 500 readings offline and transmits to the Render backend API with exponential backoff & jitter. |
| **Push Notifications** | Targeted OneSignal push alerts on upper/lower critical breaches and sensor disconnection with anti-flap hysteresis. |

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
                                |
                         +------v------+
                         | Alert Buzzer|
                         |  (GPIO 25)  |
                         +-------------+
```

| Component | ESP32 GPIO | Mode / Interface | Wiring & Polarity Note |
| :--- | :---: | :--- | :--- |
| **DS18B20 1-Wire Probe** | **GPIO 4** | Bidirectional 1-Wire | Requires **4.7kΩ pull-up** between Data (DQ) and 3.3V |
| **SSD1306 OLED (SDA)** | **GPIO 21** | I2C Data (`Wire.h`) | Default I2C Address `0x3C` |
| **SSD1306 OLED (SCL)** | **GPIO 22** | I2C Clock (`Wire.h`) | Powered via 3.3V rail |
| **Audible Alert Buzzer** | **GPIO 25** | Digital Output (Active-Low) | Positive/Signal to GPIO 25, Negative to GND. Driven HIGH to silence, LOW to sound. |
| **Button 1 (Left / Prev)** | **GPIO 16** | `INPUT_PULLUP` (Active LOW) | Connected to GND when pressed |
| **Button 2 (Select / OK)** | **GPIO 17** | `INPUT_PULLUP` (Active LOW) | Short-press action; hold > 1.2s for Dashboard return |
| **Button 3 (Right / Next)**| **GPIO 18** | `INPUT_PULLUP` (Active LOW) | Connected to GND when pressed |

### 3.1 Acoustic Buzzer Subsystem & Hardware Protection

The audible buzzer subsystem on **GPIO 25** is engineered for industrial telemetry environments to guarantee safety without nuisance alarms:

1. **Active-Low Polarity Architecture:**
   - Active buzzer modules (e.g. KY-012, PNP driver circuits) activate when the input signal is pulled **LOW** to Ground and turn off when driven **HIGH** to 3.3V.
   - Traditional microcontrollers initialize GPIOs as floating inputs or drive them LOW, which causes active-low buzzers to sound continuously at boot.
   - Flux configures `Config::BuzzerActiveLow = true` in `FluxConfig.hpp` and explicitly drives GPIO 25 **HIGH (`soundOff()`)** during initialization in `BuzzerManager::begin()`, eliminating startup pops, clicks, or continuous buzzing.
2. **Strict Genuine Alert Gating:**
   - The buzzer sounds **ONLY** when all three conditions are satisfied:
     ```cpp
     const bool alertActive = (state_ != nullptr &&
                               state_->buzzerEnabled &&
                               state_->sensorConnected &&
                               state_->isThresholdAlert);
     ```
3. **Fail-Safe Sensor Disconnect & Read Fault Protection:**
   - If the DS18B20 probe disconnects, reports `-127.0°C`, or generates a bus read error (e.g. 85.0°C uninitialized power-on reset), `state_->sensorConnected` is immediately set to `false`.
   - The buzzer state machine calls `silence()` instantly, preventing stale breach limits from causing false continuous alarms.
   - The OLED display renders `PROBE DISCONNECTED` or sensor fault warning without sounding the buzzer.
4. **Instant Silencing on Normalization:**
   - The exact moment valid temperature returns to the safe normal range (between configured low and high thresholds), the repeating pattern resets and the buzzer is immediately silenced.
5. **Zero Interference Policy:**
   - Non-alert confirmation chirps (`playChirp`), boot chimes, and BLE connection sounds are permanently suppressed, ensuring a quiet, professional user experience.
6. **Cooperative Non-Blocking Alert Pattern:**
   - When a genuine alarm is tripped, `BuzzerManager` executes a non-blocking `millis()` state machine with a 3-pip burst:
     - **Pip 1:** 120 ms sound
     - **Pause:** 80 ms rest
     - **Pip 2:** 120 ms sound
     - **Pause:** 80 ms rest
     - **Pip 3:** 150 ms sound
     - **Inter-Cycle Quiet Period:** 2,000 ms silence before repeating.

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
│   │   ├── BuzzerManager/               # GPIO 25 non-blocking active/passive tone alerts
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
├── backend/                             # Dockerized Node.js + TypeScript Backend (Render)
│   ├── Dockerfile                       # Multi-stage production container
│   ├── render.yaml                      # Render Blueprint deployment spec
│   ├── src/                             # Express app, routes, middleware, services
│   └── tests/                           # Automated Jest test suite (20 tests passed)
│
├── supabase/                            # Cloud Database & Edge Functions
│   ├── migrations/                      # PostgreSQL schema & RLS migrations
│   │   ├── 20261023_flux_telemetry_schema.sql
│   │   └── 20261026_flux_backend_rls_hardening.sql
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

## 5. Universal Quick Start

You can build, test, deploy, and mirror without opening Android Studio.

### 5.1 Universal Single-Command Pipeline
From the repository root (`c:\project\Flux`):

```powershell
.\automate.bat
```
*(or `.\automate.ps1` in PowerShell, or `automate.bat` in CMD)*

This 1-click command automatically:
1. Detects connected physical Android phone via ADB.
2. Compiles and packages the Android debug APK.
3. Installs the APK onto your phone.
4. Launches the Flux application.
5. Opens `scrcpy` live hardware-accelerated desktop mirroring.

### 5.2 Automation Modes & Targets
```powershell
# Flash ESP32 firmware & open serial monitor (COM10)
.\automate.bat -Esp32 -Monitor

# Full system deploy (flash ESP32 + build/install Android app + mirror)
.\automate.bat -All

# Real-time phone screen mirror only
.\automate.bat -Mirror

# Compilation verification & unit testing
.\automate.bat -Test
```

### 5.3 Deploying Supabase & Edge Functions
```bash
# 1. Apply database migration
supabase db push

# 2. Deploy push dispatch function
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
| **Universal 1-Command Automation** | **Verified & Active** | Root `automate.bat` / `automate.ps1` builds, installs to phone, launches app, and mirrors via `scrcpy`. |
| **Desktop Screen Mirroring** | **Verified & Active** | Hardware-accelerated `scrcpy` live mirroring with USB and wireless ADB support. |
| **Automated Wi-Fi Quick Sheet** | **Verified on Hardware** | 1-step bottom sheet with auto phone SSID detection, saved password autofill, and `SET_WIFI` BLE command. |
| **Alert Engine & FCM** | **Verified & Active** | Real FCM push ingestion into Room DB (`NotificationEntity`), anti-flap hysteresis ($0.3^\circ\text{C}$), and 5-min cooldown. |
| **Zero Mock Data** | **Verified & Clean** | Removed all simulated cards and fake dates; alerts screen reads purely from live Room DB. |
| **ESP32 Firmware** | **Verified & Flashed** | PlatformIO firmware flashed on `COM10`; non-blocking 1Hz BLE telemetry, active-low buzzer on GPIO 25, and 8-screen OLED. |
| **Acoustic Alert Buzzer** | **Verified on Hardware** | Active-low polarity configured (`BuzzerActiveLow = true`). Complete silence at boot and during UI operations; sounds strictly on genuine temperature breaches (3-pip burst); silences immediately upon recovery, mute, or probe disconnect. |
| **Render Docker Backend** | **Live & Connected** | Deployed in Singapore (`singapore` cluster) on Render at `https://flux-telemetry-backend-glwn.onrender.com`. Multi-stage Docker container (Node 22) with Supabase database connected and 20/20 Jest unit tests passing. |
| **Durable Offline Queue** | **Verified in Room DB** | Durable SQLite Room queue (`QueuedTelemetryEntity`, bounded to 500 records) with exponential backoff & jitter replacing in-memory queue. |
| **Android UI & Navigation** | **Verified & Built** | Strict frozen header alignment, single-viewport dashboard, dynamic C/F units, and real-time Bézier trend chart. |
| **Supabase & Edge Functions** | **Verified by Design** | PostgreSQL schema, RLS hardening, triggers, and OneSignal dispatch ready with 15-minute anti-flap hysteresis. |


