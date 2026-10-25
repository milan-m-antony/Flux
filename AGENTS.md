# Flux Agent Instructions

This repository houses **Flux**, a high-precision smart telemetry and thermal monitoring application built using Jetpack Compose, Kotlin Multiplatform principles, Bluetooth Low Energy (BLE), and Firebase Cloud Messaging (FCM).

---

## 1. Code Reusability & Architecture
- **DRY Principle**: Never duplicate UI layouts, canvas trigonometry, or data mapping logic.
- **Design Tokens**: Centralize and reuse all colors, typography, shapes, and elevation in `com.example.ui.theme`:
  - `FluxOrange`, `FluxBgLight`, `FluxCardWhite`, `FluxCardBorder`, `FluxTextDark`, `FluxTextMuted`, `FluxTextLight`.
- **Component Modularity**: Extract repeating UI cards, pills, indicators, and buttons into dedicated composables in `com.example.ui.components` or component sections.
- **State Management**: Business logic and reactive streams remain in `MainViewModel` and domain coordinators (`FluxConnectivityCoordinator`, `BLEConnectionManager`).

---

## 2. Compilation & Build Verification
- **Compilation Check**: Before concluding any coding task or refactor, always run:
  ```powershell
  .\gradlew compileDebugKotlin --no-daemon
  ```
- **Windows File Lock Recovery**: If daemon locks `R.jar`, stop daemons and clear the intermediate cache:
  ```powershell
  .\gradlew --stop
  Remove-Item -Force -Recurse 'app\build\intermediates\compile_and_runtime_r_class_jar' -ErrorAction SilentlyContinue
  ```
- **Unit Testing**: Validate changes against existing tests:
  ```powershell
  .\gradlew testDebugUnitTest --no-daemon
  ```

---

## 3. UI Design & Layout Standards
- **Header Lock & Frozen Alignment**: The top header (`FluxHeader`) design, dimensions, and element alignments are strictly finalized and frozen. NEVER change the header layout, padding, or its internal offsets (`Flux1` at `offset(y = 2.dp)`, `Online`/`Offline` row at `offset(y = (-1.8).dp)`, `1.dp` vertical spacer, and `FluxSignalMeter` alignment).
- **Single-Page Viewport Balance**: Keep dashboard screens balanced within a single mobile viewport (~640dp - 740dp content height) without requiring scrolling.
- **Header Clearance**: Allow `86.dp` clearance below the fixed header to prevent clipping the top of circular gauges and indicators, with the gauge 'Medium' label at `offset(y = (-14).dp)`.
- **Visual Accuracy**: Math and positioning for radial ticks, thumb knobs, and range labels (`16°C` / `40°C`) must align precisely with arc endpoints.
- **Dynamic Units**: Properly handle unit conversion for all labels and axis ticks when switching between `°C` and `°F`.

---

## 4. Git & Branch Management
- Commit using Conventional Commits format: `feat(...)`, `fix(...)`, `refactor(...)`.
- Keep active branches synchronized: `main`, `dev`, and `onesignal-integration`.

---

## 5. Universal Automation & Device Mirroring
- **Single-Command Pipeline**: Use `.\automate.ps1` (or `automate.bat`) from the workspace root to compile, package, install to connected phone, launch `MainActivity`, and open `scrcpy` live mirroring in 1 step.
- **CLI-Only Operations**: Android Studio is not required. All builds, tests, device pushes, and logs operate through Gradle, ADB, and PlatformIO.
- **Screen Mirroring**: `scrcpy` is configured with `--window-title "Flux Mobile Screen"` and `--stay-awake`. Wire-free debugging operates via `adb tcpip 5555`.

---

## 6. Real-Time Telemetry & Zero Mock Data Policy
- **No Mock or Hardcoded Records**: Simulated alerts, fake test buttons, and hardcoded dates are strictly prohibited.
- **Room DB Ingestion**: Incoming FCM pushes (`MyFirebaseMessagingService`) and BLE breach events write directly to Room database (`NotificationEntity`).
- **Anti-Flap Hysteresis**: Maintain a $0.3^\circ\text{C}$ buffer and a 5-minute cooldown (`BREACH_COOLDOWN_MS = 300,000L`) to suppress alert flap.
- **Recovery Alerts**: Emit resolution notifications (`"Sensor Restored"`, `"Temperature Normalized"`) when safe thresholds return.

