#!/bin/bash

# ============================================
# Vercel Clone API - cURL Collection
# ============================================
# This collection contains all API endpoints except the upload/deploy endpoint
# 
# Backend Service: http://localhost:8080
# Deployment Service: http://localhost:8081
# ============================================

# Replace with your actual session ID
SESSION_ID="your-session-id-here"

echo "============================================"
echo "Vercel Clone API - cURL Collection"
echo "============================================"
echo ""

# ============================================
# BACKEND SERVICE (Port 8080)
# ============================================

echo "1. Check Deployment Status (Backend Service)"
echo "--------------------------------------------"
echo "GET http://localhost:8080/status?id={sessionId}"
echo ""
echo "curl -X GET \"http://localhost:8080/status?id=${SESSION_ID}\""
echo ""
curl -X GET "http://localhost:8080/status?id=${SESSION_ID}"
echo ""
echo ""

# ============================================
# DEPLOYMENT SERVICE (Port 8081)
# ============================================

echo "2. Get Detailed Deployment Status"
echo "--------------------------------------------"
echo "GET http://localhost:8081/api/deployment/status/{sessionId}"
echo ""
echo "curl -X GET \"http://localhost:8081/api/deployment/status/${SESSION_ID}\" -H \"Content-Type: application/json\""
echo ""
curl -X GET "http://localhost:8081/api/deployment/status/${SESSION_ID}" \
  -H "Content-Type: application/json"
echo ""
echo ""

echo "3. Stop Deployment"
echo "--------------------------------------------"
echo "DELETE http://localhost:8081/api/deployment/halt/{sessionId}"
echo ""
echo "curl -X DELETE \"http://localhost:8081/api/deployment/halt/${SESSION_ID}\" -H \"Content-Type: application/json\""
echo ""
curl -X DELETE "http://localhost:8081/api/deployment/halt/${SESSION_ID}" \
  -H "Content-Type: application/json"
echo ""
echo ""

echo "4. Restart Deployment"
echo "--------------------------------------------"
echo "POST http://localhost:8081/api/deployment/restart/{sessionId}"
echo ""
echo "curl -X POST \"http://localhost:8081/api/deployment/restart/${SESSION_ID}\" -H \"Content-Type: application/json\""
echo ""
curl -X POST "http://localhost:8081/api/deployment/restart/${SESSION_ID}" \
  -H "Content-Type: application/json"
echo ""
echo ""

echo "============================================"
echo "Collection Complete"
echo "============================================"


