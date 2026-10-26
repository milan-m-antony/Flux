import request from 'supertest';
import { createApp } from '../src/app';

const mockCheckSupabaseHealth = jest.fn();
const mockValidateRequiredConfig = jest.fn();

jest.mock('../src/config/supabase', () => ({
  getSupabaseClient: jest.fn(),
  checkSupabaseHealth: () => mockCheckSupabaseHealth()
}));

jest.mock('../src/config/env', () => {
  const actual = jest.requireActual('../src/config/env');
  return {
    ...actual,
    validateRequiredConfig: () => mockValidateRequiredConfig()
  };
});

describe('Health and Readiness Endpoints', () => {
  const app = createApp();

  beforeEach(() => {
    mockCheckSupabaseHealth.mockReset();
    mockValidateRequiredConfig.mockReset();

    mockCheckSupabaseHealth.mockResolvedValue({ ok: true, latencyMs: 25 });
    mockValidateRequiredConfig.mockReturnValue({ valid: true, missing: [] });
  });

  it('GET /health returns 200 OK with lightweight process status', async () => {
    const res = await request(app).get('/health');
    expect(res.status).toBe(200);
    expect(res.body).toHaveProperty('status', 'ok');
    expect(res.body).toHaveProperty('service', 'flux-backend');
    expect(res.body).toHaveProperty('uptimeSeconds');
    expect(res.body).toHaveProperty('timestamp');
  });

  it('GET /ready returns 200 when database and configuration are healthy', async () => {
    const res = await request(app).get('/ready');
    expect(res.status).toBe(200);
    expect(res.body).toHaveProperty('status', 'ready');
    expect(res.body.checks.database.status).toBe('connected');
    expect(res.body.checks.configuration.status).toBe('ok');
  });

  it('GET /ready returns 503 when database is disconnected', async () => {
    mockCheckSupabaseHealth.mockResolvedValueOnce({ ok: false, error: 'Connection refused' });

    const res = await request(app).get('/ready');
    expect(res.status).toBe(503);
    expect(res.body).toHaveProperty('status', 'not_ready');
    expect(res.body.checks.database.status).toBe('disconnected');
    expect(res.body.checks.database.error).toBe('Connection refused');
  });

  it('GET /ready returns 503 when configuration is missing required variables', async () => {
    mockValidateRequiredConfig.mockReturnValueOnce({
      valid: false,
      missing: ['SUPABASE_SERVICE_ROLE_KEY']
    });

    const res = await request(app).get('/ready');
    expect(res.status).toBe(503);
    expect(res.body.status).toBe('not_ready');
    expect(res.body.checks.configuration.status).toBe('missing_variables');
    expect(res.body.checks.configuration.missing).toContain('SUPABASE_SERVICE_ROLE_KEY');
  });
});
