import { Request, Response, NextFunction } from 'express';
import { getSupabaseClient } from '../config/supabase';

export async function verifyDeviceOwnership(req: Request, res: Response, next: NextFunction): Promise<void> {
  const userId = req.user?.uid;
  if (!userId) {
    res.status(401).json({
      success: false,
      error: 'Unauthenticated user.',
      code: 'UNAUTHORIZED',
      timestamp: new Date().toISOString()
    });
    return;
  }

  // Determine deviceId from route parameter or body
  const deviceId = req.params.deviceId || req.body?.deviceId;
  if (!deviceId) {
    res.status(400).json({
      success: false,
      error: 'deviceId is required to verify ownership.',
      code: 'MISSING_DEVICE_ID',
      timestamp: new Date().toISOString()
    });
    return;
  }

  try {
    const supabase = getSupabaseClient();
    const { data: device, error } = await supabase
      .from('devices')
      .select('id, owner_id, display_name')
      .eq('id', deviceId)
      .maybeSingle();

    if (error) {
      res.status(500).json({
        success: false,
        error: `Database error while verifying device ownership: ${error.message}`,
        code: 'DATABASE_ERROR',
        timestamp: new Date().toISOString()
      });
      return;
    }

    if (!device) {
      // If it's a POST to telemetry, auto-register the new device under the authenticated user
      if (req.method === 'POST') {
        const { error: insertError } = await supabase.from('devices').insert({
          id: deviceId,
          display_name: `Flux ${deviceId}`,
          owner_id: userId,
          device_status: 'ONLINE',
          last_seen: new Date().toISOString()
        });

        if (insertError) {
          res.status(500).json({
            success: false,
            error: `Failed to register new device: ${insertError.message}`,
            code: 'DEVICE_REGISTRATION_FAILED',
            timestamp: new Date().toISOString()
          });
          return;
        }

        next();
        return;
      }

      // For GET requests on a non-existent device
      res.status(404).json({
        success: false,
        error: `Device '${deviceId}' not found.`,
        code: 'DEVICE_NOT_FOUND',
        timestamp: new Date().toISOString()
      });
      return;
    }

    // If device is assigned to 'anonymous', claim it for the authenticated user
    if (device.owner_id === 'anonymous') {
      const { error: claimError } = await supabase
        .from('devices')
        .update({ owner_id: userId })
        .eq('id', deviceId);

      if (claimError) {
        console.warn(`[DeviceClaim] Warning: Failed to update anonymous device owner: ${claimError.message}`);
      }
      next();
      return;
    }

    // Verify ownership matches
    if (device.owner_id !== userId) {
      res.status(403).json({
        success: false,
        error: 'Access denied: You do not have permission to view or manage this device.',
        code: 'FORBIDDEN',
        timestamp: new Date().toISOString()
      });
      return;
    }

    next();
  } catch (err: any) {
    res.status(500).json({
      success: false,
      error: `Unexpected error during ownership check: ${err?.message || err}`,
      code: 'INTERNAL_ERROR',
      timestamp: new Date().toISOString()
    });
  }
}
