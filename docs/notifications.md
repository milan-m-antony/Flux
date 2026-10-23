# Flux — OneSignal Push Notifications & Alert Engine

## 1. Overview & Architecture

**OneSignal** is the push notification delivery pipeline for **Flux — Smart Telemetry System**. It is utilized exclusively for mobile notification dispatch, keeping telemetry and database workloads confined to Supabase.

```
+--------------------+        +--------------------+        +--------------------+
|  DS18B20 Sensor    |        |  Supabase Database |        |  Supabase Edge Fn  |
|  (Reads Temp)      |------->|  (INSERT telemetry)|------->| flux-alert-        |
+--------------------+        +---------+----------+        | evaluator          |
                                        |                   +---------+----------+
                                        |                             |
                                        v                             v
+--------------------+        +--------------------+        +--------------------+
| Android Companion  |<-------|  OneSignal REST    |<-------| Evaluates Limits   |
| (Receives Push)    |  Push  |  API v1            | HTTPS  | & Anti-Flap Window |
+--------------------+        +--------------------+        +--------------------+
```

---

## 2. Server-Side Secret Isolation

To prevent security vulnerabilities and reverse-engineering risks:
1. **Zero Client Secrets:** The OneSignal REST API Key is **NEVER** embedded into the Android APK, `BuildConfig`, strings.xml, or ESP32 firmware constants.
2. **Edge Function Secrets:** The privileged REST API key is stored strictly within Supabase Vault / Edge Function Environment Variables:
   ```bash
   supabase secrets set ONESIGNAL_REST_API_KEY="your_onesignal_rest_api_key"
   supabase secrets set ONESIGNAL_APP_ID="your_onesignal_app_id"
   ```

---

## 3. Threshold Evaluation & Hysteresis Logic

The alert engine implements strict safety rules to guarantee reliable notifications without overwhelming the user:

### Trip Conditions
- **High Temperature Breach:** Measured temperature exceeds `user_settings.high_temp_threshold` (e.g. > 28.0°C).
- **Low Temperature Breach:** Measured temperature falls below `user_settings.low_temp_threshold` (e.g. < 18.0°C).
- **Sensor Disconnect:** Sensor returns `DEVICE_DISCONNECTED_C` (-127°C) or invalid NaN.

### Anti-Flap Window (Hysteresis)
- If a temperature hovers around the threshold (e.g. fluctuating between 28.1°C and 27.9°C), a naive system would send dozens of push notifications.
- **Flux Hysteresis Rule:** The Edge Function queries `alert_events` for the same device and event type. Notifications are suppressed if an alert was already dispatched within the preceding **15-minute window** for the same ongoing breach.
- **Re-Arming:** When the temperature stabilizes back inside the normal band, the breach state clears, instantly re-arming the alert engine for any future incident.

---

## 4. Edge Function Dispatch Implementation (`supabase/functions/flux-alert-evaluator/index.ts`)

```typescript
const response = await fetch("https://onesignal.com/api/v1/notifications", {
  method: "POST",
  headers: {
    "Content-Type": "application/json",
    "Authorization": `Basic ${oneSignalKey}`,
  },
  body: JSON.stringify({
    app_id: oneSignalAppId,
    include_aliases: { external_id: [device.owner_id] },
    target_channel: "push",
    headings: { en: "Flux Temperature Alert" },
    contents: { en: alertMessage },
    data: { device_id: deviceId, temp: temperature, type: eventType },
  }),
});
```

---

## 5. Android Client Integration

1. **Permission Handling:** On Android 13+ (API 33+), `Manifest.permission.POST_NOTIFICATIONS` is dynamically requested at runtime via `rememberMultiplePermissionsState`.
2. **User Identity Pairing:** When a user logs in, `OneSignal.login(userId)` links the phone's push token to the Supabase `auth.uid()`, enabling targeted push delivery without broadcasting to all devices.
3. **Notification Click Navigation:** When the user taps a Flux notification, the payload contains `device_id` and `type`, opening the app directly to `FluxLiveMonitorScreen` or `FluxAlertSettingsScreen`.
