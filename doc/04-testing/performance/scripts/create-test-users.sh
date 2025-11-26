#!/bin/bash
# 创建性能测试用户脚本
#
# 用法：./create-test-users.sh [base_url] [user_count]
#
# 参数：
#   base_url   - API 基础 URL（默认：http://localhost:8080）
#   user_count - 创建的用户数量（默认：50）
#
# 示例：
#   ./create-test-users.sh
#   ./create-test-users.sh http://localhost:8080 100

set -e

BASE_URL="${1:-http://localhost:8080}"
USER_COUNT="${2:-50}"

echo "========================================"
echo "创建性能测试用户"
echo "========================================"
echo "API URL: $BASE_URL"
echo "用户数量: $USER_COUNT"
echo "========================================"

# 检查应用是否可用
echo "检查应用健康状态..."
HEALTH=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/actuator/health")
if [ "$HEALTH" != "200" ]; then
    echo "错误：应用不可用 (HTTP $HEALTH)"
    exit 1
fi
echo "应用健康状态：OK"

# 创建性能测试用户
echo ""
echo "开始创建用户..."
SUCCESS_COUNT=0
FAIL_COUNT=0

for i in $(seq -w 1 $USER_COUNT); do
    USERNAME="perfuser$i"
    EMAIL="perfuser$i@test.com"

    RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/auth/register" \
        -H "Content-Type: application/json" \
        -d "{
            \"username\": \"$USERNAME\",
            \"email\": \"$EMAIL\",
            \"password\": \"SecureP@ss123\"
        }")

    # 检查是否成功（201 Created 或已存在）
    if echo "$RESPONSE" | grep -q '"success":true\|用户名已存在'; then
        SUCCESS_COUNT=$((SUCCESS_COUNT + 1))
        echo "✓ 用户 $USERNAME 创建成功"
    else
        FAIL_COUNT=$((FAIL_COUNT + 1))
        echo "✗ 用户 $USERNAME 创建失败: $RESPONSE"
    fi
done

# 创建 BCrypt 测试用户
echo ""
echo "创建 BCrypt 测试用户..."
RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/auth/register" \
    -H "Content-Type: application/json" \
    -d '{
        "username": "bcrypttest",
        "email": "bcrypttest@test.com",
        "password": "SecureP@ss123"
    }')

if echo "$RESPONSE" | grep -q '"success":true\|用户名已存在'; then
    echo "✓ BCrypt 测试用户创建成功"
else
    echo "✗ BCrypt 测试用户创建失败: $RESPONSE"
fi

# 创建会话验证测试用户
echo ""
echo "创建会话验证测试用户..."
for i in $(seq 1 10); do
    USERNAME="perftest_session_$i"
    RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/auth/register" \
        -H "Content-Type: application/json" \
        -d "{
            \"username\": \"$USERNAME\",
            \"email\": \"$USERNAME@test.com\",
            \"password\": \"SecureP@ss123\"
        }")

    if echo "$RESPONSE" | grep -q '"success":true\|用户名已存在'; then
        echo "✓ 会话测试用户 $USERNAME 创建成功"
    fi
done

# 汇总
echo ""
echo "========================================"
echo "创建完成"
echo "========================================"
echo "成功: $SUCCESS_COUNT"
echo "失败: $FAIL_COUNT"
echo "========================================"

# 验证用户可以登录
echo ""
echo "验证用户登录..."
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/auth/login" \
    -H "Content-Type: application/json" \
    -d '{
        "identifier": "perfuser001",
        "password": "SecureP@ss123",
        "rememberMe": false
    }')

if echo "$LOGIN_RESPONSE" | grep -q '"success":true'; then
    echo "✓ 用户登录验证成功"
    TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
    echo "  Token: ${TOKEN:0:50}..."
else
    echo "✗ 用户登录验证失败: $LOGIN_RESPONSE"
    exit 1
fi

echo ""
echo "性能测试用户创建完成！"
echo "现在可以运行性能测试了。"
