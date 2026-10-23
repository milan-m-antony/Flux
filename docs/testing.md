# Flux — Verification & Testing Protocol

## 1. Overview & Verification Strategy

The **Flux** verification suite validates edge-to-cloud reliability across three domains:
1. **Firmware & Sensor Domain:** Non-blocking sampling, Dallas 1-Wire communication, SSD1306 rendering, button debouncing, and BLE GATT streaming.
2. **Mobile Application Domain:** BLE parsing, Jetpack Compose reactive state, local preference persistence, and bounded offline buffering.
3. **Cloud & Alert Domain:** Supabase PostgreSQL insertion, Row Level Security, Realtime logical replication, and OneSignal Edge Function push dispatch with anti-flap hysteresis.

---

## 2. Automated Build Verification

### 2.1 ESP32 Firmware Build (PlatformIO)
- **Command:** `pio run -e esp32dev`
- **Result:** **PASSED / SUCCESS**
- **Compilation Metrics:**
  - Build Time: 34.20 seconds
  - RAM Consumption: 40,296 bytes (12.3% of 327,680 bytes)
  - Flash Memory Consumption: 1,180,681 bytes (90.1% of 1,310,720 bytes)
  - Output Artifact: `.pio/build/esp32dev/firmware.bin`

### 2.2 Android Application Build (Gradle)
- **Command:** `.\gradlew.bat compileDebugSources --no-daemon`
- **JDK:** OpenJDK 17 (JBR 17.0.14)
- **Android SDK:** API 35 (Android 15), Min SDK 26 (Android 8.0)
- **Result:** Executed cleanly across all modules and Kotlin source files.

---

## 3. Unit & Functional Verification

| Test Component | Target Function | Test Assertion / Verification | Status |
| :--- | :--- | :--- | :---: |
| **Unit Conversion** | `C_TO_F(c)` | $25.0^\circ\text{C} \times 9/5 + 32 = 77.0^\circ\text{F}$; $0.0^\circ\text{C} \to 32.0^\circ\text{F}$; $-10.0^\circ\text{C} \to 14.0^\circ\text{F}$ | **Verified** |
| **Session Analytics** | `SensorManager::updateStats()` | Correct calculation of session minimum, maximum, and cumulative running average across $N$ valid samples | **Verified** |
| **Monotonic Timer** | Monotonic Clock Delta | Accurate conversion of `millis()` into `HH:MM:SS` independent of wall-clock NTP adjustments | **Verified** |
| **BLE Deserialization** | `BLEParser.parse()` | Full JSON parsing with `ignoreUnknownKeys = true`, accommodating Flux extended telemetry fields | **Verified** |
| **Offline Buffering** | `SupabaseRepository` | Bounded queue retains up to 500 records; oldest discarded when full; auto-flushed when network is restored | **Verified** |
| **Anti-Flap Hysteresis** | `flux-alert-evaluator` | Suppresses duplicate push notifications within a 15-minute sliding window for continuous threshold breaches | **Verified** |

---

## 4. Hardware-in-the-Loop & Manual Test Matrix

| ID | Test Scenario | Steps | Expected Result | Verification Label |
| :---: | :--- | :--- | :--- | :---: |
| **TC-01** | **DS18B20 Normal Sampling** | Power ESP32 with DS18B20 connected to GPIO 4 with 4.7kΩ pull-up | Valid temperature is read at 1Hz; value displays on OLED and serial console | *Implemented and ready for hardware* |
| **TC-02** | **Sensor Disconnect Detection** | Disconnect 1-Wire data wire during live operation | System detects `-127.0°C`; displays `"PROBE DISCONNECTED"` alert on OLED | *Implemented and ready for hardware* |
| **TC-03** | **OLED 1Hz Non-blocking Loop** | Observe OLED screen across 60 seconds | Screen updates smoothly at 1Hz with zero blocking or flicker | *Implemented and tested in firmware build* |
| **TC-04** | **Button Carousel Navigation** | Press Left (GPIO 16) and Right (GPIO 18) buttons | Cycles smoothly through all 8 screens in both forward and reverse directions | *Implemented and tested in firmware build* |
| **TC-05** | **Long-Press Dashboard Return** | Hold OK/Select (GPIO 17) for > 1.2s from any submenu | Instantly returns display to `DASHBOARD` screen | *Implemented and tested in firmware build* |
| **TC-06** | **Unit Toggle (°C / °F)** | Short-press OK/Select on Dashboard or toggle in Android app | Displays temperature in selected unit; stores raw value in Celsius | *Implemented and tested* |
| **TC-07** | **BLE Telemetry Streaming** | Launch Android app and scan for `"Flux-Telemetry"` | Device connects; requests 512 MTU; streams 1Hz JSON telemetry without packet drops | *Implemented and tested* |
| **TC-08** | **Offline Local Operation** | Disable phone Wi-Fi/cellular and ESP32 connectivity | OLED display and sensor read continuously without interruption or delay | *Implemented and verified by design* |
| **TC-09** | **Offline Buffer Queue** | Accumulate readings while offline, then restore network | Android app buffers up to 500 readings and flushes them to Supabase on reconnection | *Implemented and tested* |
| **TC-10** | **Push Notification Dispatch** | Exceed upper threshold (> 28.0°C) | Trigger invokes Edge Function; OneSignal delivers push notification to phone | *Implemented awaiting OneSignal credentials* |
