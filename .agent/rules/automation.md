# Universal Automation & Device Mirroring Guidelines

## 1. Single-Command Execution
The repository provides root-level automation scripts (`automate.bat` and `automate.ps1`) to streamline developer workflows without needing Android Studio:
- **Build, Deploy, Launch, and Mirror in 1 Step**:
  ```cmd
  automate.bat
  ```
  or in PowerShell:
  ```powershell
  .\automate.ps1
  ```
- **ESP32 Firmware Flashing**:
  ```cmd
  automate.bat -Esp32
  ```
- **Unified Full System Deploy (ESP32 + Android + Mirror)**:
  ```cmd
  automate.bat -All
  ```
- **Screen Mirror Only**:
  ```cmd
  automate.bat -Mirror
  ```
- **Run Kotlin Compilation & Unit Tests**:
  ```cmd
  automate.bat -Test
  ```

## 2. Environment Auto-Configuration
The automation script automatically discovers and configures:
- **JDK 17 (`JAVA_HOME`)**: Checks `C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot` and Adoptium installations.
- **Android SDK (`ANDROID_HOME`)**: Checks `C:\Android\Sdk` and adds `platform-tools` to session PATH.
- **PlatformIO (`pio.exe`)**: Discovers virtual environment under `$env:USERPROFILE\.platformio\penv\Scripts`.
- **scrcpy**: Discovers WinGet package under `AppData\Local\Microsoft\WinGet\Packages\Genymobile.scrcpy*`.

## 3. Physical Device Management via ADB
- Always query connected devices via `adb devices` before attempting install.
- Target device identifier is automatically selected or can be overridden via `-DeviceId <id>`.
- Launch command:
  ```powershell
  adb -s <device> shell am start -n com.flux.app/com.example.MainActivity
  ```

## 4. Live Desktop Mirroring (`scrcpy`)
- Standard mirror flags:
  - `--window-title "Flux Mobile Screen"`
  - `--stay-awake` (prevents phone display from sleeping)
  - Optional: `--always-on-top`
- Wireless mirroring can be enabled anytime by running:
  ```powershell
  adb tcpip 5555
  scrcpy --tcpip=<device-ip>:5555
  ```

## 5. Instant Screenshot & Clipboard Companion
- Single-command capture:
  ```powershell
  .\automate.bat -Snap
  ```
- Launches the floating pill `[ Copy Screenshot ] [ X ]` docked to the `Flux Mobile Screen` preview window:
  ```powershell
  .\automate.bat -Screenshot
  ```
- Clicking **Copy Screenshot** captures the mobile screen directly into the **Windows Clipboard** (`SetImage`) in memory (no files saved on disk). The user can instantly paste into chat with Antigravity via `Ctrl + V`.

