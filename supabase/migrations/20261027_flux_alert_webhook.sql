-- ==============================================================================
-- Flux — Smart Telemetry System: Database Webhook for Alert Edge Function
-- Enables pg_net and creates trigger on alert_events to invoke flux-alert-evaluator
-- ==============================================================================

CREATE EXTENSION IF NOT EXISTS pg_net WITH SCHEMA extensions;

CREATE OR REPLACE FUNCTION public.trigger_flux_alert_webhook()
RETURNS TRIGGER AS $$
DECLARE
    v_url TEXT := 'https://pivtrxrhxdqxzactcrrb.supabase.co/functions/v1/flux-alert-evaluator';
    v_payload JSONB;
BEGIN
    v_payload := jsonb_build_object(
        'type', TG_OP,
        'table', TG_TABLE_NAME,
        'schema', TG_TABLE_SCHEMA,
        'record', to_jsonb(NEW)
    );

    PERFORM net.http_post(
        url := v_url,
        body := v_payload,
        headers := jsonb_build_object('Content-Type', 'application/json')
    );

    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS trg_alert_events_edge_webhook ON public.alert_events;

CREATE TRIGGER trg_alert_events_edge_webhook
AFTER INSERT ON public.alert_events
FOR EACH ROW
EXECUTE FUNCTION public.trigger_flux_alert_webhook();
