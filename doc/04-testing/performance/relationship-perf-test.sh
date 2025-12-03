#!/bin/bash
# Resource Relationship Performance Test Script
# Task 18: Performance testing for 50000 relationships

set -e

BASE_URL="http://localhost:8080"
TOKEN="eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI0MyIsInVzZXJuYW1lIjoidGVzdHVzZXIwMDEiLCJyb2xlIjoiUk9MRV9VU0VSIiwiaWF0IjoxNzY0NzczODc0LCJleHAiOjE3NjQ3ODEwNzQsInNlc3Npb25JZCI6IjFjNmRiZWZiLTA0YjktNDRiOC1iMGI0LWYzNDI2N2I1ZDdkNyJ9._13AK92EBY3ZZct43kPG-AG7yQkf1s74JujRDs7KDh2f2psw-y5CsvK_rv2MTHDCe7nsWcNTtrp1MyfHw9mNiQ"

echo "=== Performance Test: Resource Relationships ==="
echo ""

# Test 1: Query Performance
echo "=== Test 1: List Relationships Query Performance ==="
start_time=$(date +%s%3N)
RESPONSE=$(curl -s -w "\n%{http_code}\n%{time_total}" \
  -H "Authorization: Bearer $TOKEN" \
  "$BASE_URL/api/v1/relationships?pageNum=1&pageSize=100")

http_code=$(echo "$RESPONSE" | tail -2 | head -1)
time_total=$(echo "$RESPONSE" | tail -1)
echo "HTTP Code: $http_code"
echo "Response Time: ${time_total}s"
if (( $(echo "$time_total < 0.5" | bc -l) )); then
  echo "✅ PASS: Response time < 500ms"
else
  echo "❌ FAIL: Response time >= 500ms"
fi
echo ""

# Test 2: Get Resource Relationships
echo "=== Test 2: Get Resource Relationships Performance ==="
# Get first resource ID from existing resources
RESOURCE_ID=$(curl -s -H "Authorization: Bearer $TOKEN" "$BASE_URL/api/v1/resources?pageNum=1&pageSize=1" | jq -r '.data.content[0].id // empty')

if [ -n "$RESOURCE_ID" ]; then
  RESPONSE=$(curl -s -w "\n%{time_total}" \
    -H "Authorization: Bearer $TOKEN" \
    "$BASE_URL/api/v1/relationships/resource/$RESOURCE_ID")
  time_total=$(echo "$RESPONSE" | tail -1)
  echo "Resource ID: $RESOURCE_ID"
  echo "Response Time: ${time_total}s"
  if (( $(echo "$time_total < 0.5" | bc -l) )); then
    echo "✅ PASS: Response time < 500ms"
  else
    echo "❌ FAIL: Response time >= 500ms"
  fi
else
  echo "No resources found for testing"
fi
echo ""

# Test 3: Cycle Detection Performance
echo "=== Test 3: Cycle Detection Performance ==="
if [ -n "$RESOURCE_ID" ]; then
  RESPONSE=$(curl -s -w "\n%{time_total}" \
    -H "Authorization: Bearer $TOKEN" \
    "$BASE_URL/api/v1/relationships/resource/$RESOURCE_ID/cycle-detection")
  time_total=$(echo "$RESPONSE" | tail -1)
  echo "Response Time: ${time_total}s"
  if (( $(echo "$time_total < 0.5" | bc -l) )); then
    echo "✅ PASS: Response time < 500ms"
  else
    echo "❌ FAIL: Response time >= 500ms"
  fi
fi
echo ""

# Test 4: Graph Traversal Performance
echo "=== Test 4: Graph Traversal Performance ==="
if [ -n "$RESOURCE_ID" ]; then
  RESPONSE=$(curl -s -w "\n%{time_total}" \
    -H "Authorization: Bearer $TOKEN" \
    "$BASE_URL/api/v1/relationships/resource/$RESOURCE_ID/traverse?maxDepth=10")
  time_total=$(echo "$RESPONSE" | tail -1)
  echo "Response Time: ${time_total}s"
  if (( $(echo "$time_total < 0.5" | bc -l) )); then
    echo "✅ PASS: Response time < 500ms"
  else
    echo "❌ FAIL: Response time >= 500ms"
  fi
fi
echo ""

# Test 5: Concurrent Request Test
echo "=== Test 5: Concurrent Request Test (10 requests) ==="
if [ -n "$RESOURCE_ID" ]; then
  start_time=$(date +%s%3N)
  for i in {1..10}; do
    curl -s -H "Authorization: Bearer $TOKEN" \
      "$BASE_URL/api/v1/relationships/resource/$RESOURCE_ID" > /dev/null &
  done
  wait
  end_time=$(date +%s%3N)
  total_time=$((end_time - start_time))
  avg_time=$((total_time / 10))
  echo "Total time for 10 concurrent requests: ${total_time}ms"
  echo "Average response time: ${avg_time}ms"
  if [ $avg_time -lt 500 ]; then
    echo "✅ PASS: Average response time < 500ms"
  else
    echo "❌ FAIL: Average response time >= 500ms"
  fi
fi

echo ""
echo "=== Performance Test Completed ==="
