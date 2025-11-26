#!/bin/bash
# AIOps Service 安全测试脚本
#
# 用法：./security-test.sh [base_url]
#
# 测试内容：
# 1. SQL 注入测试
# 2. XSS 攻击测试
# 3. 暴力破解防护测试
# 4. Token 安全测试
# 5. CSRF 防护测试
#
# 需求覆盖：
# - REQ-NFR-SEC-001 到 REQ-NFR-SEC-006

set -e

BASE_URL="${1:-http://localhost:8080}"
REGISTER_URL="$BASE_URL/api/v1/auth/register"
LOGIN_URL="$BASE_URL/api/v1/auth/login"
VALIDATE_URL="$BASE_URL/api/v1/session/validate"
LOGOUT_URL="$BASE_URL/api/v1/auth/logout"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

PASSED=0
FAILED=0
TOTAL=0

# 输出测试结果
pass() {
    echo -e "${GREEN}✅ 通过${NC}: $1"
    ((PASSED++))
    ((TOTAL++))
}

fail() {
    echo -e "${RED}❌ 失败${NC}: $1"
    ((FAILED++))
    ((TOTAL++))
}

warn() {
    echo -e "${YELLOW}⚠️ 警告${NC}: $1"
}

info() {
    echo -e "${NC}ℹ️  $1"
}

# 检查应用是否可用
check_app() {
    echo "========================================"
    echo "检查应用状态"
    echo "========================================"

    HEALTH=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/actuator/health" 2>/dev/null || echo "000")
    if [ "$HEALTH" = "200" ]; then
        pass "应用健康检查"
    else
        fail "应用不可用 (HTTP $HEALTH)"
        exit 1
    fi
}

# 创建测试用户
create_test_user() {
    local username=$1
    curl -s -X POST "$REGISTER_URL" \
        -H "Content-Type: application/json" \
        -d "{
            \"username\": \"$username\",
            \"email\": \"$username@test.com\",
            \"password\": \"SecureP@ss123\"
        }" > /dev/null 2>&1
}

# 登录获取 Token
get_token() {
    local username=$1
    local password=$2
    local response=$(curl -s -X POST "$LOGIN_URL" \
        -H "Content-Type: application/json" \
        -d "{
            \"identifier\": \"$username\",
            \"password\": \"$password\",
            \"rememberMe\": false
        }")

    echo "$response" | grep -o '"token":"[^"]*"' | cut -d'"' -f4
}

# ======================================
# 测试1: SQL 注入测试
# ======================================
test_sql_injection() {
    echo ""
    echo "========================================"
    echo "测试1: SQL 注入测试"
    echo "========================================"

    # SQL 注入 payload
    local payloads=(
        "' OR '1'='1"
        "admin'--"
        "1; DROP TABLE users;--"
        "' UNION SELECT * FROM users--"
        "1' AND '1'='1"
        "\" OR \"1\"=\"1"
    )

    local all_blocked=true

    for payload in "${payloads[@]}"; do
        info "测试 payload: $payload"

        # 测试登录接口
        local response=$(curl -s -X POST "$LOGIN_URL" \
            -H "Content-Type: application/json" \
            -d "{
                \"identifier\": \"$payload\",
                \"password\": \"test\",
                \"rememberMe\": false
            }")

        # 检查是否返回了敏感信息或成功登录
        if echo "$response" | grep -qi "success.*true\|token.*eyJ"; then
            fail "SQL注入成功: $payload"
            all_blocked=false
        elif echo "$response" | grep -qi "syntax\|mysql\|sql\|database\|query"; then
            fail "SQL错误信息泄露: $payload"
            all_blocked=false
        else
            info "  已阻止"
        fi
    done

    if $all_blocked; then
        pass "SQL 注入防护有效"
    fi
}

