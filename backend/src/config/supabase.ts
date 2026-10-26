import { createClient, SupabaseClient } from '@supabase/supabase-js';
import { config } from './env';

let supabaseInstance: SupabaseClient | null = null;

export function getSupabaseClient(): SupabaseClient {
  if (supabaseInstance) {
    return supabaseInstance;
  }

  if (!config.supabaseUrl || !config.supabaseServiceRoleKey) {
    throw new Error('Supabase client cannot be initialized: SUPABASE_URL or SUPABASE_SERVICE_ROLE_KEY missing.');
  }

  supabaseInstance = createClient(config.supabaseUrl, config.supabaseServiceRoleKey, {
    auth: {
      persistSession: false,
      autoRefreshToken: false,
    },
  });

  return supabaseInstance;
}

export async function checkSupabaseHealth(): Promise<{ ok: boolean; latencyMs?: number; error?: string }> {
  const start = Date.now();
  try {
    const client = getSupabaseClient();
    const { error } = await client.from('devices').select('id').limit(1);
    if (error) {
      return { ok: false, error: error.message };
    }
    return { ok: true, latencyMs: Date.now() - start };
  } catch (err: any) {
    return { ok: false, error: err?.message || 'Failed to connect to Supabase' };
  }
}
