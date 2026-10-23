# Flux Firmware Documentation

Flux is an ESP32-based high-reliability smart temperature telemetry firmware built with PlatformIO and the Arduino framework.

This repository contains the production firmware for the Flux edge telemetry device, monitoring ambient environment, liquid probes, and biological fermentation processes with real-time Bluetooth Low Energy (BLE) GATT streaming and local display telemetry.

## Hardware Target

- ESP32 DevKit V1 / ESP32-WROOM-32
- DS18B20 digital temperature probe (GPIO 4)
- 3 tactile push buttons for local navigation & calibration
- TP4056 Li-ion battery charging module with ADC monitoring
- 3.7V 18650 Li-ion battery

## Features Implemented

- **DS18B20 Precision Temperature Sensing**: Real-time reading with CRC check.
- **BLE GATT Streaming Server**: 512 MTU JSON payload broadcast at 1.0 Hz with profile data.
- **Battery & Power Management**: Real-time voltage sampling and battery percentage reporting.
- **Profile Modes**: Live Monitor, Environment, Liquid, and Fermentation profiles.
- **Non-blocking Architecture**: Strict `millis()` scheduling without blocking `delay()`.

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
    ButtonManager/
    BluetoothManager/
    PowerManager/
    SensorManager/
    Utils/
```

## Build Status

Build with PlatformIO:

```powershell
pio run -d esp32
```