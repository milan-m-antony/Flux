import * as admin from 'firebase-admin';
import fs from 'fs';
import { config } from './env';

let firebaseApp: admin.app.App | null = null;

export function getFirebaseApp(): admin.app.App {
  if (firebaseApp) {
    return firebaseApp;
  }

  if (admin.apps.length > 0 && admin.apps[0]) {
    firebaseApp = admin.apps[0];
    return firebaseApp;
  }

  try {
    if (config.firebaseCredentialsPath && fs.existsSync(config.firebaseCredentialsPath)) {
      const serviceAccount = JSON.parse(fs.readFileSync(config.firebaseCredentialsPath, 'utf8'));
      firebaseApp = admin.initializeApp({
        credential: admin.credential.cert(serviceAccount)
      });
    } else if (config.firebaseProjectId && config.firebaseClientEmail && config.firebasePrivateKey) {
      firebaseApp = admin.initializeApp({
        credential: admin.credential.cert({
          projectId: config.firebaseProjectId,
          clientEmail: config.firebaseClientEmail,
          privateKey: config.firebasePrivateKey
        })
      });
    } else {
      // Fallback to application default credentials
      firebaseApp = admin.initializeApp({
        credential: admin.credential.applicationDefault()
      });
    }
  } catch (err: any) {
    // If running in development or tests where credentials are provided via mock, do not crash immediately
    console.warn(`[FirebaseAdmin] Initialization warning: ${err?.message || err}`);
  }

  return firebaseApp || admin.initializeApp({ projectId: config.firebaseProjectId || 'flux-smart-monitor' });
}

export function getFirebaseAuth(): admin.auth.Auth {
  const app = getFirebaseApp();
  return admin.auth(app);
}
