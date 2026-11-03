# Alerts, FCM & Telemetry Engine Guidelines

## 1. Zero Mock Data Policy
- Mock, hardcoded, or simulator alert data is strictly prohibited in production code paths.
- The alerts screen (`FluxAlertsScreen`) reads live records exclusively from the local Room database (`FluxDatabase` / `NotificationDao`).
- All simulated test cards and fake dates have been decommissioned.

## 2. Anti-Flap Hysteresis & Cooldown Logic
To prevent alert fatigue and redundant database/push events:
- **Temperature Margin (Hysteresis)**: An anti-flap tolerance buffer of $0.3^\circ\text{C}$ is enforced around both low and high thresholds.
- **Breach Cooldown**: A cooldown period of 5 minutes (`BREACH_COOLDOWN_MS = 300,000L`) suppresses repeated triggers for an ongoing breach.
- **Recovery Events**: When values stabilize back inside normal limits, clear the breach flag and issue a `"Temperature Normalized"` resolution alert.
- **Sensor Disconnection**: If sensor reading returns `status == "Disconnected"` or `temp <= -100.0`, dispatch a critical `"Sensor Disconnected"` alert. When valid readings resume, dispatch `"Sensor Restored"`.

## 3. Real Firebase Cloud Messaging (FCM) Flow
- `MyFirebaseMessagingService` parses incoming FCM payloads (both data and notification payloads).
- Automatically converts incoming pushes into `NotificationEntity` instances and persists them into Room DB via `Dispatchers.IO`.
- Dispatches Android system heads-up notifications across the `flux_sound_alerts_channel_v2` notification channel.
- Automatically refreshes and syncs FCM tokens to Firestore on `onNewToken`.
