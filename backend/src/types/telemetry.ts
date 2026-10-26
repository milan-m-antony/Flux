import { z } from 'zod';

export const TelemetryProfileEnum = z.enum([
  'Live Monitor',
  'Environment',
  'Liquid Probe',
  'Fermentation',
]);

export type TelemetryProfile = z.infer<typeof TelemetryProfileEnum>;

// Ingest telemetry schema
export const IngestTelemetrySchema = z.object({
  deviceId: z
    .string({ required_error: 'deviceId is required' })
    .min(1, 'deviceId cannot be empty')
    .max(64, 'deviceId exceeds maximum length of 64')
    .regex(/^[A-Za-z0-9:_-]+$/, 'deviceId must contain only alphanumeric characters, colons, underscores, or dashes'),
  temperatureCelsius: z
    .number({ required_error: 'temperatureCelsius is required' })
    .min(-55.0, 'temperatureCelsius below hardware limit (-55°C)')
    .max(125.0, 'temperatureCelsius above hardware limit (+125°C)'),
  recordedAt: z
    .union([z.string().datetime(), z.number().int().positive()])
    .optional()
    .transform((val) => {
      if (!val) return new Date().toISOString();
      if (typeof val === 'number') return new Date(val).toISOString();
      return val;
    })
    .refine((iso) => {
      const date = new Date(iso);
      const now = Date.now();
      // Must not be earlier than 2020-01-01 and not more than 1 hour in the future
      return date.getTime() > 1577836800000 && date.getTime() < now + 3600000;
    }, { message: 'recordedAt must be a valid timestamp between 2020 and 1 hour in the future' }),
  activeProfile: z
    .string()
    .default('Live Monitor')
    .refine((val) => ['Live Monitor', 'Environment', 'Liquid Probe', 'Fermentation'].includes(val), {
      message: 'activeProfile must be one of: Live Monitor, Environment, Liquid Probe, Fermentation'
    }),
  isAlert: z.boolean().default(false),
  source: z.string().max(32).default('BLE'),
  sessionId: z.string().max(64).optional(),
  batteryPercentage: z.number().int().min(0).max(100).optional(),
  batteryVoltage: z.number().min(0.0).max(5.5).optional(),
  sensorConnected: z.boolean().optional().default(true),
  statusText: z.string().max(64).optional(),
  idempotencyKey: z.string().max(128).optional()
});

export type IngestTelemetryInput = z.infer<typeof IngestTelemetrySchema>;

// Query parameters for historical telemetry
export const HistoricalTelemetryQuerySchema = z.object({
  limit: z.coerce.number().int().min(1).max(500).default(50),
  offset: z.coerce.number().int().min(0).default(0),
  startDate: z.string().datetime().optional(),
  endDate: z.string().datetime().optional(),
  profile: z.string().optional()
});

export type HistoricalTelemetryQuery = z.infer<typeof HistoricalTelemetryQuerySchema>;

// Query parameters for alerts
export const DeviceAlertsQuerySchema = z.object({
  limit: z.coerce.number().int().min(1).max(200).default(50),
  offset: z.coerce.number().int().min(0).default(0),
  status: z.enum(['PENDING', 'SENT', 'RESOLVED', 'SUPPRESSED']).optional()
});

export type DeviceAlertsQuery = z.infer<typeof DeviceAlertsQuerySchema>;
