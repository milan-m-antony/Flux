import dotenv from 'dotenv';
import path from 'path';

// Load .env if present
dotenv.config({ path: path.resolve(process.cwd(), '.env') });
dotenv.config({ path: path.resolve(process.cwd(), '../.env') });

export interface AppConfig {
  nodeEnv: string;
  port: number;
  supabaseUrl: string;
  supabaseServiceRoleKey: string;
  firebaseProjectId?: string;
  firebaseClientEmail?: string;
  firebasePrivateKey?: string;
  firebaseCredentialsPath?: string;
  corsOrigin: string;
  rateLimitWindowMs: number;
  rateLimitMaxRequests: number;
}

export const config: AppConfig = {
  nodeEnv: process.env.NODE_ENV || 'development',
  port: parseInt(process.env.PORT || '10000', 10),
  supabaseUrl: process.env.SUPABASE_URL || '',
  supabaseServiceRoleKey: process.env.SUPABASE_SERVICE_ROLE_KEY || '',
  firebaseProjectId: process.env.FIREBASE_PROJECT_ID,
  firebaseClientEmail: process.env.FIREBASE_CLIENT_EMAIL,
  firebasePrivateKey: process.env.FIREBASE_PRIVATE_KEY?.replace(/\\n/g, '\n'),
  firebaseCredentialsPath: process.env.FIREBASE_CREDENTIALS_PATH,
  corsOrigin: process.env.CORS_ORIGIN || '*',
  rateLimitWindowMs: parseInt(process.env.RATE_LIMIT_WINDOW_MS || '60000', 10), // 1 minute
  rateLimitMaxRequests: parseInt(process.env.RATE_LIMIT_MAX_REQUESTS || '120', 10), // 120 req/min
};

export function validateRequiredConfig(): { valid: boolean; missing: string[] } {
  const missing: string[] = [];
  if (!config.supabaseUrl) missing.push('SUPABASE_URL');
  if (!config.supabaseServiceRoleKey) missing.push('SUPABASE_SERVICE_ROLE_KEY');

  const hasFirebaseCreds = 
    Boolean(config.firebaseProjectId && config.firebaseClientEmail && config.firebasePrivateKey) ||
    Boolean(config.firebaseCredentialsPath) ||
    Boolean(process.env.GOOGLE_APPLICATION_CREDENTIALS);

  if (!hasFirebaseCreds) {
    missing.push('FIREBASE_CREDENTIALS (FIREBASE_PROJECT_ID/CLIENT_EMAIL/PRIVATE_KEY or GOOGLE_APPLICATION_CREDENTIALS)');
  }

  return {
    valid: missing.length === 0,
    missing
  };
}
