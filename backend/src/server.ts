import http from 'http';
import { createApp } from './app';
import { config, validateRequiredConfig } from './config/env';

const app = createApp();
const server = http.createServer(app);

// Configure production timeouts (Render recommendations)
server.keepAliveTimeout = 65000; // 65 seconds (must be higher than Render reverse-proxy keepalive of 60s)
server.headersTimeout = 66000;   // 66 seconds

const host = '0.0.0.0';
const port = config.port;

server.listen(port, host, () => {
  console.log(`[FluxBackend] Server running on http://${host}:${port} in ${config.nodeEnv} mode`);
  
  const { valid, missing } = validateRequiredConfig();
  if (!valid) {
    console.warn(`[FluxBackend] Warning: Missing required configuration keys: ${missing.join(', ')}`);
    console.warn('[FluxBackend] Some protected database and auth endpoints will fail until configured.');
  } else {
    console.log('[FluxBackend] All core environment configurations verified.');
  }
});

// Graceful shutdown handling
function gracefulShutdown(signal: string) {
  console.log(`[FluxBackend] ${signal} signal received: closing HTTP server gracefully...`);
  server.close(() => {
    console.log('[FluxBackend] HTTP server closed cleanly. Exiting process.');
    process.exit(0);
  });

  // Force shutdown after 10 seconds if hanging
  setTimeout(() => {
    console.error('[FluxBackend] Forceful shutdown triggered after timeout.');
    process.exit(1);
  }, 10000).unref();
}

process.on('SIGTERM', () => gracefulShutdown('SIGTERM'));
process.on('SIGINT', () => gracefulShutdown('SIGINT'));

process.on('unhandledRejection', (reason, promise) => {
  console.error('[FluxBackend] Unhandled Rejection at:', promise, 'reason:', reason);
});

process.on('uncaughtException', (err) => {
  console.error('[FluxBackend] Uncaught Exception:', err);
  gracefulShutdown('UNCAUGHT_EXCEPTION');
});
