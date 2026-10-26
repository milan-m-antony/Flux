import request from 'supertest';
import { createApp } from '../src/app';

const mockVerifyIdToken = jest.fn();

jest.mock('../src/config/firebase', () => ({
  getFirebaseAuth: () => ({
    verifyIdToken: mockVerifyIdToken
  })
}));

describe('Authentication Middleware', () => {
  const app = createApp();

  beforeEach(() => {
    mockVerifyIdToken.mockReset();
  });

  it('rejects requests with missing Authorization header (401)', async () => {
    const res = await request(app)
      .post('/api/v1/telemetry')
      .send({
        deviceId: 'Flux1',
        temperatureCelsius: 24.5
      });

    expect(res.status).toBe(401);
    expect(res.body.code).toBe('UNAUTHORIZED');
    expect(res.body.error).toContain('Missing or malformed Authorization header');
  });

  it('rejects requests with empty Bearer token (401)', async () => {
    const res = await request(app)
      .post('/api/v1/telemetry')
      .set('Authorization', 'Bearer ')
      .send({
        deviceId: 'Flux1',
        temperatureCelsius: 24.5
      });

    expect(res.status).toBe(401);
    expect(res.body.code).toBe('UNAUTHORIZED');
  });

  it('rejects requests with invalid Firebase token (401)', async () => {
    mockVerifyIdToken.mockRejectedValueOnce(new Error('Decoding error'));

    const res = await request(app)
      .post('/api/v1/telemetry')
      .set('Authorization', 'Bearer invalid-token-xyz')
      .send({
        deviceId: 'Flux1',
        temperatureCelsius: 24.5
      });

    expect(res.status).toBe(401);
    expect(res.body.code).toBe('INVALID_TOKEN');
  });

  it('returns specific TOKEN_EXPIRED code when Firebase token is expired (401)', async () => {
    const expiredError: any = new Error('Firebase ID token has expired.');
    expiredError.code = 'auth/id-token-expired';
    mockVerifyIdToken.mockRejectedValueOnce(expiredError);

    const res = await request(app)
      .post('/api/v1/telemetry')
      .set('Authorization', 'Bearer expired-token-123')
      .send({
        deviceId: 'Flux1',
        temperatureCelsius: 24.5
      });

    expect(res.status).toBe(401);
    expect(res.body.code).toBe('TOKEN_EXPIRED');
    expect(res.body.error).toContain('expired');
  });
});
