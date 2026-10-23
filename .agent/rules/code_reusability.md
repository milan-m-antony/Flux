# Code Reusability & Modularity Guidelines

## 1. Single Source of Truth
- **Theme & Design System**: Always reuse tokens defined in `com.example.ui.theme`:
  - Colors: `FluxOrange`, `FluxBgLight`, `FluxCardWhite`, `FluxCardBorder`, `FluxTextDark`, `FluxTextMuted`, `FluxTextLight`, etc.
  - Avoid ad-hoc hardcoded hex colors when standard design tokens already exist.
- **Data Models**: Reuse existing domain models (`BleDevice`, `FluxReading`, `FluxConnectivityState`, etc.) rather than defining parallel ad-hoc structures.

## 2. Jetpack Compose Component Reusability
- **Extract Reusable Composables**: When a UI element (card, button, pill, stat display, indicator) is used in more than one place, extract it as a standalone, previewable `@Composable` in `com.example.ui.components` or a dedicated screen component.
- **Composable Signatures**:
  - Expose customizable modifiers (`modifier: Modifier = Modifier`).
  - Pass callbacks (`onNavigate`, `onClick`, etc.) instead of hardcoding navigation logic.
  - Accept state rather than embedding local state when the component can be stateless (state hoisting).
- **Reusable Canvas & Geometry Utilities**:
  - Centralize math, trigonometry, spline smoothing, and path drawing logic into helper functions instead of re-implementing cubic Bézier interpolations in multiple places.

## 3. Architecture & Separation of Concerns
- **ViewModel Layer**: Keep business logic, BLE connection states, and network telemetry in `MainViewModel` or specialized coordinators (`FluxConnectivityCoordinator`, `BLEConnectionManager`).
- **Compose Layer**: Keep composables purely focused on presentation, layout, animation, and emitting user events.
- **Repository Pattern**: Centralize database and API calls in repository classes, reusing them across view models and background workers.
