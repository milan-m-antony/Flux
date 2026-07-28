# NaTrack Firmware Documentation

NaTrack is an ESP32-based medical prototype firmware built with PlatformIO and the Arduino framework.

This repository currently contains the first implementation stage: a clean, modular ESP32 base project that verifies the board is running and prints runtime status information over Serial.

## Current Hardware Target

- ESP32 DevKit V1 / ESP32-WROOM-32
- MAX30102 pulse oximeter sensor, not implemented yet
- DS18B20 temperature sensor, not implemented yet
- 3 tactile push buttons, not implemented yet
- TP4056 charging module
- 3.7V 18650 Li-ion battery

## What Is Implemented Right Now

The firmware currently does only the ESP32 bring-up and status reporting stage:

- Initializes Serial at 115200 baud.
- Prints an immediate startup status snapshot.
- Repeats the same snapshot every second.
- Uses `millis()` timing instead of `delay()`.
- Uses a multi-file architecture instead of placing everything in `main.cpp`.
- Keeps future modules separated behind their own classes and folders.

The status output includes:

- `Device Started`
- `Free Heap`
- `Chip Model`
- `Flash Size`
- `SDK Version`
- `Running Time`

## Current Project Structure

```text
src/
    main.cpp
    app/
        DeviceApp.hpp
        DeviceApp.cpp

include/
    NaTrackConfig.hpp

lib/
    ButtonManager/
        src/
            ButtonManager.hpp
            ButtonManager.cpp
    BluetoothManager/
        src/
            BluetoothManager.hpp
            BluetoothManager.cpp
    PowerManager/
        src/
            PowerManager.hpp
            PowerManager.cpp
    SensorManager/
        MAX30102/
            src/
                MAX30102Sensor.hpp
                MAX30102Sensor.cpp
        Temperature/
            src/
                TemperatureSensor.hpp
                TemperatureSensor.cpp
    Utils/
        src/
            SystemInfo.hpp
            SystemInfo.cpp
            StatusReporter.hpp
            StatusReporter.cpp
```

## Why Each File Exists

### `src/main.cpp`

This is the smallest possible application entry point. It owns one `DeviceApp` instance and forwards `setup()` and `loop()` into the app layer.

### `src/app/DeviceApp.hpp` and `src/app/DeviceApp.cpp`

These files contain the top-level firmware coordinator. `DeviceApp` initializes each module and calls their non-blocking `update()` methods from the main loop.

### `include/NaTrackConfig.hpp`

This file stores shared constants used by the firmware, such as the Serial baud rate and the reporting interval.

### `lib/Utils/src/SystemInfo.*`

These files collect ESP32 runtime information such as heap size, chip model, flash size, SDK version, and uptime.

### `lib/Utils/src/StatusReporter.*`

These files handle the Serial Monitor output. They format and print the ESP32 status snapshot once per second.

### `lib/ButtonManager/*`

This module is a placeholder for the future button input layer. It currently compiles cleanly but does not read hardware yet.

### `lib/BluetoothManager/*`

This module is a placeholder for the future BLE layer. Bluetooth is intentionally disabled in the current stage.

### `lib/PowerManager/*`

This module is a placeholder for battery and power-management work. It is ready for future voltage or charging-state logic.

### `lib/SensorManager/MAX30102/*`

This module is a placeholder for the pulse oximeter sensor integration.

### `lib/SensorManager/Temperature/*`

This module is a placeholder for the DS18B20 temperature sensor integration.

## Implementation Notes

- The firmware does not use `delay()`.
- Timing is driven by `millis()`.
- The current code is written in modern C++ style with small reusable classes.
- The base project compiles successfully for `esp32dev`.
- Sensor and BLE logic are intentionally not implemented yet.

## Build Status

The project has been verified with:

```powershell
& "$env:USERPROFILE\.platformio\penv\Scripts\pio.exe" run
```

The build completed successfully for the `esp32dev` environment.

## Next Planned Steps

1. Add a dedicated button input implementation.
2. Add BLE support when the communication layer is needed.
3. Add the MAX30102 sensor driver.
4. Add the DS18B20 temperature driver.
5. Add power and battery monitoring.