# Vercel Clone - Java Deployment System

A Vercel-like deployment system built with Java Spring Boot that clones repositories, stores them in R2 Cloudflare, compiles them as JAR files, and runs them in Docker containers.

## Architecture

- **Backend Service** (Port 8080): Handles repository cloning and uploads to R2
- **Deployment Service** (Port 8081): Processes deployment queue, compiles projects, and manages Docker containers
- **Redis**: Message queue and status storage
- **MySQL**: Database for application data
- **R2 Cloudflare**: Object storage for project files

## Prerequisites

- Docker and Docker Compose
- Maven
- Java 21
- R2 Cloudflare account with access keys

## Setup

1. **Configure Environment Variables**

Create a `.env` file in the root directory:

```env
ACCESS_KEY=your_r2_access_key
ACCOUNT_ID=your_r2_account_id
SECRET_KEY=your_r2_secret_key
```

2. **Build the Applications**

```bash
# Build backend
cd backend
mvn clean package

# Build deployment service
cd ../deployment
mvn clean package
```

3. **Start the System**

```bash
docker-compose up -d
```

## API Endpoints

### Backend Service (Port 8080)

- `POST /deploy` - Deploy a repository
  ```json
  {
    "repoUrl": "https://github.com/username/repo.git"
  }
  ```

- `GET /status?id={sessionId}` - Check deployment status

### Deployment Service (Port 8081)

- `GET /api/deployment/status/{sessionId}` - Get detailed deployment status
- `DELETE /api/deployment/{sessionId}` - Stop deployment
- `POST /api/deployment/{sessionId}/restart` - Restart deployment

## Deployment Flow

1. **Clone**: Repository is cloned and uploaded to R2
2. **Queue**: Session ID is added to Redis queue
3. **Download**: Deployment service downloads project from R2
4. **Compile**: Maven compiles the project into a JAR
5. **Deploy**: Docker container is created and JAR is executed
6. **Monitor**: Status is tracked in Redis

## Status Types

- `PENDING`: Waiting in queue
- `DOWNLOADING`: Downloading from R2
- `COMPILING`: Maven compilation
- `DEPLOYING`: Creating Docker container
- `RUNNING`: Container is running
- `FAILED`: Deployment failed
- `STOPPED`: Container stopped

## Features

- Automatic port allocation for containers
- Status tracking and monitoring
- Container lifecycle management
- Error handling and cleanup
- RESTful API for deployment control

## Troubleshooting

1. **Check logs**: `docker-compose logs -f [service-name]`
2. **Verify Redis connection**: Ensure Redis is running on localhost:6379
3. **Check Docker**: Ensure Docker daemon is running
4. **Verify R2 credentials**: Check access keys in application.properties

## Development

To run in development mode:

```bash
# Start dependencies
docker-compose up redis mysql -d

# Run backend
cd backend
mvn spring-boot:run

# Run deployment service
cd ../deployment
mvn spring-boot:run
``` 