import { Router, Request, Response, NextFunction } from 'express';
import { authenticateFirebaseToken } from '../middleware/auth';
import { verifyDeviceOwnership } from '../middleware/ownership';
import { validateRequest } from '../middleware/validate';
import { telemetryRateLimiter } from '../middleware/rateLimiter';
import { IngestTelemetrySchema } from '../types/telemetry';
import { ingestTelemetry } from '../services/telemetryService';

export const telemetryRouter = Router();

// POST /api/v1/telemetry
telemetryRouter.post(
  '/',
  telemetryRateLimiter,
  authenticateFirebaseToken,
  validateRequest(IngestTelemetrySchema, 'body'),
  verifyDeviceOwnership,
  async (req: Request, res: Response, next: NextFunction) => {
    try {
      const result = await ingestTelemetry(req.body);
      const statusCode = result.idempotentReplay ? 200 : 201;

      res.status(statusCode).json({
        success: true,
        data: result,
        message: result.idempotentReplay
          ? 'Telemetry acknowledged (idempotent duplicate submission)'
          : 'Telemetry ingested successfully',
        timestamp: new Date().toISOString()
      });
    } catch (err) {
      next(err);
    }
  }
);
