# Flux — Supabase Cloud Backend & Database Architecture

## 1. Cloud Role & Design Philosophy

**Supabase** serves as the exclusive cloud backend for **Flux — Smart Telemetry System**. It handles:
- Time-series telemetry storage for historical charting.
- Latest device state caching for instant UI hydration.
- Real-time PostgreSQL WAL streaming to the Android companion application.
- Server-side alert threshold evaluation with deduplication hysteresis.
- User preference persistence.

> [!IMPORTANT]
> **Zero Firebase Telemetry Writes:** All telemetry, device state, and alert logs are routed to Supabase. The existing Firebase project remains untouched, preserving its quota for external applications.

---

## 2. Database Schema (`supabase/migrations/20261023_flux_telemetry_schema.sql`)

The database consists of 5 relational tables protected by PostgreSQL Row Level Security (RLS):

```
+--------------------+        +---------------------+
|   public.devices   |<-------|   public.telemetry  |
+---------+----------+        +---------------------+
          |
          +------------------>+---------------------+
          |                   | public.device_state |
          |                   +---------------------+
          v
+--------------------+        +---------------------+
|public.alert_events |        |public.user_settings |
+--------------------+        +---------------------+
```

### Table 1: `public.devices`
Registers physical ESP32 telemetry hardware.
- `id` (text, PK): Unique device hardware identifier (e.g., `FLUX-ESP32-A101`).
- `display_name` (text): Human-readable device label.
- `owner_id` (uuid, FK to `auth.users`): Supabase user account owning the node.
- `firmware_version` (text): Deployed firmware release (e.g. `2.0.0`).
- `last_seen` (timestamptz): Heartbeat timestamp of last telemetry reception.
- `created_at` (timestamptz): Registration date.

### Table 2: `public.telemetry`
High-throughput time-series sensor readings.
- `id` (uuid, PK): Auto-generated unique reading ID.
- `device_id` (text, FK): Associated hardware unit.
- `timestamp` (timestamptz): Point-in-time timestamp.
- `temperature` (numeric(5,2)): Reading value stored strictly in **Celsius**.
- `mode` (text): Active profile (`LIVE_MONITOR`, `ENVIRONMENT`, `LIQUID`, `FERMENTATION`).
- `session_id` (text, optional): Session grouping identifier.
- `created_at` (timestamptz): Insertion timestamp.
- **Indexes:** `idx_telemetry_device_time (device_id, timestamp DESC)`.

### Table 3: `public.device_state`
Single-row snapshot of latest device metrics for instant dashboard queries.
- `device_id` (text, PK): Hardware identifier.
- `latest_temperature` (numeric(5,2)): Most recent valid temperature in °C.
- `active_profile` (text): Active software profile.
- `preferred_unit` (text): Display unit (`C` or `F`).
- `last_seen` (timestamptz): Latest contact timestamp.
- `last_synced` (timestamptz): Sync transaction timestamp.

### Table 4: `public.user_settings`
User-level threshold and configuration preferences.
- `user_id` (uuid, PK): Associated user account.
- `preferred_unit` (text): Default `C` or `F`.
- `default_profile` (text): Default startup profile.
- `high_temp_threshold` (numeric(5,2)): Upper safety trip point in °C (Default: `28.0`).
- `low_temp_threshold` (numeric(5,2)): Lower safety trip point in °C (Default: `18.0`).
- `updated_at` (timestamptz).

### Table 5: `public.alert_events`
Audit log of all triggered alarms and dispatch statuses.
- `id` (uuid, PK): Alert event identifier.
- `device_id` (text, FK).
- `event_type` (text): `HIGH_TEMP_BREACH`, `LOW_TEMP_BREACH`, `SENSOR_DISCONNECT`.
- `measured_temp` (numeric(5,2)): Measured value triggering the alarm.
- `threshold` (numeric(5,2)): Configured threshold value breached.
- `triggered_at` (timestamptz): Trigger timestamp.
- `notification_sent` (boolean): OneSignal push delivery status.

---

## 3. Realtime Synchronization

Supabase Realtime is enabled via PostgreSQL Logical Replication:
```sql
ALTER PUBLICATION supabase_realtime ADD TABLE public.telemetry;
ALTER PUBLICATION supabase_realtime ADD TABLE public.device_state;
```
The Android application opens a scoped channel listening to `device_state` updates for the active device ID, receiving sub-second updates without database polling.

---

## 4. Ten-Day Resource & Quota Assessment

Calculated based on continuous telemetry streaming at the default **5-second interval**:

| Metric | 1 Minute | 1 Hour | 1 Day (24h) | 10 Days | Supabase Free Limit | % of Quota |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: |
| **Telemetry Records** | 12 | 720 | 17,280 | **172,800** | Unlimited Rows | Safe |
| **Row Storage Size** | 2.4 KB | 144 KB | 3.45 MB | **34.5 MB** | 500 MB DB Storage | **6.9%** |
| **Realtime Messages** | 12 | 720 | 17,280 | **172,800** | 2,500,000 / mo | **6.9%** |
| **Device State Upserts** | 12 | 720 | 17,280 | **172,800** | Included in DB IO | Safe |

### Cost-Control Recommendations
1. **Configurable Cloud Interval:** While local OLED and BLE update at 1.0 Hz, cloud uploads default to 5.0 seconds. Increasing the interval to 15.0 seconds reduces 10-day storage to just **11.5 MB** and Realtime messages to **57,600**.
2. **Data Retention Policy:** Telemetry older than 30 days is pruned via a scheduled cron job or partitioned table drop, keeping database disk usage permanently under 100 MB.
3. **Offline Queue Management:** The Android client maintains a bounded offline queue capped at **500 records**. If network connectivity is lost for over 40 minutes, the queue retains the newest records, preventing memory leaks and database spikes upon reconnection.

---

## 5. Security & Row Level Security (RLS)

1. **Client Access:** Android app accesses Supabase via the publishable `anon` key.
2. **Device Write Security:** Writes to `telemetry` and `device_state` require authenticated session matching device ownership or a signed JWT from a trusted ingestion endpoint.
3. **Server Secrets:** The Supabase `service_role` key and OneSignal REST API key are strictly held in Edge Function environment secrets and are NEVER exposed to client code or APK builds.
