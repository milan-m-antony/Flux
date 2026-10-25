# Flux — OLED Interface & Button Navigation Guide

## 1. Local Interface Architecture

The **Flux** firmware features an offline-first, local OLED user interface designed for a 0.96-inch monochrome SSD1306 display (128×64 resolution). All rendering operations are non-blocking, operating on an internal state machine driven by `DisplayManager`, `ScreenManager`, and `NavigationManager`.

```
                        +----------------------+
                        |   Startup Animation  | (1.8s boot progress)
                        +----------+-----------+
                                   |
                                   v
+-----------------------------------------------------------------------------------+
|                            Screen Hierarchy & Sub-menus                           |
|                                                                                   |
|  [MAIN MONITOR] <=====================> [MODE SELECTION]                          |
|         ^                                      | (Live, Env, Liq, Ferm)           |
|         |                                      v                                  |
|         +-----------------------------> [SETTINGS MENU]                           |
|                                                |                                  |
|                 +----------------+-------------+------------+                     |
|                 |                |                          |                     |
|                 v                v                          v                     |
|          [WI-FI STATUS]   [BLUETOOTH STATUS]          [PREFERENCES]               |
|                                                             | (Unit, Limits,      |
|                                                             |  Buzzer ON/OFF)     |
|                                                             v                     |
|                                                       [DEVICE INFO]               |
+-----------------------------------------------------------------------------------+
       ^
       | (Long-Press OK > 1.2s returns to Main Monitor from ANY screen or menu)
```

---

## 2. Screen Catalog & Visual Layouts

### Screen 1: Startup Animation (`ScreenId::MainMonitor` during boot)
- **Duration:** 1800 ms (non-blocking timer).
- **Layout:**
  - Header: `"FLUX TELEMETRY"` centered (7x10 font).
  - Subtitle: `"v2.0.0 INITIALIZING"` (5x7 font).
  - Animated horizontal progress bar filling from 0% to 100% across 100 pixels.
- **Behavior:** Automatically transitions to `MainMonitor` upon completion without stalling sensor reading, buzzer state machine, or BLE advertising. Any button press during boot skips the animation immediately.

### Screen 2: Main Monitor / Dashboard (`ScreenId::MainMonitor`)
- **Purpose:** Primary real-time telemetry overview.
- **Layout:**
  - **Top Header:** `"FLUX"` logo with BLE connection indicator (`[B]` when connected, `[ ]` when advertising) and Wi-Fi indicator (`[W]`).
  - **Hero Area:** Large current temperature reading (e.g., ` 24.5 °C` or ` 76.1 °F`).
  - **Active Profile Badge:** `[LIVE]`, `[ENV]`, `[LIQ]`, or `[FERM]`.
  - **Bottom Footer:** Session extremes (`Lo: 21.2°  Hi: 26.8°`).
- **Button Controls:**
  - **Left Button:** Transitions to `ModeSelection` screen.
  - **Right Button:** Transitions to `Settings` menu.
  - **OK Button:** Quick toggle between **°C** and **°F**.

### Screen 3: Mode Selection (`ScreenId::ModeSelection`)
- **Purpose:** Switch active software monitoring profile.
- **Profiles Available:**
  1. `LIVE` — Raw sensor monitor with maximum dynamic range.
  2. `ENV` — Ambient environmental comfort monitoring (18°C–26°C band).
  3. `LIQ` — Liquid submersion probe with thermal stability tracking.
  4. `FERM` — Yeast fermentation window (18°C–22°C) with elapsed session timer.
- **Button Controls:**
  - **Left Button:** Move selection up (or return to `MainMonitor` if at top).
  - **Right Button:** Move selection down (cycles 0–3).
  - **OK Button:** Select highlighted profile and return to `MainMonitor`.

### Screen 4: Settings Menu (`ScreenId::Settings`)
- **Purpose:** Gateway to system configuration sub-menus.
- **Menu Items:**
  1. `Wi-Fi Settings` — Opens `WifiStatus` screen.
  2. `Bluetooth` — Opens `BluetoothStatus` screen.
  3. `Preferences` — Opens `Preferences` screen.
  4. `Device Info` — Opens `DeviceInfo` screen.
- **Button Controls:**
  - **Left Button:** Move up (or return to `MainMonitor` if at top).
  - **Right Button:** Move down (cycles 0–3).
  - **OK Button:** Enter highlighted sub-screen.

### Screen 5: Preferences (`ScreenId::Preferences`)
- **Purpose:** Operational units, threshold safety bounds, and audible alarm settings.
- **Configurable Items:**
  1. `Unit: °C / °F` — Toggles preferred temperature unit.
  2. `High Limit: 26°C -> 35°C` — Cycles high temperature alarm trip point (26, 28, 30, 32, 35°C).
  3. `Low Limit: 12°C -> 20°C` — Cycles low temperature alarm trip point (12, 15, 18, 20°C).
  4. `Buzzer: ON / OFF` — Toggles physical alert buzzer on **GPIO 25**. Emits an acoustic confirmation pip (3000 Hz) when enabled; immediately mutes any active alarm when set to OFF.
- **Button Controls:**
  - **Left Button:** Move up (or return to `Settings` menu if at top).
  - **Right Button:** Move down (cycles 0–3).
  - **OK Button:** Toggle/cycle value of highlighted preference.

### Screen 6: Device Info (`ScreenId::DeviceInfo`)
- **Purpose:** Hardware and firmware diagnostics.
- **Information Rendered:**
  - Firmware Version: `v2.0.0`
  - Chip Model: `ESP32-D0WD-V3`
  - Free SRAM Heap: (e.g., `184 KB`)
  - Flash Memory: `4 MB`
  - Monotonic System Uptime: `HH:MM:SS`
- **OK / Left Button Action:** Returns to `Settings` menu.

### Screen 7: Wi-Fi Status (`ScreenId::WifiStatus`)
- **Purpose:** Local network connectivity verification.
- **Information Rendered:**
  - Connection State: `CONNECTED` or `DISCONNECTED`
  - Configured SSID: e.g., `kERALA VISION`
  - Assigned IP Address
  - Signal Strength: RSSI dBm
- **OK / Left Button Action:** Returns to `Settings` menu.

### Screen 8: Bluetooth Status (`ScreenId::BluetoothStatus`)
- **Purpose:** BLE link status and GATT parameters.
- **Information Rendered:**
  - Local BLE Device Name: `Flux1`
  - Status: `ADVERTISING` or `CONNECTED`
  - Negotiated MTU: `512 bytes`
  - Active Client Info
- **OK / Left Button Action:** Returns to `Settings` menu.

---

## 3. Physical Button Controls (3-Button Layout)

The physical interface uses 3 tactile push buttons on the front panel:

| Physical Button | ESP32 GPIO | Short-Press Action | Long-Press Action (> 1.2s) |
| :--- | :---: | :--- | :--- |
| **Button Left** | **GPIO 16** | Navigate previous / up / back | Jump to top / previous menu |
| **Button OK / Select** | **GPIO 17** | Toggle unit, select item, cycle option | **Instant return to Main Monitor from ANY screen** |
| **Button Right** | **GPIO 18** | Navigate next / down / forward | Cycle to bottom / next item |

### Debouncing & Input Guard
- Buttons use internal pullups (`INPUT_PULLUP`) and trigger on falling edges (`LOW`).
- `ButtonDebouncer` enforces a **50ms stable window** to prevent bounce contacts.
- Repeated accidental clicks are eliminated using edge-trigger state transitions.
