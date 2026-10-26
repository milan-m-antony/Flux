import { getSupabaseClient } from '../config/supabase';
import { IngestTelemetryInput, HistoricalTelemetryQuery } from '../types/telemetry';

// In-memory LRU cache for idempotency keys to prevent duplicate inserts during network retries
const processedIdempotencyKeys = new Map<string, { timestamp: number; recordId: number | string }>();
const IDEMPOTENCY_TTL_MS = 10 * 60 * 1000; // 10 minutes

function cleanupIdempotencyKeys() {
  const cutoff = Date.now() - IDEMPOTENCY_TTL_MS;
  for (const [key, val] of processedIdempotencyKeys.entries()) {
    if (val.timestamp < cutoff) {
      processedIdempotencyKeys.delete(key);
    }
  }
}

export async function ingestTelemetry(input: IngestTelemetryInput) {
  const supabase = getSupabaseClient();

  // 1. Check idempotency key if provided
  if (input.idempotencyKey) {
    cleanupIdempotencyKeys();
    const existing = processedIdempotencyKeys.get(input.idempotencyKey);
    if (existing) {
      return {
        id: existing.recordId,
        deviceId: input.deviceId,
        temperatureCelsius: input.temperatureCelsius,
        recordedAt: input.recordedAt,
        activeProfile: input.activeProfile,
        isAlert: input.isAlert,
        idempotentReplay: true
      };
    }
  }

  // 2. Check for exact duplicate timestamp for the same device to prevent identical double-insertion
  const { data: existingDup } = await supabase
    .from('telemetry')
    .select('id, temperature_celsius, recorded_at')
    .eq('device_id', input.deviceId)
    .eq('recorded_at', input.recordedAt)
    .maybeSingle();

  if (existingDup) {
    if (input.idempotencyKey) {
      processedIdempotencyKeys.set(input.idempotencyKey, {
        timestamp: Date.now(),
        recordId: existingDup.id
      });
    }
    return {
      id: existingDup.id,
      deviceId: input.deviceId,
      temperatureCelsius: existingDup.temperature_celsius,
      recordedAt: existingDup.recorded_at,
      activeProfile: input.activeProfile,
      isAlert: input.isAlert,
      idempotentReplay: true
    };
  }

  // 3. Insert into public.telemetry
  const { data, error } = await supabase
    .from('telemetry')
    .insert({
      device_id: input.deviceId,
      recorded_at: input.recordedAt,
      temperature_celsius: input.temperatureCelsius,
      active_profile: input.activeProfile,
      is_alert: input.isAlert,
      source: input.source,
      session_id: input.sessionId || null
    })
    .select()
    .single();

  if (error) {
    throw new Error(`Failed to insert telemetry: ${error.message}`);
  }

  // 4. Update optional battery & peripheral fields on device_state if provided
  if (input.batteryPercentage !== undefined || input.batteryVoltage !== undefined || input.statusText !== undefined) {
    const updatePayload: Record<string, any> = {};
    if (input.batteryPercentage !== undefined) updatePayload.battery_percentage = input.batteryPercentage;
    if (input.batteryVoltage !== undefined) updatePayload.battery_voltage = input.batteryVoltage;
    if (input.sensorConnected !== undefined) updatePayload.sensor_connected = input.sensorConnected;
    if (input.statusText !== undefined) updatePayload.status_text = input.statusText;

    if (Object.keys(updatePayload).length > 0) {
      await supabase
        .from('device_state')
        .update(updatePayload)
        .eq('device_id', input.deviceId);
    }
  }

  if (input.idempotencyKey && data?.id) {
    processedIdempotencyKeys.set(input.idempotencyKey, {
      timestamp: Date.now(),
      recordId: data.id
    });
  }

  return {
    id: data.id,
    deviceId: data.device_id,
    temperatureCelsius: Number(data.temperature_celsius),
    recordedAt: data.recorded_at,
    activeProfile: data.active_profile,
    isAlert: data.is_alert,
    source: data.source,
    createdAt: data.created_at,
    idempotentReplay: false
  };
}

export async function getHistoricalTelemetry(deviceId: string, query: HistoricalTelemetryQuery) {
  const supabase = getSupabaseClient();

  let req = supabase
    .from('telemetry')
    .select('id, device_id, temperature_celsius, active_profile, is_alert, source, recorded_at, created_at', { count: 'exact' })
    .eq('device_id', deviceId)
    .order('recorded_at', { ascending: false })
    .range(query.offset, query.offset + query.limit - 1);

  if (query.startDate) {
    req = req.gte('recorded_at', query.startDate);
  }
  if (query.endDate) {
    req = req.lte('recorded_at', query.endDate);
  }
  if (query.profile) {
    req = req.eq('active_profile', query.profile);
  }

  const { data, count, error } = await req;

  if (error) {
    throw new Error(`Failed to fetch historical telemetry: ${error.message}`);
  }

  return {
    data: (data || []).map((row) => ({
      id: row.id,
      deviceId: row.device_id,
      temperatureCelsius: Number(row.temperature_celsius),
      activeProfile: row.active_profile,
      isAlert: row.is_alert,
      source: row.source,
      recordedAt: row.recorded_at,
      createdAt: row.created_at
    })),
    pagination: {
      limit: query.limit,
      offset: query.offset,
      total: count || 0
    }
  };
}
