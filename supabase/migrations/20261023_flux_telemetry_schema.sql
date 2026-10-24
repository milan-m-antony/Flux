-- ==============================================================================
-- Flux — Smart Telemetry System: PostgreSQL Database Schema & RLS Migrations
-- Target: Supabase (PostgreSQL 15+)
-- Date: 2026-10-23
-- ==============================================================================

-- 1. Enable required extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 2. Devices Table
CREATE TABLE IF NOT EXISTS public.devices (
    id TEXT PRIMARY KEY,                           -- Unique device identifier (e.g. BLE MAC or hardware UUID)
    display_name TEXT NOT NULL DEFAULT 'Flux Device',
    owner_id TEXT NOT NULL DEFAULT 'anonymous',    -- References Supabase auth.users uid or authorized tenant
    firmware_version TEXT NOT NULL DEFAULT 'v2.0.0',
    device_status TEXT NOT NULL DEFAULT 'ONLINE',  -- ONLINE, OFFLINE, MAINTENANCE, ALERT
    battery_percentage INT DEFAULT 100,
    battery_voltage NUMERIC(4, 2) DEFAULT 4.20,
    last_seen TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Index for device lookups by owner
CREATE INDEX IF NOT EXISTS idx_devices_owner ON public.devices(owner_id);

-- 3. Telemetry Table (Time-series / historical telemetry records)
CREATE TABLE IF NOT EXISTS public.telemetry (
    id BIGSERIAL PRIMARY KEY,
    device_id TEXT NOT NULL REFERENCES public.devices(id) ON DELETE CASCADE,
    recorded_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    temperature_celsius NUMERIC(5, 2) NOT NULL,
    active_profile TEXT NOT NULL DEFAULT 'Live Monitor', -- 'Live Monitor', 'Environment', 'Liquid Probe', 'Fermentation'
    source TEXT NOT NULL DEFAULT 'BLE',
    session_id TEXT,
    is_alert BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Efficient composite indexes for time-series queries
CREATE INDEX IF NOT EXISTS idx_telemetry_device_time ON public.telemetry(device_id, recorded_at DESC);
CREATE INDEX IF NOT EXISTS idx_telemetry_profile ON public.telemetry(active_profile);

-- 4. Device State Table (Latest single-row snapshot for instant fast reads & Realtime sync)
CREATE TABLE IF NOT EXISTS public.device_state (
    device_id TEXT PRIMARY KEY REFERENCES public.devices(id) ON DELETE CASCADE,
    latest_temperature_celsius NUMERIC(5, 2) NOT NULL,
    active_profile TEXT NOT NULL DEFAULT 'Live Monitor',
    preferred_unit TEXT NOT NULL DEFAULT 'C',            -- 'C' or 'F'
    battery_percentage INT DEFAULT 100,
    battery_voltage NUMERIC(4, 2) DEFAULT 4.20,
    charging BOOLEAN DEFAULT FALSE,
    sensor_connected BOOLEAN DEFAULT TRUE,
    is_alert BOOLEAN DEFAULT FALSE,
    status_text TEXT DEFAULT 'Normal',
    uptime_seconds BIGINT DEFAULT 0,
    last_seen TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_sync TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 5. User Settings Table
CREATE TABLE IF NOT EXISTS public.user_settings (
    user_id TEXT PRIMARY KEY,
    preferred_unit TEXT NOT NULL DEFAULT 'C',
    default_profile TEXT NOT NULL DEFAULT 'Live Monitor',
    low_threshold_celsius NUMERIC(5, 2) NOT NULL DEFAULT 18.00,
    high_threshold_celsius NUMERIC(5, 2) NOT NULL DEFAULT 28.00,
    cloud_upload_interval_sec INT NOT NULL DEFAULT 5,    -- Configurable: 5s default
    onesignal_player_id TEXT,
    push_notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 6. Alert Events Table
CREATE TABLE IF NOT EXISTS public.alert_events (
    id BIGSERIAL PRIMARY KEY,
    device_id TEXT NOT NULL REFERENCES public.devices(id) ON DELETE CASCADE,
    event_type TEXT NOT NULL,                           -- 'HIGH_TEMP', 'LOW_TEMP', 'RANGE_RECOVERY', 'SENSOR_FAULT'
    temperature_celsius NUMERIC(5, 2) NOT NULL,
    configured_threshold NUMERIC(5, 2),
    active_profile TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    processing_status TEXT NOT NULL DEFAULT 'PENDING',  -- 'PENDING', 'SENT', 'RESOLVED', 'SUPPRESSED'
    onesignal_notification_id TEXT,
    resolved_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_alert_events_device ON public.alert_events(device_id, created_at DESC);

-- ==============================================================================
-- 7. Row Level Security (RLS) Policies
-- ==============================================================================

ALTER TABLE public.devices ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.telemetry ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.device_state ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_settings ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.alert_events ENABLE ROW LEVEL SECURITY;

-- Allow authenticated users to view & manage their own devices
CREATE POLICY "Users can manage their own devices"
ON public.devices FOR ALL
USING (auth.uid()::text = owner_id OR owner_id = 'anonymous')
WITH CHECK (auth.uid()::text = owner_id OR owner_id = 'anonymous');

-- Telemetry read policy
CREATE POLICY "Users can read telemetry from their devices"
ON public.telemetry FOR SELECT
USING (
    EXISTS (
        SELECT 1 FROM public.devices d
        WHERE d.id = telemetry.device_id
          AND (d.owner_id = auth.uid()::text OR d.owner_id = 'anonymous')
    )
);

-- Telemetry insert policy (for authenticated users or ingestion API)
CREATE POLICY "Allow telemetry insertion for authorized devices"
ON public.telemetry FOR INSERT
WITH CHECK (
    EXISTS (
        SELECT 1 FROM public.devices d
        WHERE d.id = telemetry.device_id
    )
);

-- Device State policies (Realtime pub/sub)
CREATE POLICY "Users can read latest state for their devices"
ON public.device_state FOR SELECT
USING (
    EXISTS (
        SELECT 1 FROM public.devices d
        WHERE d.id = device_state.device_id
          AND (d.owner_id = auth.uid()::text OR d.owner_id = 'anonymous')
    )
);

CREATE POLICY "Allow upsert of latest device state"
ON public.device_state FOR ALL
USING (true)
WITH CHECK (true);

-- User settings policy
CREATE POLICY "Users can read and update their own settings"
ON public.user_settings FOR ALL
USING (auth.uid()::text = user_id OR user_id = 'anonymous')
WITH CHECK (auth.uid()::text = user_id OR user_id = 'anonymous');

-- Alert events policy
CREATE POLICY "Users can view alerts for their devices"
ON public.alert_events FOR SELECT
USING (
    EXISTS (
        SELECT 1 FROM public.devices d
        WHERE d.id = alert_events.device_id
          AND (d.owner_id = auth.uid()::text OR d.owner_id = 'anonymous')
    )
);

-- ==============================================================================
-- 8. Realtime Replication Enablement
-- ==============================================================================
-- Add telemetry, device_state and alert_events to Supabase realtime publication
ALTER PUBLICATION supabase_realtime ADD TABLE public.telemetry;
ALTER PUBLICATION supabase_realtime ADD TABLE public.device_state;
ALTER PUBLICATION supabase_realtime ADD TABLE public.alert_events;

-- ==============================================================================
-- 9. Automated Latest-State & Alert Trigger Function
-- ==============================================================================
CREATE OR REPLACE FUNCTION public.handle_new_telemetry()
RETURNS TRIGGER AS $$
DECLARE
    v_low NUMERIC(5,2) := 18.0;
    v_high NUMERIC(5,2) := 28.0;
    v_last_alert_status TEXT;
    v_event_type TEXT := NULL;
    v_threshold NUMERIC(5,2) := NULL;
BEGIN
    -- 1. Upsert into device_state for single-row instant access
    INSERT INTO public.device_state (
        device_id,
        latest_temperature_celsius,
        active_profile,
        sensor_connected,
        is_alert,
        last_seen,
        last_sync
    ) VALUES (
        NEW.device_id,
        NEW.temperature_celsius,
        NEW.active_profile,
        TRUE,
        NEW.is_alert,
        NEW.recorded_at,
        NOW()
    )
    ON CONFLICT (device_id) DO UPDATE SET
        latest_temperature_celsius = EXCLUDED.latest_temperature_celsius,
        active_profile = EXCLUDED.active_profile,
        sensor_connected = EXCLUDED.sensor_connected,
        is_alert = EXCLUDED.is_alert,
        last_seen = EXCLUDED.last_seen,
        last_sync = EXCLUDED.last_sync;

    -- Update device last_seen timestamp
    UPDATE public.devices
    SET last_seen = NEW.recorded_at,
        device_status = CASE WHEN NEW.is_alert THEN 'ALERT' ELSE 'ONLINE' END
    WHERE id = NEW.device_id;

    -- 2. Threshold Alert Evaluation with Hysteresis & Deduplication
    IF NEW.temperature_celsius > v_high THEN
        v_event_type := 'HIGH_TEMP';
        v_threshold := v_high;
    ELSIF NEW.temperature_celsius < v_low THEN
        v_event_type := 'LOW_TEMP';
        v_threshold := v_low;
    END IF;

    IF v_event_type IS NOT NULL THEN
        -- Check if an active unresolved alert already exists within the last 15 minutes to prevent alert storms
        SELECT processing_status INTO v_last_alert_status
        FROM public.alert_events
        WHERE device_id = NEW.device_id
          AND event_type = v_event_type
          AND created_at >= (NOW() - INTERVAL '15 minutes')
        ORDER BY created_at DESC
        LIMIT 1;

        IF v_last_alert_status IS NULL THEN
            INSERT INTO public.alert_events (
                device_id,
                event_type,
                temperature_celsius,
                configured_threshold,
                active_profile,
                processing_status
            ) VALUES (
                NEW.device_id,
                v_event_type,
                NEW.temperature_celsius,
                v_threshold,
                NEW.active_profile,
                'PENDING'
            );
        END IF;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Trigger firing after each telemetry insertion
DROP TRIGGER IF EXISTS trg_after_telemetry_insert ON public.telemetry;
CREATE TRIGGER trg_after_telemetry_insert
AFTER INSERT ON public.telemetry
FOR EACH ROW
EXECUTE FUNCTION public.handle_new_telemetry();

-- ==============================================================================
-- 10. Seed Initial Device Record
-- ==============================================================================
INSERT INTO public.devices (id, display_name, owner_id, firmware_version, device_status)
VALUES ('Flux1', 'Flux Smart Monitor', 'anonymous', 'v2.0.0', 'ONLINE')
ON CONFLICT (id) DO UPDATE SET
    last_seen = NOW(),
    device_status = 'ONLINE';

