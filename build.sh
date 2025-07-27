#!/bin/bash

echo "Building Vercel Clone System..."

echo "Building Backend Service..."
cd backend
mvn clean package -DskipTests
if [ $? -ne 0 ]; then
    echo "Backend build failed!"
    exit 1
fi
cd ..

echo "Building Deployment Service..."
cd deployment
mvn clean package -DskipTests
if [ $? -ne 0 ]; then
    echo "Deployment service build failed!"
    exit 1
fi
cd ..

echo "Build completed successfully!"
echo "You can now run: docker-compose up -d" 