#!/bin/bash

echo "Testing Vercel Clone Deployment System..."

BACKEND_URL="http://localhost:8080"
DEPLOYMENT_URL="http://localhost:8081"

echo "1. Testing Backend Service..."
if curl -s "$BACKEND_URL/status?id=test" > /dev/null; then
    echo "✓ Backend service is running"
else
    echo "✗ Backend service is not responding"
    exit 1
fi

echo "2. Testing Deployment Service..."
if curl -s "$DEPLOYMENT_URL/api/deployment/status/test" > /dev/null; then
    echo "✓ Deployment service is running"
else
    echo "✗ Deployment service is not responding"
    exit 1
fi

echo "3. Testing Redis Connection..."
if docker exec h-vercel-redis-1 redis-cli ping | grep -q "PONG"; then
    echo "✓ Redis is running"
else
    echo "✗ Redis is not responding"
    exit 1
fi

echo "4. Testing MySQL Connection..."
if docker exec h-vercel-mysql-1 mysql -uroot -proot -e "SELECT 1;" > /dev/null 2>&1; then
    echo "✓ MySQL is running"
else
    echo "✗ MySQL is not responding"
    exit 1
fi

echo "All services are running correctly!"
echo "You can now deploy a repository using:"
echo "curl -X POST $BACKEND_URL/deploy -H 'Content-Type: application/json' -d '{\"repoUrl\":\"https://github.com/username/repo.git\"}'" 