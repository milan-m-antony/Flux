import { Router, Request, Response } from 'express';
import { validateRequiredConfig } from '../config/env';
import { checkSupabaseHealth } from '../config/supabase';

export const healthRouter = Router();

// 1. Process health check (lightweight, zero DB overhead for Render health checks)
healthRouter.get('/health', (_req: Request, res: Response) => {
  res.status(200).json({
    status: 'ok',
    service: 'flux-backend',
    timestamp: new Date().toISOString(),
    uptimeSeconds: Math.floor(process.uptime())
  });
});

// 2. Readiness check (verifies configuration and Supabase database connectivity)
healthRouter.get('/ready', async (_req: Request, res: Response) => {
  try {
    const configStatus = validateRequiredConfig();
    const dbHealth = (await checkSupabaseHealth()) || { ok: false, error: 'Health check returned undefined' };

    const isReady = configStatus.valid && Boolean(dbHealth.ok);
    const statusCode = isReady ? 200 : 503;

    res.status(statusCode).json({
      status: isReady ? 'ready' : 'not_ready',
      service: 'flux-backend',
      timestamp: new Date().toISOString(),
      checks: {
        configuration: {
          status: configStatus.valid ? 'ok' : 'missing_variables',
          missing: configStatus.missing
        },
        database: {
          status: dbHealth.ok ? 'connected' : 'disconnected',
          latencyMs: dbHealth.latencyMs,
          error: dbHealth.error
        }
      }
    });
  } catch (err: any) {
    res.status(503).json({
      status: 'not_ready',
      service: 'flux-backend',
      timestamp: new Date().toISOString(),
      error: err?.message || 'Readiness probe failed'
    });
  }
});
