// ==============================================================================
// Flux — Smart Telemetry System: Supabase Edge Function
// Purpose: Evaluates pending alert events and dispatches OneSignal Push Notifications
// Security: Privileged OneSignal REST API key is stored server-side only
// ==============================================================================

import { serve } from "https://deno.land/std@0.168.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const ONESIGNAL_APP_ID = Deno.env.get("ONESIGNAL_APP_ID") || "";
const ONESIGNAL_REST_API_KEY = Deno.env.get("ONESIGNAL_REST_API_KEY") || "";
const SUPABASE_URL = Deno.env.get("SUPABASE_URL") || "";
const SUPABASE_SERVICE_ROLE_KEY = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") || "";

serve(async (req: Request) => {
  try {
    if (req.method !== "POST") {
      return new Response(JSON.stringify({ error: "Method not allowed" }), {
        status: 405,
        headers: { "Content-Type": "application/json" }
      });
    }

    const payload = await req.json();
    const { record } = payload; // Can be invoked via Database Webhook on alert_events INSERT

    if (!record || !record.device_id || !record.event_type) {
      return new Response(JSON.stringify({ error: "Invalid alert record payload" }), {
        status: 400,
        headers: { "Content-Type": "application/json" }
      });
    }

    // Verify OneSignal credentials
    if (!ONESIGNAL_APP_ID || !ONESIGNAL_REST_API_KEY) {
      console.warn("OneSignal credentials not configured in Edge Function secrets.");
      return new Response(JSON.stringify({ 
        status: "SKIPPED", 
        message: "OneSignal credentials not set. Alert recorded in database." 
      }), {
        status: 200,
        headers: { "Content-Type": "application/json" }
      });
    }

    // Format notification text based on event type
    let title = "Flux Temperature Alert";
    let message = `Temperature reached ${record.temperature_celsius}°C in ${record.active_profile}.`;

    if (record.event_type === "HIGH_TEMP") {
      title = "⚠️ High Temperature Warning";
      message = `High temperature alert: ${record.temperature_celsius}°C exceeded threshold (${record.configured_threshold}°C) in ${record.active_profile}.`;
    } else if (record.event_type === "LOW_TEMP") {
      title = "❄️ Low Temperature Warning";
      message = `Low temperature alert: ${record.temperature_celsius}°C dropped below threshold (${record.configured_threshold}°C) in ${record.active_profile}.`;
    } else if (record.event_type === "RANGE_RECOVERY") {
      title = "✅ Temperature Restored";
      message = `Temperature has returned to acceptable range: ${record.temperature_celsius}°C.`;
    }

    // Construct OneSignal push request
    const oneSignalBody = {
      app_id: ONESIGNAL_APP_ID,
      headings: { en: title },
      contents: { en: message },
      included_segments: ["Subscribed Users"],
      data: {
        device_id: record.device_id,
        temperature: record.temperature_celsius,
        profile: record.active_profile,
        event_type: record.event_type,
        alert_id: record.id
      }
    };

    const oneSignalResponse = await fetch("https://onesignal.com/api/v1/notifications", {
      method: "POST",
      headers: {
        "Content-Type": "application/json; charset=utf-8",
        "Authorization": `Basic ${ONESIGNAL_REST_API_KEY}`
      },
      body: JSON.stringify(oneSignalBody)
    });

    const oneSignalData = await oneSignalResponse.json();

    // Update alert_events row status in Supabase
    if (SUPABASE_URL && SUPABASE_SERVICE_ROLE_KEY) {
      const supabase = createClient(SUPABASE_URL, SUPABASE_SERVICE_ROLE_KEY);
      await supabase
        .from("alert_events")
        .update({
          processing_status: oneSignalResponse.ok ? "SENT" : "FAILED",
          onesignal_notification_id: oneSignalData?.id || null
        })
        .eq("id", record.id);
    }

    return new Response(JSON.stringify({ 
      success: true, 
      notification_id: oneSignalData?.id 
    }), {
      status: 200,
      headers: { "Content-Type": "application/json" }
    });
  } catch (err) {
    return new Response(JSON.stringify({ error: err.message }), {
      status: 500,
      headers: { "Content-Type": "application/json" }
    });
  }
});
