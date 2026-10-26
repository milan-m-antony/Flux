import { getSupabaseClient } from '../config/supabase';

export async function getLatestDeviceState(deviceId: string) {
  const supabase = getSupabaseClient();

  // 1. Get state snapshot
  const { data: state, error: stateError } = await supabase
    .from('device_state')
    .select('*')
    .eq('device_id', deviceId)
    .maybeSingle();

  if (stateError) {
    throw new Error(`Failed to fetch device state: ${stateError.message}`);
  }

  // 2. Get device metadata
  const { data: device, error: devError } = await supabase
    .from('devices')
    .select('id, display_name, owner_id, firmware_version, device_status, last_seen')
    .eq('id', deviceId)
    .maybeSingle();

  if (devError) {
    throw new Error(`Failed to fetch device info: ${devError.message}`);
  }

  if (!state && !device) {
    return null;
  }

  return {
    deviceId,
    displayName: device?.display_name || `Flux ${deviceId}`,
    deviceStatus: device?.device_status || (state?.is_alert ? 'ALERT' : 'ONLINE'),
    firmwareVersion: device?.firmware_version || 'v2.0.0',
    ownerId: device?.owner_id,
    latestTemperatureCelsius: state ? Number(state.latest_temperature_celsius) : null,
    activeProfile: state?.active_profile || 'Live Monitor',
    preferredUnit: state?.preferred_unit || 'C',
    batteryPercentage: state?.battery_percentage ?? 100,
    batteryVoltage: state?.battery_voltage ? Number(state.battery_voltage) : 4.2,
    charging: state?.charging ?? false,
    sensorConnected: state?.sensor_connected ?? true,
    isAlert: state?.is_alert ?? false,
    statusText: state?.status_text || 'Normal',
    uptimeSeconds: state?.uptime_seconds ?? 0,
    lastSeen: device?.last_seen || state?.last_seen || null,
    lastSync: state?.last_sync || null
  };
}

export async function claimDevice(deviceId: string, userId: string) {
  const supabase = getSupabaseClient();

  const { data: existing, error: findError } = await supabase
    .from('devices')
    .select('id, owner_id')
    .eq('id', deviceId)
    .maybeSingle();

  if (findError) {
    throw new Error(`Error looking up device: ${findError.message}`);
  }

  if (existing && existing.owner_id !== 'anonymous' && existing.owner_id !== userId) {
    throw new Error('Device is already claimed by another user');
  }

  if (!existing) {
    const { data: created, error: createError } = await supabase
      .from('devices')
      .insert({
        id: deviceId,
        display_name: `Flux ${deviceId}`,
        owner_id: userId,
        device_status: 'ONLINE',
        last_seen: new Date().toISOString()
      })
      .select()
      .single();

    if (createError) throw new Error(`Failed to claim new device: ${createError.message}`);
    return created;
  }

  const { data: updated, error: updateError } = await supabase
    .from('devices')
    .update({ owner_id: userId })
    .eq('id', deviceId)
    .select()
    .single();

  if (updateError) throw new Error(`Failed to update device owner: ${updateError.message}`);
  return updated;
}
