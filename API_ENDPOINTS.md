# API Endpoints - cURL Collection

This document contains all API endpoints (except upload/deploy) with example cURL commands.

## Backend Service (Port 8080)

### 1. Check Deployment Status

Get the deployment status for a given session ID.

**Endpoint:** `GET /status`

**Query Parameters:**
- `id` (required): Session ID of the deployment

**Example:**
```bash
curl -X GET "http://localhost:8080/status?id=abc123xyz4567890" \
  -H "Content-Type: application/json"
```

**Response:**
- Returns status string: `"PENDING"`, `"DOWNLOADING"`, `"COMPILING"`, `"DEPLOYING"`, `"RUNNING"`, `"FAILED"`, `"STOPPED"`, or `"ERROR"`

---

## Deployment Service (Port 8081)

### 2. Get Detailed Deployment Status

Get comprehensive deployment status including container ID, port, JAR path, and timestamps.

**Endpoint:** `GET /api/deployment/status/{sessionId}`

**Path Parameters:**
- `sessionId` (required): Session ID of the deployment

**Example:**
```bash
curl -X GET "http://localhost:8081/api/deployment/status/abc123xyz4567890" \
  -H "Content-Type: application/json"
```

**Response:**
```json
{
  "sessionId": "abc123xyz4567890",
  "status": "RUNNING",
  "containerId": "a1b2c3d4e5f6",
  "port": 8080,
  "jarPath": "/tmp/deployments/abc123xyz4567890/target/app.jar",
  "errorMessage": null,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:35:00"
}
```

**Status Codes:**
- `200 OK`: Status found and returned
- `404 Not Found`: Session ID not found

---

### 3. Stop Deployment

Stop and remove a running deployment container.

**Endpoint:** `DELETE /api/deployment/halt/{sessionId}`

**Path Parameters:**
- `sessionId` (required): Session ID of the deployment to stop

**Example:**
```bash
curl -X DELETE "http://localhost:8081/api/deployment/halt/abc123xyz4567890" \
  -H "Content-Type: application/json"
```

**Response:**
```json
"Deployment stopped successfully"
```

**Status Codes:**
- `200 OK`: Deployment stopped successfully
- `500 Internal Server Error`: Failed to stop deployment

---

### 4. Restart Deployment

Restart a stopped deployment using the same JAR and port.

**Endpoint:** `POST /api/deployment/restart/{sessionId}`

**Path Parameters:**
- `sessionId` (required): Session ID of the deployment to restart

**Example:**
```bash
curl -X POST "http://localhost:8081/api/deployment/restart/abc123xyz4567890" \
  -H "Content-Type: application/json"
```

**Response:**
```json
"Deployment restarted successfully"
```

**Status Codes:**
- `200 OK`: Deployment restarted successfully
- `400 Bad Request`: Cannot restart (missing JAR path or port)
- `404 Not Found`: Session ID not found
- `500 Internal Server Error`: Failed to restart deployment

---

## Quick Reference

### All Endpoints Summary

| Method | Endpoint | Service | Description |
|--------|----------|---------|-------------|
| GET | `/status?id={id}` | Backend (8080) | Get simple status string |
| GET | `/api/deployment/status/{sessionId}` | Deployment (8081) | Get detailed deployment status |
| DELETE | `/api/deployment/halt/{sessionId}` | Deployment (8081) | Stop deployment |
| POST | `/api/deployment/restart/{sessionId}` | Deployment (8081) | Restart deployment |

### Status Values

- `PENDING`: Waiting in queue
- `DOWNLOADING`: Downloading from R2
- `COMPILING`: Maven compilation in progress
- `DEPLOYING`: Creating Docker container
- `RUNNING`: Container is active
- `FAILED`: Deployment failed
- `STOPPED`: Container stopped

---

## Testing with Environment Variables

You can set a session ID as an environment variable for easier testing:

```bash
export SESSION_ID="abc123xyz4567890"

# Then use in commands:
curl -X GET "http://localhost:8080/status?id=${SESSION_ID}"
curl -X GET "http://localhost:8081/api/deployment/status/${SESSION_ID}"
curl -X DELETE "http://localhost:8081/api/deployment/halt/${SESSION_ID}"
curl -X POST "http://localhost:8081/api/deployment/restart/${SESSION_ID}"
```

---

## Example Workflow

```bash
# 1. Check status (simple)
curl -X GET "http://localhost:8080/status?id=abc123xyz4567890"

# 2. Get detailed status
curl -X GET "http://localhost:8081/api/deployment/status/abc123xyz4567890"

# 3. Stop deployment
curl -X DELETE "http://localhost:8081/api/deployment/halt/abc123xyz4567890"

# 4. Restart deployment
curl -X POST "http://localhost:8081/api/deployment/restart/abc123xyz4567890"

# 5. Verify it's running again
curl -X GET "http://localhost:8081/api/deployment/status/abc123xyz4567890"
```


