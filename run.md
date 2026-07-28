# PlatformIO ESP32 Commands

Since the project was restructured, you need to tell PlatformIO where the `esp32` project directory is by using the `-d esp32` flag.

## 1. Build (check for compile errors)
& "$env:USERPROFILE\.platformio\penv\Scripts\pio.exe" run -d esp32

## 2. Upload to ESP32
& "$env:USERPROFILE\.platformio\penv\Scripts\pio.exe" run --target upload -d esp32

## 3. Serial Monitor
& "$env:USERPROFILE\.platformio\penv\Scripts\pio.exe" device monitor --port COM10 --baud 115200 -d esp32