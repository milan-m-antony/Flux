# Workflow: Build, Deploy & Mirror Android Companion App

## Overview
Automates the full build, deploy, and screen mirroring loop for the Flux Android application using the single-command automation script without launching Android Studio.

## Prerequisites
1. Physical Android phone connected via USB cable with **USB Debugging enabled**.
2. Run `adb devices` to verify device authorization.

## Execution
Run from the repository root:
```powershell
.\automate.ps1
```
or via Command Prompt / Windows Explorer:
```cmd
automate.bat
```

## What Happens Under the Hood
1. Verifies and discovers `JAVA_HOME` (JDK 17) and `ANDROID_HOME` (Android SDK).
2. Verifies connected Android device via `adb devices`.
3. Runs Gradle assembly: `.\gradlew.bat assembleDebug --no-daemon`.
4. Installs the APK onto the physical phone: `adb -s <id> install -r app-debug.apk`.
5. Starts the main activity: `adb -s <id> shell am start -n com.flux.app/com.example.MainActivity`.
6. Launches `scrcpy` in an interactive window with the title `"Flux Mobile Screen"` and `--stay-awake`.
