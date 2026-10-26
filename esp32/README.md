# Flux Firmware Documentation

Flux is an ESP32-based high-reliability smart temperature telemetry firmware built with PlatformIO and the Arduino framework.

This repository contains the production firmware for the Flux edge telemetry device, monitoring ambient environment, liquid probes, and biological fermentation processes with real-time Bluetooth Low Energy (BLE) GATT streaming, local SSD1306 OLED display, and audible alarm alerts on GPIO 25.

## Hardware Target

- **Microcontroller:** ESP32 DevKit V1 / ESP32-WROOM-32 (Silicon Labs CP210x USB bridge on COM10)
- **Temperature Probe:** DS18B20 digital 1-Wire temperature probe on **GPIO 4** (with 4.7kΩ pull-up)
- **Display:** 0.96-inch monochrome SSD1306 OLED (128×64) on I2C (**GPIO 21 SDA, GPIO 22 SCL**)
- **Audible Alert Buzzer:** Active or passive piezo buzzer on **GPIO 25**
- **Navigation Buttons:** 3 tactile push buttons on **GPIO 16 (L), GPIO 17 (OK), GPIO 18 (R)**
- **Battery Management:** Li-ion ADC voltage monitoring and percentage reporting

## Features Implemented

- **DS18B20 Precision Temperature Sensing:** Non-blocking 12-bit conversion with CRC verification.
- **Audible Alert Buzzer (GPIO 25):** 100% non-blocking cooperative state machine reserved exclusively for genuine active temperature-threshold alarms (`2400 Hz / 2600 Hz` periodic pip burst). Complete silence during boot, menu navigation, and BLE connect/disconnect; instant silencing on normal temperature, sensor disconnect, read fault, or mute.
- **SSD1306 Local OLED Display:** 8 hierarchical screens including Main Monitor, Mode Selection, Settings, Preferences, Wi-Fi, Bluetooth, and Device Diagnostics.
- **Tactile Button Debouncer:** 50ms software debouncing with long-press (> 1.2s) instant return to Main Monitor.
- **BLE GATT Streaming Server:** 512 MTU single-packet JSON telemetry broadcast at 1.0 Hz with session stats and buzzer state.
- **Battery & Power Management:** Real-time voltage sampling and battery percentage reporting.
- **Non-blocking Cooperative Scheduler:** Strict `millis()` delta scheduling with zero blocking `delay()`.

## Project Structure

```text
src/
    main.cpp
    app/
        DeviceApp.hpp
        DeviceApp.cpp

include/
    FluxConfig.hpp
    DeviceState.hpp
    DisplayManagerConfig.hpp

lib/
    BatteryManager/
    BluetoothManager/
    ButtonManager/
    BuzzerManager/
    DisplayManager/
    PowerManager/
    SensorManager/
    SettingsManager/
    Utils/
```

## Build & Flash Commands

Using PlatformIO Core via PowerShell:

```powershell
# Compile firmware
& "$env:USERPROFILE\.platformio\penv\Scripts\pio.exe" run -d esp32

# Upload to hardware (e.g. COM10)
& "$env:USERPROFILE\.platformio\penv\Scripts\pio.exe" run --target upload -d esp32

# Open serial monitor at 115200 baud
& "$env:USERPROFILE\.platformio\penv\Scripts\pio.exe" device monitor --baud 115200 -d esp32
```
