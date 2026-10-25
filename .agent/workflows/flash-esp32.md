# Workflow: Build, Flash & Monitor ESP32 Firmware

## Overview
Compiles and flashes the PlatformIO firmware project onto the ESP32 hardware node.

## Prerequisites
1. ESP32 board connected via USB cable (typically registers on `COM10` via Silicon Labs CP210x driver).
2. PlatformIO Core installed (`pio`).

## Execution
Run from repository root:
```powershell
.\automate.ps1 -Esp32 -Port COM10
```
To flash and immediately attach the 115200 baud serial monitor:
```powershell
.\automate.ps1 -Esp32 -Port COM10 -Monitor
```

To flash both ESP32 and deploy the Android app simultaneously:
```powershell
.\automate.ps1 -All
```
