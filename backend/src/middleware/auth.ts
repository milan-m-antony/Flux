import { Request, Response, NextFunction } from 'express';
import { getFirebaseAuth } from '../config/firebase';

export interface AuthenticatedUser {
  uid: string;
  email?: string;
  name?: string;
}

declare global {
  namespace Express {
    interface Request {
      user?: AuthenticatedUser;
    }
  }
}

export async function authenticateFirebaseToken(req: Request, res: Response, next: NextFunction): Promise<void> {
  const authHeader = req.headers.authorization;

  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    res.status(401).json({
      success: false,
      error: 'Missing or malformed Authorization header. Expected Bearer token.',
      code: 'UNAUTHORIZED',
      timestamp: new Date().toISOString()
    });
    return;
  }

  const idToken = authHeader.split('Bearer ')[1]?.trim();
  if (!idToken) {
    res.status(401).json({
      success: false,
      error: 'Empty Bearer token provided.',
      code: 'UNAUTHORIZED',
      timestamp: new Date().toISOString()
    });
    return;
  }

  try {
    const auth = getFirebaseAuth();
    const decodedToken = await auth.verifyIdToken(idToken);
    
    req.user = {
      uid: decodedToken.uid,
      email: decodedToken.email,
      name: decodedToken.name
    };

    next();
  } catch (err: any) {
    const errorCode = err?.code || 'AUTH_ERROR';
    const isExpired = errorCode === 'auth/id-token-expired';

    res.status(401).json({
      success: false,
      error: isExpired ? 'Firebase ID token has expired. Please refresh token.' : 'Invalid or unverifiable Firebase ID token.',
      code: isExpired ? 'TOKEN_EXPIRED' : 'INVALID_TOKEN',
      timestamp: new Date().toISOString()
    });
  }
}
