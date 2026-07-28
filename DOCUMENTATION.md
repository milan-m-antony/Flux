# NaTrack (Sodium Tracking & Hydration Companion) Documentation

Welcome to **NaTrack**, a modern, state-of-the-art Android application designed to pair with external electrochemical Na+ (Sodium) sensors. It helps users manage their hydration, monitor sodium concentration levels, view historical health trends, and receive smart, personalized insights.

Built entirely using **Kotlin**, **Jetpack Compose**, and **Material Design 3**, the application offers a premium, modern design language blending custom **Neo-Brutalist shadow aesthetics** with a responsive layout optimized for all screen sizes.

---

## 📖 Table of Contents
1. [Architecture Overview](#1-architecture-overview)
2. [Screen & UI Modules](#2-screen--ui-modules)
3. [Design System & Styling (Neo-Brutalist)](#3-design-system--styling-neo-brutalist)
4. [Bluetooth Low Energy (BLE) Integration](#4-bluetooth-low-energy-ble-integration)
5. [Calibration Engine (2-Point Electrochemical)](#5-calibration-engine-2-point-electrochemical)
6. [Notification & Alert Subsystem](#6-notification--alert-subsystem)
7. [Local State & Persistence](#7-local-state--persistence)

---

## 1. Architecture Overview

NaTrack follows the **Model-View-ViewModel (MVVM)** architectural pattern combined with clean architecture principles to ensure code separation, testability, and high-performance runtime updates.

```
       ┌────────────────────────┐
       │   Compose UI Screens   │
       │ (HomeScreen, Scan,...) │
       └───────────┬────────────┘
                   │ Collects UI State via Flow
                   ▼
       ┌────────────────────────┐
       │     MainViewModel      │
       │ (State Management, UI) │
       └───────────┬────────────┘
                   │ Orchestrates Data Flows
                   ▼
       ┌────────────────────────┐
       │  BleManager / Repos    │
       │ (BLE Sensor, Settings) │
       └────────────────────────┘
```

- **View (UI Layer)**: Built with Jetpack Compose. Utilizes reactive, state-driven composables that automatically recompose when local or global flows emit new states.
- **ViewModel (State Holder)**: `MainViewModel` exposes state as `StateFlow` streams. It ensures safe threading using Kotlin Coroutines and maintains device-level status across navigation transitions.
- **Model (Service Layer)**: Includes `BleManager` for hardware communications, local data caches, and settings configurations.

---

## 2. Screen & UI Modules

The application is split into highly responsive screens organized via standard Compose Navigation:

### A. Home Dashboard (`HomeScreen.kt`)
- **Header Profile**: Display-style greeting featuring the user's name, customized avatar profile, live BLE device connection indicators, and a badged notification bell.
- **Status Summary**: High-contrast indicator showcasing the user's current sodium status (e.g., *Normal*, *High*, *Low*) alongside real-time time telemetry.
- **Weekly Trend Graph**: Dynamic visualizer that draws custom bezier curves mapping historical sodium trends without heavy third-party canvas overhead.
- **Quick Action Grid**: Quick-access shortcuts to log water intake, initiate sensor calibration, or manage paired hardware.

### B. Device Setup & Scanning (`ScanScreen.kt` & `DeviceSetupScreen.kt`)
- Guides the user through local Bluetooth permissions and scanning.
- Features real-time connection status cards, list-views of available sodium sensors, and flat scanning buttons.

### C. Calibration Center (`CalibrateScreen.kt`)
- Guides the user through standard electrochemical **2-point sensor calibration**.
- Uses an automated backing simulator if live hardware readings are not present, ensuring the app remains fully functional in all test conditions.

### D. Device Actions (`DeviceScreen.kt`)
- Comprehensive management of the connected NaTrack sensor.
- Provides options for simulating firmware installation updates with dynamic progress bars, device specs info modal, and progressive hardware-checking diagnostics.

### E. Notification Center (`NotificationScreen.kt`)
- Real-time hub for warnings, connection states, and goal thresholds.
- Supports swipe-to-dismiss, marking all items read, and dynamic fade animations.

---

## 3. Design System & Styling (Neo-Brutalist)

The application features a gorgeous **Neo-Brutalist theme** structured through Material 3 guidelines (`Theme.kt`):

- **Dynamic Shadows (`neoShadow`)**: Custom Modifier extensions draw distinct dual offset shadows (light top-left highlight and dark bottom-right shadow) to construct a raised tactile glassmorphism theme.
- **Adaptive Lighting**: Fully styled for both extreme Dark Slate theme (true pitch-black backdrop) and pure light themes.
- **Border and Contours**: Card items utilize explicit `1.dp` borders paired with `RoundedCornerShape(32.dp)` for a friendly, visually strong product outline.
- **Scale and Touch Accuracy**: All buttons, action elements, and nav items strictly adhere to the **48dp minimum touch target** for stellar accessibility.

---

## 4. Bluetooth Low Energy (BLE) Integration

The core communication logic resides in `BleManager.kt`:

- **Device Discovery**: Scans for advertisements filtered by specific NaTrack UUID signatures.
- **GATT Connectivity**: Manages connection lifecycles, service discovery, and descriptors.
- **Rx Notifications**: Enables Bluetooth GATT notifications on electrochemical characteristic channels to receive automatic raw millivolt (`mV`) updates as they occur on-sensor.
- **Simulation Layer**: Gracefully falls back to mock sine-wave oscillations if mock-mode is active, keeping telemetry alive for testing.

---

## 5. Calibration Engine (2-Point Electrochemical)

Electrochemical sodium sensors output tiny voltage variations measured in millivolts (`mV`). To translate this into a medically useful concentration ($\text{mmol/L}$), the app utilizes a linear logarithmic calibration model:

$$\text{Concentration} = \text{Base} \times e^{\left(\frac{\text{mV} - V_0}{S}\right)}$$

The `CalibrateScreen` implements a step-by-step wizard to determine the Slope ($S$) and Offset ($V_0$):
1. **Point 1**: Place sensor in standard solution 1 (e.g., $120\text{ mmol/L}$) $\rightarrow$ Record live $mV_1$.
2. **Point 2**: Place sensor in standard solution 2 (e.g., $150\text{ mmol/L}$) $\rightarrow$ Record live $mV_2$.
3. **Calculation**: Compute precise slope values and save calibration metrics to `MainViewModel` to scale future live sensor readings.

---

## 6. Notification & Alert Subsystem

The `NotificationScreen` provides deep interaction with alerts:
- **Alert Categorization**:
  - `ALERT`: Urgent high/low sodium events (styled with high-contrast red Status indicators).
  - `WARNING`: Actions needed (e.g. Low Battery < 20%, Calibration Due).
  - `INFO`: Standard telemetry confirmations (e.g. Device Connected, Daily Goal Reached).
- **Dynamic State Cleaning**: Mark individual items read (re-render with beautiful semi-transparent fade states) or completely clear notifications to trigger an elegant empty "All Caught Up" state layout.

---

## 7. Local State & Persistence
- **Runtime Persistence**: Managed reactively within `MainViewModel` using coroutine state updates.
- **Settings Store**: Configuration flags for push notifications, unit structures (metric/imperial), and historical calibration states are saved dynamically.
