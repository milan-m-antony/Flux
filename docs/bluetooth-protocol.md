# Flux — Bluetooth Low Energy (BLE) Communication Protocol

## 1. Overview & GATT Profile

Flux uses **Bluetooth Low Energy (BLE)** to establish a fast, non-blocking telemetry link between the ESP32 firmware and the Android companion app. The protocol maintains 100% backward compatibility with legacy NaTrack clients while extending the payload to deliver Flux telemetry profiles, units, session statistics, and alarms.

```
+--------------------+                     +--------------------+
|   ESP32 Peripheral |                     |   Android Central  |
|  (GATT Server)     |                     |   (GATT Client)    |
+---------+----------+                     +---------+----------+
          |                                          |
          |  <--- 1. Scan for "Flux-Telemetry" ----- |
          |                                          |
          |  <--- 2. Connect & Request MTU 512 ----- |
          |                                          |
          |  <--- 3. Subscribe to Characteristic --- |
          |                                          |
          |  --- 4. Notify JSON Telemetry (1Hz) ---> |
          |                                          |
          |  <--- 5. Write Commands (e.g. UNIT:F) -- |
```

---

## 2. UUID Definitions

| Entity | Type | UUID | Description |
| :--- | :--- | :--- | :--- |
| **Primary Telemetry Service** | Service | `6E400011-B5A3-F393-E0A9-E50E24DCCA9E` | Primary custom Nordic UART-style service |
| **Telemetry Notify Char** | Characteristic | `6E400009-B5A3-F393-E0A9-E50E24DCCA9E` | Asynchronous JSON telemetry streamed at 1Hz (`NOTIFY`, `READ`) |
| **Command Write Char** | Characteristic | `6E400002-B5A3-F393-E0A9-E50E24DCCA9E` | Remote configuration command receiver (`WRITE`, `WRITE_NO_RESPONSE`) |
| **Client Characteristic Config** | Descriptor | `00002902-0000-1000-8000-00805F9B34FB` | Standard BLE CCCD for enabling notifications |

---

## 3. Advertising Parameters

- **Advertised Local Name:** `"Flux-Telemetry"`
- **Fallback Matching (Android BLE Scanner):** Matches any device whose name starts with `"Flux"` OR `"NaTrack"`.
- **Advertising Interval:** 100 ms (Min: 0x20, Max: 0x40).
- **Auto-Restart:** Advertising automatically resumes upon client disconnection without resetting MCU state.

---

## 4. Telemetry Payload Format (1Hz JSON Stream)

Transmitted across characteristic `6E400009-B5A3-F393-E0A9-E50E24DCCA9E`.

### JSON Structure

```json
{
  "temp": 24.38,
  "humidity": 0.0,
  "timestamp": 128450,
  "counter": 142,
  "na": 0.0,
  "mode": 0,
  "unit": "C",
  "minTemp": 21.12,
  "maxTemp": 26.50,
  "avgTemp": 23.85,
  "elapsedSec": 284,
  "alert": 0
}
```

### Field Definitions

| Field | Type | Unit | Description | Legacy / Flux |
| :--- | :---: | :---: | :--- | :---: |
| `temp` | Float | °C | Current temperature reading in Celsius | Legacy Compatible |
| `humidity` | Float | % | Hardware placeholder (set to `0.0`) | Legacy Compatible |
| `timestamp` | Long | ms | Monotonic device uptime timestamp in milliseconds | Legacy Compatible |
| `counter` | Int | count | Monotonically incrementing packet sequence number | Legacy Compatible |
| `na` | Float | mmol/L | Legacy telemetry placeholder (set to `0.0`) | Legacy Compatible |
| `mode` | Int | enum | Active profile: `0`=Live, `1`=Environment, `2`=Liquid, `3`=Ferment | **Flux Extension** |
| `unit` | String | "C" / "F" | Current display unit preference on OLED | **Flux Extension** |
| `minTemp` | Float | °C | Lowest valid reading in current session | **Flux Extension** |
| `maxTemp` | Float | °C | Highest valid reading in current session | **Flux Extension** |
| `avgTemp` | Float | °C | Cumulative average of valid samples in session | **Flux Extension** |
| `elapsedSec` | Long | s | Elapsed session timer duration in seconds | **Flux Extension** |
| `alert` | Int | bitmask | Alert status: `0`=Normal, `1`=High Alarm, `2`=Low Alarm | **Flux Extension** |

---

## 5. Control & Command Messages

Sent from the Android app to ESP32 characteristic `6E400002-B5A3-F393-E0A9-E50E24DCCA9E`.

| Command String | Description | ESP32 Action |
| :--- | :--- | :--- |
| `UNIT:C` | Set display unit to Celsius | Updates `DeviceState.preferredUnit = UNIT_C` and refreshes OLED |
| `UNIT:F` | Set display unit to Fahrenheit | Updates `DeviceState.preferredUnit = UNIT_F` and refreshes OLED |
| `MODE:0` | Switch to Live Monitor profile | Switches active OLED profile mode to `PROFILE_LIVE_MONITOR` |
| `MODE:1` | Switch to Environment profile | Switches active OLED profile mode to `PROFILE_ENVIRONMENT` |
| `MODE:2` | Switch to Liquid Probe profile | Switches active OLED profile mode to `PROFILE_LIQUID` |
| `MODE:3` | Switch to Fermentation profile | Switches active OLED profile mode to `PROFILE_FERMENTATION` |
| `RESET_STATS` | Reset session statistics | Resets `minTemp`, `maxTemp`, `avgTemp`, and elapsed session timer to 0 |
| `THRESH:<low>,<high>` | Configure alarm thresholds | Sets low and high alarm trip points (e.g. `THRESH:18.0,28.0`) |

---

## 6. Connection Lifecycle & MTU

1. **MTU Negotiation:** Default BLE MTU is 23 bytes (limiting payload to 20 bytes). The ESP32 firmware configures an MTU of 512 bytes (`BLEDevice::setMTU(512)`). Android requests 512 bytes upon connection (`gatt.requestMtu(512)`), enabling transmission of the complete 140-byte JSON telemetry payload in a single packet without fragmentation.
2. **Reconnection & Resilience:**
   - If the Android client moves out of range, the ESP32 detects the disconnect callback and immediately resumes advertising.
   - The local OLED and temperature sensor continue measuring and displaying at 1Hz without freezing.
   - When the client returns within range, Android's `BleManager` auto-reconnects and resubscribes.
