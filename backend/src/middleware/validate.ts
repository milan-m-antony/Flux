import { Request, Response, NextFunction } from 'express';
import { ZodSchema, ZodError } from 'zod';

type RequestLocation = 'body' | 'query' | 'params';

export function validateRequest(schema: ZodSchema<any>, location: RequestLocation = 'body') {
  return (req: Request, res: Response, next: NextFunction): void => {
    try {
      const target = req[location];
      const parsed = schema.parse(target);
      req[location] = parsed;
      next();
    } catch (err) {
      if (err instanceof ZodError) {
        const issues = err.errors.map((e) => ({
          field: e.path.join('.'),
          message: e.message
        }));

        res.status(400).json({
          success: false,
          error: 'Validation failed on request payload.',
          code: 'VALIDATION_ERROR',
          details: issues,
          timestamp: new Date().toISOString()
        });
        return;
      }

      next(err);
    }
  };
}
