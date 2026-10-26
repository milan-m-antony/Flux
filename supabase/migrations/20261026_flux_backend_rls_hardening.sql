-- ==============================================================================
-- Flux — Smart Telemetry System: RLS Hardening & Backend Integration Migration
-- Date: 2026-10-26
-- Target: Supabase (PostgreSQL 15+)
-- Purpose:
--   1. Harden permissive device_state and telemetry RLS policies.
--   2. Enforce device ownership for client queries while allowing server-side
--      service-role backend ingestion.
--   3. Preserve Supabase Realtime replication for telemetry, state, and alerts.
-- ==============================================================================

-- 1. Harden device_state RLS policies (Remove open public upsert)
DROP POLICY IF EXISTS "Allow upsert of latest device state" ON public.device_state;

CREATE POLICY "Users can manage latest state for their devices"
ON public.device_state FOR ALL
USING (
    EXISTS (
        SELECT 1 FROM public.devices d
        WHERE d.id = device_state.device_id
          AND (d.owner_id = auth.uid()::text OR d.owner_id = 'anonymous')
    )
)
WITH CHECK (
    EXISTS (
        SELECT 1 FROM public.devices d
        WHERE d.id = device_state.device_id
          AND (d.owner_id = auth.uid()::text OR d.owner_id = 'anonymous')
    )
);

-- 2. Harden telemetry INSERT policy to verify device ownership
DROP POLICY IF EXISTS "Allow telemetry insertion for authorized devices" ON public.telemetry;

CREATE POLICY "Allow telemetry insertion for authorized devices"
ON public.telemetry FOR INSERT
WITH CHECK (
    EXISTS (
        SELECT 1 FROM public.devices d
        WHERE d.id = telemetry.device_id
          AND (d.owner_id = auth.uid()::text OR d.owner_id = 'anonymous')
    )
);

-- 3. Composite index on devices for fast ownership checks
CREATE INDEX IF NOT EXISTS idx_devices_id_owner ON public.devices(id, owner_id);

-- 4. Verify Realtime publication membership
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_publication_tables 
        WHERE pubname = 'supabase_realtime' AND schemaname = 'public' AND tablename = 'telemetry'
    ) THEN
        ALTER PUBLICATION supabase_realtime ADD TABLE public.telemetry;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_publication_tables 
        WHERE pubname = 'supabase_realtime' AND schemaname = 'public' AND tablename = 'device_state'
    ) THEN
        ALTER PUBLICATION supabase_realtime ADD TABLE public.device_state;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_publication_tables 
        WHERE pubname = 'supabase_realtime' AND schemaname = 'public' AND tablename = 'alert_events'
    ) THEN
        ALTER PUBLICATION supabase_realtime ADD TABLE public.alert_events;
    END IF;
END $$;
