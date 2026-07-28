1uild (check for compile errors)

& "$env:USERPROFILE\.platformio\penv\Scripts\pio.exe" run


2. Upload to ESP32

& "$env:USERPROFILE\.platformio\penv\Scripts\pio.exe" run --target upload




# Build
& "$env:USERPROFILE\.platformio\penv\Scripts\pio.exe" run

# Upload
& "$env:USERPROFILE\.platformio\penv\Scripts\pio.exe" run --target upload

# Serial Monitor
& "$env:USERPROFILE\.platformio\penv\Scripts\pio.exe" device monitor --port COM10 --baud 115200