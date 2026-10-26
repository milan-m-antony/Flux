import request from 'supertest';
import { createApp } from '../src/app';

const mockVerifyIdToken = jest.fn();
const mockSupabaseFrom = jest.fn();

jest.mock('../src/config/firebase', () => ({
  getFirebaseAuth: () => ({
    verifyIdToken: mockVerifyIdToken
  })
}));

jest.mock('../src/config/supabase', () => ({
  getSupabaseClient: () => ({
    from: mockSupabaseFrom
  })
}));

describe('Device Endpoints (/api/v1/devices)', () => {
  const app = createApp();
  const validToken = 'valid-firebase-jwt-token';
  const authenticatedUser = { uid: 'user_123', email: 'tester@flux.io' };

  beforeEach(() => {
    jest.clearAllMocks();
    mockVerifyIdToken.mockResolvedValue(authenticatedUser);
  });

  it('GET /:deviceId/telemetry returns paginated readings', async () => {
    mockSupabaseFrom.mockImplementation((table: string) => {
      if (table === 'devices') {
        return {
          select: () => ({
            eq: () => ({
              maybeSingle: jest.fn().mockResolvedValue({
                data: { id: 'Flux1', owner_id: 'user_123' },
                error: null
              })
            })
          })
        };
      }
      if (table === 'telemetry') {
        return {
          select: () => ({
            eq: () => ({
              order: () => ({
                range: jest.fn().mockResolvedValue({
                  data: [
                    {
                      id: 1,
                      device_id: 'Flux1',
                      temperature_celsius: 26.5,
                      active_profile: 'Live Monitor',
                      is_alert: false,
                      source: 'BLE',
                      recorded_at: '2026-10-26T15:00:00Z',
                      created_at: '2026-10-26T15:00:01Z'
                    }
                  ],
                  count: 1,
                  error: null
                })
              })
            })
          })
        };
      }
      return {};
    });

    const res = await request(app)
      .get('/api/v1/devices/Flux1/telemetry?limit=10&offset=0')
      .set('Authorization', `Bearer ${validToken}`);

    expect(res.status).toBe(200);
    expect(res.body.success).toBe(true);
    expect(res.body.data.length).toBe(1);
    expect(res.body.data[0].temperatureCelsius).toBe(26.5);
    expect(res.body.pagination.limit).toBe(10);
    expect(res.body.pagination.total).toBe(1);
  });

  it('GET /:deviceId/telemetry rejects invalid limit > 500 (400)', async () => {
    mockSupabaseFrom.mockImplementation((table: string) => {
      if (table === 'devices') {
        return {
          select: () => ({
            eq: () => ({
              maybeSingle: jest.fn().mockResolvedValue({
                data: { id: 'Flux1', owner_id: 'user_123' },
                error: null
              })
            })
          })
        };
      }
      return {};
    });

    const res = await request(app)
      .get('/api/v1/devices/Flux1/telemetry?limit=1000') // limit max is 500
      .set('Authorization', `Bearer ${validToken}`);

    expect(res.status).toBe(400);
    expect(res.body.code).toBe('VALIDATION_ERROR');
  });

  it('GET /:deviceId/state returns latest device state snapshot', async () => {
    mockSupabaseFrom.mockImplementation((table: string) => {
      if (table === 'devices') {
        return {
          select: () => ({
            eq: () => ({
              maybeSingle: jest.fn().mockResolvedValue({
                data: {
                  id: 'Flux1',
                  owner_id: 'user_123',
                  display_name: 'Flux1 Smart Monitor',
                  firmware_version: 'v2.1.0',
                  device_status: 'ONLINE',
                  last_seen: '2026-10-26T15:10:00Z'
                },
                error: null
              })
            })
          })
        };
      }
      if (table === 'device_state') {
        return {
          select: () => ({
            eq: () => ({
              maybeSingle: jest.fn().mockResolvedValue({
                data: {
                  device_id: 'Flux1',
                  latest_temperature_celsius: 27.81,
                  active_profile: 'Live Monitor',
                  preferred_unit: 'C',
                  battery_percentage: 98,
                  battery_voltage: 4.18,
                  charging: false,
                  sensor_connected: true,
                  is_alert: false,
                  status_text: 'Normal',
                  uptime_seconds: 120,
                  last_seen: '2026-10-26T15:10:00Z',
                  last_sync: '2026-10-26T15:10:01Z'
                },
                error: null
              })
            })
          })
        };
      }
      return {};
    });

    const res = await request(app)
      .get('/api/v1/devices/Flux1/state')
      .set('Authorization', `Bearer ${validToken}`);

    expect(res.status).toBe(200);
    expect(res.body.success).toBe(true);
    expect(res.body.data.deviceId).toBe('Flux1');
    expect(res.body.data.latestTemperatureCelsius).toBe(27.81);
    expect(res.body.data.batteryPercentage).toBe(98);
    expect(res.body.data.statusText).toBe('Normal');
  });

  it('GET /:deviceId/alerts returns device alert events', async () => {
    mockSupabaseFrom.mockImplementation((table: string) => {
      if (table === 'devices') {
        return {
          select: () => ({
            eq: () => ({
              maybeSingle: jest.fn().mockResolvedValue({
                data: { id: 'Flux1', owner_id: 'user_123' },
                error: null
              })
            })
          })
        };
      }
      if (table === 'alert_events') {
        return {
          select: () => ({
            eq: () => ({
              order: () => ({
                range: jest.fn().mockResolvedValue({
                  data: [
                    {
                      id: 42,
                      device_id: 'Flux1',
                      event_type: 'HIGH_TEMP',
                      temperature_celsius: 29.5,
                      configured_threshold: 28.0,
                      active_profile: 'Live Monitor',
                      processing_status: 'SENT',
                      onesignal_notification_id: 'os-alert-123',
                      created_at: '2026-10-26T15:05:00Z',
                      resolved_at: null
                    }
                  ],
                  count: 1,
                  error: null
                })
              })
            })
          })
        };
      }
      return {};
    });

    const res = await request(app)
      .get('/api/v1/devices/Flux1/alerts')
      .set('Authorization', `Bearer ${validToken}`);

    expect(res.status).toBe(200);
    expect(res.body.success).toBe(true);
    expect(res.body.data.length).toBe(1);
    expect(res.body.data[0].eventType).toBe('HIGH_TEMP');
    expect(res.body.data[0].temperatureCelsius).toBe(29.5);
    expect(res.body.data[0].processingStatus).toBe('SENT');
  });
});
