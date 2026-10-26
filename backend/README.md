# Flux IoT Backend Service

Production-ready, Dockerized Node.js + TypeScript + Express backend for the **Flux Smart Telemetry System**. Designed for seamless deployment on [Render](https://render.com) and integration with Supabase PostgreSQL and Firebase Authentication.

---

## 1. System Architecture

```text
ESP32 (Flux1) 
   │  (BLE GATT Telemetry)
   ▼
Android App (Jetpack Compose)
   │  (Durable Room Queue + HTTPS Bearer Auth)
   ▼
Render Backend API (Node.js/Express in Docker)
   │  (Server-Side Supabase Client with service_role)
   ▼
Supabase PostgreSQL (Triggers & RLS)
   ├── `telemetry` (Time-series log)
   ├── `device_state` (Latest state snapshot)
   └── `alert_events` (Threshold alerts)
         │  (Database Webhook)
         ▼
Supabase Edge Function (`flux-alert-evaluator`)
         │  (OneSignal Push Notification)
         ▼
Subscribed Mobile Clients
```

---

## 2. API Endpoints

### Public Endpoints
| Method | Route | Description |
|---|---|---|
| `GET` | `/health` | Lightweight process health check for load balancers / Render health probe |
| `GET` | `/ready` | Deep readiness check: verifies environment variables and Supabase DB connection |

### Protected Endpoints (`Authorization: Bearer <Firebase_ID_Token>`)
| Method | Route | Description |
|---|---|---|
| `POST` | `/api/v1/telemetry` | Validates and ingests telemetry; enforces device ownership and idempotency |
| `GET` | `/api/v1/devices/:deviceId/telemetry` | Paginated historical telemetry points with optional time range and profile filters |
| `GET` | `/api/v1/devices/:deviceId/state` | Retrieves single latest state snapshot for instant UI rendering |
| `GET` | `/api/v1/devices/:deviceId/alerts` | Retrieves device alert history with optional status filter |
| `POST` | `/api/v1/devices/:deviceId/claim` | Binds an unowned or anonymous device to the authenticated user |

---

## 3. Sample Payloads

### `POST /api/v1/telemetry`
**Headers:**
```http
Authorization: Bearer <Firebase_ID_Token>
Content-Type: application/json
```
**Request Body:**
```json
{
  "deviceId": "Flux1",
  "temperatureCelsius": 27.81,
  "recordedAt": "2026-10-26T15:30:00.000Z",
  "activeProfile": "Live Monitor",
  "isAlert": false,
  "source": "BLE",
  "batteryPercentage": 98,
  "batteryVoltage": 4.18,
  "sensorConnected": true,
  "idempotencyKey": "4a73e6d8-2b81-42ec-8fa4-18751fa0ef2b"
}
```
**Response (`201 Created` or `200 OK` on duplicate replay):**
```json
{
  "success": true,
  "data": {
    "id": 1052,
    "deviceId": "Flux1",
    "temperatureCelsius": 27.81,
    "recordedAt": "2026-10-26T15:30:00.000Z",
    "activeProfile": "Live Monitor",
    "isAlert": false,
    "source": "BLE",
    "idempotentReplay": false
  },
  "message": "Telemetry ingested successfully",
  "timestamp": "2026-10-26T15:30:01.120Z"
}
```

---

## 4. Local Development & Testing

### Prerequisites
- Node.js 20+
- npm 10+

### Setup
```bash
cd backend
npm install
npm run build
npm test
```

### Running Locally
```bash
# Start in dev mode with live reload
npm run dev

# Or build and start production bundle
npm run build
npm start
```

---

## 5. Docker Build & Run

### Build Docker Image
```bash
docker build -t flux-backend:latest -f Dockerfile .
```

### Run Container
```bash
docker run -d \
  --name flux-backend \
  -p 10000:10000 \
  -e PORT=10000 \
  -e NODE_ENV=production \
  -e SUPABASE_URL="https://your-project.supabase.co" \
  -e SUPABASE_SERVICE_ROLE_KEY="your-service-role-key" \
  -e FIREBASE_PROJECT_ID="your-project-id" \
  -e FIREBASE_CLIENT_EMAIL="firebase-adminsdk@your-project.iam.gserviceaccount.com" \
  -e FIREBASE_PRIVATE_KEY="-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----\n" \
  flux-backend:latest
```

---

## 6. Render Deployment Guide

### Option A: Automatic Deployment via Render Blueprint (`render.yaml`)
1. Log in to [dashboard.render.com](https://dashboard.render.com).
2. Click **New +** $\rightarrow$ **Blueprint**.
3. Connect your repository: `https://github.com/milan-m-antony/Flux`.
4. Render will detect `render.yaml` and configure the web service automatically.
5. In the Render Dashboard, fill in the secret environment variables marked `sync: false`:
   - `SUPABASE_URL`
   - `SUPABASE_SERVICE_ROLE_KEY`
   - `FIREBASE_PROJECT_ID`
   - `FIREBASE_CLIENT_EMAIL`
   - `FIREBASE_PRIVATE_KEY`
6. Click **Apply**. Render will trigger the multi-stage Docker build, execute health checks against `/health`, and deploy the service.

### Option B: Manual Web Service Creation on Render
1. Click **New +** $\rightarrow$ **Web Service**.
2. Connect repository: `https://github.com/milan-m-antony/Flux`.
3. Choose **Docker** runtime:
   - Root Directory: `backend`
   - Dockerfile Path: `./Dockerfile`
   - Docker Context: `.`
4. Set Instance Type: **Free** (or Starter).
5. Health Check Path: `/health`.
6. Add Environment Variables (as listed above).
7. Deploy.
