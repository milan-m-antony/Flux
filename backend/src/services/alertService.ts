import { getSupabaseClient } from '../config/supabase';
import { DeviceAlertsQuery } from '../types/telemetry';

export async function getDeviceAlerts(deviceId: string, query: DeviceAlertsQuery) {
  const supabase = getSupabaseClient();

  let req = supabase
    .from('alert_events')
    .select('id, device_id, event_type, temperature_celsius, configured_threshold, active_profile, processing_status, onesignal_notification_id, created_at, resolved_at', { count: 'exact' })
    .eq('device_id', deviceId)
    .order('created_at', { ascending: false })
    .range(query.offset, query.offset + query.limit - 1);

  if (query.status) {
    req = req.eq('processing_status', query.status);
  }

  const { data, count, error } = await req;

  if (error) {
    throw new Error(`Failed to fetch device alerts: ${error.message}`);
  }

  return {
    data: (data || []).map((row) => ({
      id: row.id,
      deviceId: row.device_id,
      eventType: row.event_type,
      temperatureCelsius: Number(row.temperature_celsius),
      configuredThreshold: row.configured_threshold ? Number(row.configured_threshold) : null,
      activeProfile: row.active_profile,
      processingStatus: row.processing_status,
      onesignalNotificationId: row.onesignal_notification_id,
      createdAt: row.created_at,
      resolvedAt: row.resolved_at
    })),
    pagination: {
      limit: query.limit,
      offset: query.offset,
      total: count || 0
    }
  };
}
