import express, { Express, Request, Response } from 'express';
import helmet from 'helmet';
import cors from 'cors';
import { config } from './config/env';
import { globalRateLimiter } from './middleware/rateLimiter';
import { errorHandler } from './middleware/errorHandler';
import { healthRouter } from './routes/healthRoutes';
import { telemetryRouter } from './routes/telemetryRoutes';
import { deviceRouter } from './routes/deviceRoutes';

export function createApp(): Express {
  const app: Express = express();

  // 1. Security Headers & CORS
  app.use(helmet());
  app.use(cors({
    origin: config.corsOrigin === '*' ? true : config.corsOrigin,
    credentials: true,
    methods: ['GET', 'POST', 'PUT', 'PATCH', 'DELETE', 'OPTIONS'],
    allowedHeaders: ['Content-Type', 'Authorization', 'X-Requested-With']
  }));

  // 2. Request parsing with strict size limits
  app.use(express.json({ limit: '1mb' }));
  app.use(express.urlencoded({ extended: true, limit: '1mb' }));

  // 3. Global rate limiter
  app.use(globalRateLimiter);

  // 4. Mount Routes
  app.use('/', healthRouter);
  app.use('/api/v1/telemetry', telemetryRouter);
  app.use('/api/v1/devices', deviceRouter);

  // 5. 404 Route Not Found Handler
  app.use((req: Request, res: Response) => {
    res.status(404).json({
      success: false,
      error: `Endpoint not found: ${req.method} ${req.originalUrl}`,
      code: 'ROUTE_NOT_FOUND',
      timestamp: new Date().toISOString()
    });
  });

  // 6. Centralized Error Handler
  app.use(errorHandler);

  return app;
}