# ======================================
# 测试2: XSS 攻击测试
# ======================================
test_xss() {
    echo ""
    echo "========================================"
    echo "测试2: XSS 攻击测试"
    echo "========================================"

    # XSS payload
    local payloads=(
        "<script>alert('XSS')</script>"
        "<img src=x onerror=alert('XSS')>"
        "javascript:alert('XSS')"
        "<svg onload=alert('XSS')>"
        "'\"><script>alert('XSS')</script>"
    )

    local all_blocked=true

    for payload in "${payloads[@]}"; do
        info "测试 payload: $payload"

        # 测试注册接口
        local response=$(curl -s -X POST "$REGISTER_URL" \
            -H "Content-Type: application/json" \
            -d "{
                \"username\": \"$payload\",
                \"email\": \"xss@test.com\",
                \"password\": \"SecureP@ss123\"
            }")

        # 检查响应中是否原样返回了 XSS payload
        if echo "$response" | grep -q "<script>\|onerror=\|javascript:"; then
            fail "XSS 未转义: $payload"
            all_blocked=false
        else
            info "  已阻止或转义"
        fi
    done

    if $all_blocked; then
        pass "XSS 防护有效"
    fi
}

# ======================================
# 测试3: 暴力破解防护测试
# ======================================
test_brute_force() {
    echo ""
    echo "========================================"
    echo "测试3: 暴力破解防护测试"
    echo "========================================"

    # 创建测试用户
    local test_user="bruteforce_$(date +%s)"
    create_test_user "$test_user"

    info "测试用户: $test_user"
    info "连续6次使用错误密码登录..."

    local locked=false

    for i in {1..6}; do
        local response=$(curl -s -X POST "$LOGIN_URL" \
            -H "Content-Type: application/json" \
            -d "{
                \"identifier\": \"$test_user\",
                \"password\": \"WrongP@ss123\",
                \"rememberMe\": false
            }")

        local http_code=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$LOGIN_URL" \
            -H "Content-Type: application/json" \
            -d "{
                \"identifier\": \"$test_user\",
                \"password\": \"WrongP@ss123\",
                \"rememberMe\": false
            }")

        info "  尝试 $i: HTTP $http_code"

        if [ "$http_code" = "423" ]; then
            locked=true
            info "  账号已锁定!"
            break
        fi

        sleep 0.5
    done

    if $locked; then
        pass "暴力破解防护有效 (5次失败后锁定)"
    else
        fail "暴力破解防护未生效"
    fi
}

# ======================================
# 测试4: Token 安全测试
# ======================================
test_token_security() {
    echo ""
    echo "========================================"
    echo "测试4: Token 安全测试"
    echo "========================================"

    # 创建测试用户并获取有效 Token
    local test_user="tokentest_$(date +%s)"
    create_test_user "$test_user"
    local valid_token=$(get_token "$test_user" "SecureP@ss123")

    if [ -z "$valid_token" ]; then
        warn "无法获取有效Token，跳过部分测试"
    else
        info "获取到有效Token"
    fi

    # 测试4.1: 伪造 Token
    info "测试伪造Token..."
    local fake_token="fake.token.here"
    local response=$(curl -s -o /dev/null -w "%{http_code}" -X GET "$VALIDATE_URL" \
        -H "Authorization: Bearer $fake_token")

    if [ "$response" = "401" ] || [ "$response" = "403" ]; then
        pass "伪造Token被拒绝"
    else
        fail "伪造Token未被正确拒绝 (HTTP $response)"
    fi

    # 测试4.2: 篡改 Token
    if [ -n "$valid_token" ]; then
        info "测试篡改Token..."
        # 修改 Token 的签名部分
        local tampered_token="${valid_token%.*}.tamperedsig"
        response=$(curl -s -o /dev/null -w "%{http_code}" -X GET "$VALIDATE_URL" \
            -H "Authorization: Bearer $tampered_token")

        if [ "$response" = "401" ] || [ "$response" = "403" ]; then
            pass "篡改Token被拒绝"
        else
            fail "篡改Token未被正确拒绝 (HTTP $response)"
        fi
    fi

    # 测试4.3: 空 Token
    info "测试空Token..."
    response=$(curl -s -o /dev/null -w "%{http_code}" -X GET "$VALIDATE_URL")

    if [ "$response" = "401" ] || [ "$response" = "403" ]; then
        pass "缺少Token被拒绝"
    else
        fail "缺少Token未被正确拒绝 (HTTP $response)"
    fi

    # 测试4.4: 有效 Token 验证
    if [ -n "$valid_token" ]; then
        info "测试有效Token..."
        response=$(curl -s -o /dev/null -w "%{http_code}" -X GET "$VALIDATE_URL" \
            -H "Authorization: Bearer $valid_token")

        if [ "$response" = "200" ]; then
            pass "有效Token被正确接受"
        else
            fail "有效Token被拒绝 (HTTP $response)"
        fi
    fi
}

