# BLE & Wi-Fi Provisioning Guidelines

## 1. 1-Step Automated Wi-Fi Quick Sheet
- Modal bottom sheet `FluxQuickWifiSheet` provides a streamlined 1-tap onboarding workflow:
  - Auto-detects the smartphone's currently connected Wi-Fi SSID using Android's `WifiManager`.
  - Automatically loads previously saved network passwords from encrypted `SettingsRepository` DataStore.
  - Formats and dispatches the payload `SET_WIFI:<ssid>,<pass>` across the BLE Command Characteristic (`6E400002-B5A3-F393-E0A9-E50E24DCCA9E`).

## 2. State Machine & Verification
- State transitions: `IDLE` -> `CONNECTING` -> `SUCCESS` or `FAILED`.
- Success is confirmed dynamically when incoming 1Hz BLE telemetry reports `"wifiConnected": true`.
- Dynamic signal strength (`espWifiRssi`) is rendered via an animated signal quality pill (`FluxSignalMeter`).

## 3. Persistent Local Storage
- Successful credentials are saved to `SettingsRepository` via `saveWifiSsid(ssid)` and `saveWifiPassword(pass)`.
- If phone Wi-Fi is disconnected, saved credentials automatically pre-fill the form.
