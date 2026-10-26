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

describe('Telemetry Ingestion Endpoint (POST /api/v1/telemetry)', () => {
  const app = createApp();

  const validToken = 'valid-firebase-jwt-token';
  const authenticatedUser = { uid: 'user_123', email: 'tester@flux.io' };

  beforeEach(() => {
    jest.clearAllMocks();
    mockVerifyIdToken.mockResolvedValue(authenticatedUser);
  });

  it('rejects invalid temperature below hardware limits (400)', async () => {
    const res = await request(app)
      .post('/api/v1/telemetry')
      .set('Authorization', `Bearer ${validToken}`)
      .send({
        deviceId: 'Flux1',
        temperatureCelsius: -60.0 // Below -55°C limit
      });

    expect(res.status).toBe(400);
    expect(res.body.code).toBe('VALIDATION_ERROR');
    expect(res.body.details[0].message).toContain('below hardware limit');
  });

  it('rejects invalid temperature above hardware limits (400)', async () => {
    const res = await request(app)
      .post('/api/v1/telemetry')
      .set('Authorization', `Bearer ${validToken}`)
      .send({
        deviceId: 'Flux1',
        temperatureCelsius: 130.0 // Above +125°C limit
      });

    expect(res.status).toBe(400);
    expect(res.body.code).toBe('VALIDATION_ERROR');
    expect(res.body.details[0].message).toContain('above hardware limit');
  });

  it('rejects missing deviceId (400)', async () => {
    const res = await request(app)
      .post('/api/v1/telemetry')
      .set('Authorization', `Bearer ${validToken}`)
      .send({
        temperatureCelsius: 25.5
      });

    expect(res.status).toBe(400);
    expect(res.body.code).toBe('VALIDATION_ERROR');
  });

  it('rejects unknown activeProfile (400)', async () => {
    const res = await request(app)
      .post('/api/v1/telemetry')
      .set('Authorization', `Bearer ${validToken}`)
      .send({
        deviceId: 'Flux1',
        temperatureCelsius: 25.5,
        activeProfile: 'Industrial Reactor' // Unknown profile
      });

    expect(res.status).toBe(400);
    expect(res.body.code).toBe('VALIDATION_ERROR');
  });

  it('rejects future timestamps more than 1 hour in advance (400)', async () => {
    const futureTime = new Date(Date.now() + 2 * 3600 * 1000).toISOString();

    const res = await request(app)
      .post('/api/v1/telemetry')
      .set('Authorization', `Bearer ${validToken}`)
      .send({
        deviceId: 'Flux1',
        temperatureCelsius: 25.5,
        recordedAt: futureTime
      });

    expect(res.status).toBe(400);
    expect(res.body.code).toBe('VALIDATION_ERROR');
  });

  it('denies write access when device is owned by a different user (403)', async () => {
    // Mock ownership check: device belongs to someone else
    mockSupabaseFrom.mockImplementation((table: string) => {
      if (table === 'devices') {
        return {
          select: () => ({
            eq: () => ({
              maybeSingle: jest.fn().mockResolvedValue({
                data: { id: 'Flux1', owner_id: 'other_user_456' },
                error: null
              })
            })
          })
        };
      }
      return {};
    });

    const res = await request(app)
      .post('/api/v1/telemetry')
      .set('Authorization', `Bearer ${validToken}`)
      .send({
        deviceId: 'Flux1',
        temperatureCelsius: 25.5
      });

    expect(res.status).toBe(403);
    expect(res.body.code).toBe('FORBIDDEN');
    expect(res.body.error).toContain('Access denied');
  });

  it('successfully ingests valid telemetry and returns 201 Created', async () => {
    const recordedTimestamp = new Date(Date.now() - 5000).toISOString();

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
              eq: () => ({
                maybeSingle: jest.fn().mockResolvedValue({ data: null, error: null })
              })
            })
          }),
          insert: () => ({
            select: () => ({
              single: jest.fn().mockResolvedValue({
                data: {
                  id: 101,
                  device_id: 'Flux1',
                  temperature_celsius: 27.8,
                  active_profile: 'Live Monitor',
                  is_alert: false,
                  source: 'BLE',
                  recorded_at: recordedTimestamp,
                  created_at: new Date().toISOString()
                },
                error: null
              })
            })
          })
        };
      }
      if (table === 'device_state') {
        return {
          update: () => ({
            eq: jest.fn().mockResolvedValue({ error: null })
          })
        };
      }
      return {};
    });

    const res = await request(app)
      .post('/api/v1/telemetry')
      .set('Authorization', `Bearer ${validToken}`)
      .send({
        deviceId: 'Flux1',
        temperatureCelsius: 27.8,
        activeProfile: 'Live Monitor',
        isAlert: false,
        source: 'BLE',
        recordedAt: recordedTimestamp,
        batteryPercentage: 95,
        batteryVoltage: 4.15
      });

    expect(res.status).toBe(201);
    expect(res.body.success).toBe(true);
    expect(res.body.data.id).toBe(101);
    expect(res.body.data.deviceId).toBe('Flux1');
    expect(res.body.data.temperatureCelsius).toBe(27.8);
    expect(res.body.data.idempotentReplay).toBe(false);
  });

  it('handles idempotent replay cleanly without creating duplicate records (200)', async () => {
    const recordedTimestamp = new Date(Date.now() - 3000).toISOString();

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
              eq: () => ({
                // Simulating exact duplicate record found
                maybeSingle: jest.fn().mockResolvedValue({
                  data: { id: 202, temperature_celsius: 27.8, recorded_at: recordedTimestamp },
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
      .post('/api/v1/telemetry')
      .set('Authorization', `Bearer ${validToken}`)
      .send({
        deviceId: 'Flux1',
        temperatureCelsius: 27.8,
        recordedAt: recordedTimestamp,
        idempotencyKey: 'idemp-key-xyz-123'
      });

    expect(res.status).toBe(200);
    expect(res.body.success).toBe(true);
    expect(res.body.data.id).toBe(202);
    expect(res.body.data.idempotentReplay).toBe(true);
  });
});
