import { Router, Request, Response, NextFunction } from 'express';
import { authenticateFirebaseToken } from '../middleware/auth';
import { verifyDeviceOwnership } from '../middleware/ownership';
import { validateRequest } from '../middleware/validate';
import { HistoricalTelemetryQuerySchema, DeviceAlertsQuerySchema } from '../types/telemetry';
import { getHistoricalTelemetry } from '../services/telemetryService';
import { getLatestDeviceState, claimDevice } from '../services/deviceService';
import { getDeviceAlerts } from '../services/alertService';

export const deviceRouter = Router();

// 1. GET /api/v1/devices/:deviceId/telemetry - historical readings
deviceRouter.get(
  '/:deviceId/telemetry',
  authenticateFirebaseToken,
  verifyDeviceOwnership,
  validateRequest(HistoricalTelemetryQuerySchema, 'query'),
  async (req: Request, res: Response, next: NextFunction) => {
    try {
      const { deviceId } = req.params;
      const result = await getHistoricalTelemetry(deviceId, req.query as any);

      res.status(200).json({
        success: true,
        data: result.data,
        pagination: result.pagination,
        timestamp: new Date().toISOString()
      });
    } catch (err) {
      next(err);
    }
  }
);

// 2. GET /api/v1/devices/:deviceId/state - latest device state snapshot
deviceRouter.get(
  '/:deviceId/state',
  authenticateFirebaseToken,
  verifyDeviceOwnership,
  async (req: Request, res: Response, next: NextFunction) => {
    try {
      const { deviceId } = req.params;
      const state = await getLatestDeviceState(deviceId);

      if (!state) {
        res.status(404).json({
          success: false,
          error: `No state recorded for device '${deviceId}'.`,
          code: 'DEVICE_STATE_NOT_FOUND',
          timestamp: new Date().toISOString()
        });
        return;
      }

      res.status(200).json({
        success: true,
        data: state,
        timestamp: new Date().toISOString()
      });
    } catch (err) {
      next(err);
    }
  }
);

// 3. GET /api/v1/devices/:deviceId/alerts - device alert history
deviceRouter.get(
  '/:deviceId/alerts',
  authenticateFirebaseToken,
  verifyDeviceOwnership,
  validateRequest(DeviceAlertsQuerySchema, 'query'),
  async (req: Request, res: Response, next: NextFunction) => {
    try {
      const { deviceId } = req.params;
      const result = await getDeviceAlerts(deviceId, req.query as any);

      res.status(200).json({
        success: true,
        data: result.data,
        pagination: result.pagination,
        timestamp: new Date().toISOString()
      });
    } catch (err) {
      next(err);
    }
  }
);

// 4. POST /api/v1/devices/:deviceId/claim - explicitly bind/claim an unowned or anonymous device
deviceRouter.post(
  '/:deviceId/claim',
  authenticateFirebaseToken,
  async (req: Request, res: Response, next: NextFunction) => {
    try {
      const { deviceId } = req.params;
      const userId = req.user!.uid;

      const claimed = await claimDevice(deviceId, userId);

      res.status(200).json({
        success: true,
        message: `Device '${deviceId}' successfully claimed by user '${userId}'.`,
        data: claimed,
        timestamp: new Date().toISOString()
      });
    } catch (err: any) {
      const isConflict = err.message?.includes('already claimed');
      res.status(isConflict ? 409 : 500).json({
        success: false,
        error: err.message,
        code: isConflict ? 'DEVICE_ALREADY_CLAIMED' : 'CLAIM_FAILED',
        timestamp: new Date().toISOString()
      });
    }
  }
);
