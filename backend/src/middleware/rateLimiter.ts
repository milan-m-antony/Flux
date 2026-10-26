import rateLimit from 'express-rate-limit';
import { config } from '../config/env';

export const globalRateLimiter = rateLimit({
  windowMs: config.rateLimitWindowMs,
  max: config.rateLimitMaxRequests,
  standardHeaders: true,
  legacyHeaders: false,
  handler: (_req, res) => {
    res.status(429).json({
      success: false,
      error: 'Too many requests. Please slow down and try again shortly.',
      code: 'RATE_LIMIT_EXCEEDED',
      timestamp: new Date().toISOString()
    });
  }
});

// Stricter rate limiter for telemetry ingestion if needed
export const telemetryRateLimiter = rateLimit({
  windowMs: 60 * 1000, // 1 minute
  max: 180, // Allow up to 180 per minute per IP / client (supports multi-device or bursts)
  standardHeaders: true,
  legacyHeaders: false,
  handler: (_req, res) => {
    res.status(429).json({
      success: false,
      error: 'Telemetry ingestion rate limit exceeded.',
      code: 'RATE_LIMIT_EXCEEDED',
      timestamp: new Date().toISOString()
    });
  }
});
