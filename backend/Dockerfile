# ==============================================================================
# Flux — Smart Telemetry System: Production Dockerfile
# Multi-stage build for Node.js + TypeScript + Express backend
# Compatible with Render, Docker Compose, and Container Orchestrators
# ==============================================================================

# --- Stage 1: Build & Compile TypeScript ---
FROM node:22-alpine AS builder

WORKDIR /app

# Install build dependencies
COPY package*.json tsconfig.json ./
RUN npm ci

# Copy TypeScript source code
COPY src ./src

# Compile to JavaScript
RUN npm run build

# Remove development dependencies to keep production footprint minimal
RUN npm prune --production

# --- Stage 2: Minimal Production Image ---
FROM node:22-alpine AS runner

WORKDIR /app

# Set production environment
ENV NODE_ENV=production
ENV PORT=10000

# Copy built application and production dependencies from builder
COPY --from=builder /app/package.json ./package.json
COPY --from=builder /app/node_modules ./node_modules
COPY --from=builder /app/dist ./dist

# Security hardening: Execute as unprivileged node user
USER node

# Document default listening port
EXPOSE 10000

# Graceful shutdown handle and start
CMD ["node", "dist/server.js"]
