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
|                            8-Screen Carousel Cycle                                |
|                                                                                   |
|  [DASHBOARD] <---> [LIVE MONITOR] <---> [ENVIRONMENT] <---> [LIQUID PROBE]        |
|      ^                                                            ^               |
|      |                                                            |               |
|      v                                                            v               |
|  [DIAGNOSTICS] <-> [SETTINGS] <-------> [STATS & TIMER] <-> [FERMENTATION]        |
+-----------------------------------------------------------------------------------+
       ^
       | (Long-Press OK > 1.2s returns to Dashboard from ANY screen)
```

---

## 2. Screen Catalog & Visual Layouts

### Screen 1: Startup Animation (`ScreenId::STARTUP`)
- **Duration:** 1800 ms (non-blocking timer).
- **Layout:**
  - Header: `"FLUX TELEMETRY"` centered (7x10 font).
  - Subtitle: `"v2.0.0 INITIALIZING"` (5x7 font).
  - Animated horizontal progress bar filling from 0% to 100% across 100 pixels.
- **Behavior:** Automatically transitions to `DASHBOARD` upon completion without stalling sensor reading or BLE advertising.

### Screen 2: Main Dashboard (`ScreenId::DASHBOARD`)
- **Purpose:** Primary overview of device operations.
- **Layout:**
  - Top Header: `"FLUX"` logo with BLE connection indicator (`[B]` when connected, `[ ]` when advertising) and Cloud indicator (`[S]`).
  - Hero Area: Large current temperature (e.g., ` 24.5 °C` or ` 76.1 °F`).
  - Middle: Active Profile Badge (`[ENV]`, `[LIQ]`, or `[FERM]`).
  - Bottom Footer: Session extremes (`Lo: 21.2°  Hi: 26.8°`).
- **OK Button Action:** Cycles temperature unit between **°C** and **°F**.

### Screen 3: Live Monitor (`ScreenId::LIVE_MONITOR`)
- **Purpose:** Real-time sensor verification and alert limits.
- **Layout:**
  - Large digital readout in center.
  - Active upper and lower alert limits (`Limits: 18.0 - 28.0`).
  - Alarm banner: `"*** CRITICAL ALERT ***"` flashes if reading exceeds bounds.
- **OK Button Action:** Toggles audible/OLED alert mute state.

### Screen 4: Environment Profile (`ScreenId::ENV_PROFILE`)
- **Purpose:** Ambient room, laboratory, or server rack temperature tracking.
- **Layout:**
  - Profile Header: `"PROFILE: ENVIRONMENT"`.
  - Current Ambient Temp.
  - Recommended Comfort Band: `18.0°C - 26.0°C`.
  - Qualitative Comfort Indicator: `[ OPTIMAL ]` / `[ TOO COLD ]` / `[ TOO HOT ]`.
- **OK Button Action:** Sets device profile to `ENV`.

### Screen 5: Liquid Probe (`ScreenId::LIQUID_PROFILE`)
- **Purpose:** Submerged probe measurement for water, liquids, and thermal baths.
- **Layout:**
  - Profile Header: `"PROFILE: LIQUID"`.
  - Current Liquid Temp.
  - Thermal Stability Tracker: Displays stability variance (`+/-0.1°C / min`).
  - Sensor connection safeguard: Explicit `PROBE DISCONNECTED` alert if resistance exceeds bounds.
- **OK Button Action:** Sets device profile to `LIQUID`.

### Screen 6: Fermentation Profile (`ScreenId::FERMENT_PROFILE`)
- **Purpose:** Precision temperature regulation for fermentation and brewing experiments.
- **Layout:**
  - Profile Header: `"PROFILE: FERMENTATION"`.
  - Current Must/Wort Temp.
  - Active Fermentation Window: `18.0°C - 22.0°C` (Yeast target).
  - Elapsed Fermentation Duration: `T+ 14h 22m`.
- **OK Button Action:** Sets device profile to `FERMENTATION`.

### Screen 7: Statistics & Timer (`ScreenId::STATS_TIMER`)
- **Purpose:** Continuous session analysis without cloud dependency.
- **Layout:**
  - Monotonic Elapsed Timer: `T+ HH:MM:SS`.
  - `Min Temp:` Lowest valid reading recorded this session.
  - `Max Temp:` Highest valid reading recorded this session.
  - `Avg Temp:` Running cumulative average of all valid samples.
  - `Samples:` Total valid readings acquired.
- **OK Button Action:** **Reset Session Statistics** (Clears min, max, avg, and resets timer to 0).

### Screen 8: System Status & Diagnostics (`ScreenId::SYSTEM_STATUS`)
- **Purpose:** Device operational health inspection.
- **Layout:**
  - `BLE:` `CONNECTED` / `ADVERTISING`.
  - `Probe:` `DS18B20 ONLINE (12-bit)`.
  - `Cloud:` `SYNC ACTIVE` (or `OFFLINE BUFFERED`).
  - `Heap:` Free SRAM memory in kilobytes (e.g. `Heap: 184 KB`).
  - `Uptime:` Elapsed device uptime in seconds / hours.
- **OK Button Action:** Forces diagnostic sensor bus re-scan.

---

## 3. Physical Button Controls (3-Button Layout)

The physical interface uses 3 tactile push buttons on the front panel:

| Physical Button | ESP32 GPIO | Short-Press Action | Long-Press Action (> 1.2s) |
| :--- | :---: | :--- | :--- |
| **Button Left** | **GPIO 16** | Navigate to Previous Screen | Jump backward 3 screens |
| **Button OK / Select** | **GPIO 17** | Screen Action (Toggle °C/°F, Activate Profile, Reset Stats) | **Instant return to Main Dashboard** |
| **Button Right** | **GPIO 18** | Navigate to Next Screen | Jump forward 3 screens |

### Debouncing & Input Guard
- Buttons use internal pullups (`INPUT_PULLUP`) and trigger on falling edges (`LOW`).
- `ButtonDebouncer` enforces a **50ms stable window** to prevent bounce contacts.
- Repeated accidental clicks are eliminated using edge-trigger state transitions.