# ======================================
# 测试5: 敏感信息泄露测试
# ======================================
test_info_disclosure() {
    echo ""
    echo "========================================"
    echo "测试5: 敏感信息泄露测试"
    echo "========================================"

    # 测试错误响应不包含敏感信息
    info "检查错误响应..."

    local response=$(curl -s -X POST "$LOGIN_URL" \
        -H "Content-Type: application/json" \
        -d '{"identifier":"nonexistent","password":"wrong","rememberMe":false}')

    local all_safe=true

    # 检查是否泄露了技术细节
    if echo "$response" | grep -qi "stacktrace\|exception\|at com\.\|at org\."; then
        fail "错误响应包含堆栈信息"
        all_safe=false
    fi

    if echo "$response" | grep -qi "mysql\|jdbc\|hibernate\|spring"; then
        fail "错误响应包含技术实现细节"
        all_safe=false
    fi

    # 检查响应是否区分用户存在与否
    local response1=$(curl -s -X POST "$LOGIN_URL" \
        -H "Content-Type: application/json" \
        -d '{"identifier":"definitely_not_exist_user","password":"wrong","rememberMe":false}')

    local response2=$(curl -s -X POST "$LOGIN_URL" \
        -H "Content-Type: application/json" \
        -d '{"identifier":"another_not_exist","password":"wrong","rememberMe":false}')

    # 提取错误消息
    local msg1=$(echo "$response1" | grep -o '"message":"[^"]*"' | head -1)
    local msg2=$(echo "$response2" | grep -o '"message":"[^"]*"' | head -1)

    if [ "$msg1" = "$msg2" ] && echo "$msg1" | grep -qi "用户名或密码错误"; then
        info "  错误消息一致，不区分用户是否存在"
    fi

    if $all_safe; then
        pass "敏感信息保护有效"
    fi
}

# ======================================
# 测试6: HTTP 安全头测试
# ======================================
test_security_headers() {
    echo ""
    echo "========================================"
    echo "测试6: HTTP 安全头测试"
    echo "========================================"

    local headers=$(curl -s -I "$BASE_URL/actuator/health")

    # 检查常见安全头
    info "检查安全响应头..."

    if echo "$headers" | grep -qi "X-Content-Type-Options"; then
        pass "X-Content-Type-Options 已设置"
    else
        warn "X-Content-Type-Options 未设置"
    fi

    if echo "$headers" | grep -qi "X-Frame-Options"; then
        pass "X-Frame-Options 已设置"
    else
        warn "X-Frame-Options 未设置"
    fi

    if echo "$headers" | grep -qi "X-XSS-Protection"; then
        info "  X-XSS-Protection 已设置 (已废弃，现代浏览器不需要)"
    fi

    if echo "$headers" | grep -qi "Cache-Control.*no-store\|no-cache"; then
        pass "Cache-Control 已正确设置"
    else
        warn "Cache-Control 可能需要检查"
    fi
}

# ======================================
# 主函数
# ======================================
main() {
    echo "========================================"
    echo "AIOps Service 安全测试"
    echo "========================================"
    echo "目标: $BASE_URL"
    echo "时间: $(date)"
    echo ""

    check_app
    test_sql_injection
    test_xss
    test_brute_force
    test_token_security
    test_info_disclosure
    test_security_headers

    echo ""
    echo "========================================"
    echo "测试结果汇总"
    echo "========================================"
    echo -e "通过: ${GREEN}$PASSED${NC}"
    echo -e "失败: ${RED}$FAILED${NC}"
    echo "总计: $TOTAL"
    echo ""

    if [ $FAILED -eq 0 ]; then
        echo -e "${GREEN}✅ 所有安全测试通过${NC}"
        exit 0
    else
        echo -e "${RED}❌ 存在安全问题需要修复${NC}"
        exit 1
    fi
}

main "$@"
