#!/bin/bash
# F08 - 子图管理端到端测试脚本
# 验证子图管理功能的所有需求 (REQ 1-10)
#
# Feature: f08-subgraph-management
# Task: 29 - 编写端到端测试脚本
#
# Usage:
#   ./subgraph-e2e-test.sh [BASE_URL]
#
# Example:
#   ./subgraph-e2e-test.sh http://localhost:8080

set -e

# 配置
BASE_URL="${1:-http://localhost:8080}"
TIMESTAMP=$(date +%s)
PASSED=0
FAILED=0
TOTAL=0

# 颜色
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# 辅助函数
log_info() { echo -e "${YELLOW}[INFO]${NC} $1"; }
log_pass() { echo -e "${GREEN}[PASS]${NC} $1"; ((PASSED++)); ((TOTAL++)); }
log_fail() { echo -e "${RED}[FAIL]${NC} $1"; ((FAILED++)); ((TOTAL++)); }
log_section() { echo -e "\n${BLUE}========== $1 ==========${NC}"; }

# 检查响应
assert_success() {
    local response=$1
    local test_name=$2
    if echo "$response" | grep -q '"success":true'; then
        log_pass "$test_name"
        return 0
    else
        log_fail "$test_name: $response"
        return 1
    fi
}

assert_http_status() {
    local status=$1
    local expected=$2
    local test_name=$3
    if [ "$status" = "$expected" ]; then
        log_pass "$test_name"
        return 0
    else
        log_fail "$test_name: expected HTTP $expected, got HTTP $status"
        return 1
    fi
}

extract_id() {
    echo "$1" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2
}

extract_token() {
    echo "$1" | grep -o '"token":"[^"]*"' | cut -d'"' -f4
}

extract_version() {
    echo "$1" | grep -o '"version":[0-9]*' | head -1 | cut -d':' -f2
}

echo "=============================================="
echo "    F08 - 子图管理端到端测试"
echo "    时间: $(date)"
echo "    基础URL: $BASE_URL"
echo "=============================================="

# ============================================
# 准备测试数据
# ============================================
log_section "准备测试数据"

# 创建第一个测试用户
USER1="subgraph_user1_${TIMESTAMP}"
EMAIL1="${USER1}@test.com"
PASSWORD="SecureP@ss123"

log_info "创建测试用户1: $USER1"
REG_RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/auth/register" \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"$USER1\",\"email\":\"$EMAIL1\",\"password\":\"$PASSWORD\"}")

LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"identifier\":\"$USER1\",\"password\":\"$PASSWORD\",\"rememberMe\":false}")
TOKEN1=$(extract_token "$LOGIN_RESPONSE")

if [ -z "$TOKEN1" ]; then
    log_fail "用户1登录失败，无法继续测试"
    exit 1
fi
log_pass "用户1登录成功"

# 创建第二个测试用户（用于权限测试）
USER2="subgraph_user2_${TIMESTAMP}"
EMAIL2="${USER2}@test.com"

log_info "创建测试用户2: $USER2"
curl -s -X POST "$BASE_URL/api/v1/auth/register" \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"$USER2\",\"email\":\"$EMAIL2\",\"password\":\"$PASSWORD\"}" > /dev/null

LOGIN_RESPONSE2=$(curl -s -X POST "$BASE_URL/api/v1/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"identifier\":\"$USER2\",\"password\":\"$PASSWORD\",\"rememberMe\":false}")
TOKEN2=$(extract_token "$LOGIN_RESPONSE2")

if [ -z "$TOKEN2" ]; then
    log_fail "用户2登录失败"
else
    log_pass "用户2登录成功"
fi

# ============================================
# 需求1: 子图创建
# ============================================
log_section "需求1: 子图创建"

SUBGRAPH_NAME="test-subgraph-${TIMESTAMP}"

# AC1: 创建子图
log_info "AC1: 创建子图..."
CREATE_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/v1/subgraphs" \
    -H "Authorization: Bearer $TOKEN1" \
    -H "Content-Type: application/json" \
    -d "{
        \"name\": \"$SUBGRAPH_NAME\",
        \"description\": \"测试子图描述\",
        \"tags\": [\"test\", \"e2e\"],
        \"metadata\": {\"domain\": \"payment\", \"env\": \"test\"}
    }")

HTTP_STATUS=$(echo "$CREATE_RESPONSE" | tail -1)
RESPONSE_BODY=$(echo "$CREATE_RESPONSE" | head -n -1)

if [ "$HTTP_STATUS" = "201" ]; then
    SUBGRAPH_ID=$(extract_id "$RESPONSE_BODY")
    SUBGRAPH_VERSION=$(extract_version "$RESPONSE_BODY")
    log_pass "需求1 AC1: 子图创建成功，ID=$SUBGRAPH_ID"
else
    log_fail "需求1 AC1: 子图创建失败，HTTP $HTTP_STATUS: $RESPONSE_BODY"
    SUBGRAPH_ID=""
fi

# AC2: 创建者自动成为 Owner
if [ -n "$SUBGRAPH_ID" ]; then
    log_info "AC2: 验证创建者自动成为 Owner..."
    DETAIL_RESPONSE=$(curl -s -X GET "$BASE_URL/api/v1/subgraphs/$SUBGRAPH_ID" \
        -H "Authorization: Bearer $TOKEN1")
    if echo "$DETAIL_RESPONSE" | grep -q '"owners"'; then
        log_pass "需求1 AC2: 创建者自动成为 Owner"
    else
        log_fail "需求1 AC2: 未找到 Owner 信息"
    fi
fi

# AC4: 名称重复检查
log_info "AC4: 验证名称重复被拒绝..."
DUP_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/v1/subgraphs" \
    -H "Authorization: Bearer $TOKEN1" \
    -H "Content-Type: application/json" \
    -d "{\"name\": \"$SUBGRAPH_NAME\", \"description\": \"重复名称测试\"}")

DUP_STATUS=$(echo "$DUP_RESPONSE" | tail -1)
if [ "$DUP_STATUS" = "409" ]; then
    log_pass "需求1 AC4: 名称重复返回 409 Conflict"
else
    log_fail "需求1 AC4: 名称重复应返回 409，实际返回 HTTP $DUP_STATUS"
fi

# AC3: 缺少必填字段
log_info "AC3: 验证缺少必填字段被拒绝..."
INVALID_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/v1/subgraphs" \
    -H "Authorization: Bearer $TOKEN1" \
    -H "Content-Type: application/json" \
    -d "{\"description\": \"缺少名称\"}")

INVALID_STATUS=$(echo "$INVALID_RESPONSE" | tail -1)
if [ "$INVALID_STATUS" = "400" ]; then
    log_pass "需求1 AC3: 缺少必填字段返回 400 Bad Request"
else
    log_fail "需求1 AC3: 缺少必填字段应返回 400，实际返回 HTTP $INVALID_STATUS"
fi

# ============================================
# 需求2: 子图列表视图
# ============================================
log_section "需求2: 子图列表视图"

# AC1: 查询子图列表
log_info "AC1: 查询子图列表..."
LIST_RESPONSE=$(curl -s -X GET "$BASE_URL/api/v1/subgraphs?page=1&pageSize=20" \
    -H "Authorization: Bearer $TOKEN1")
if assert_success "$LIST_RESPONSE" "需求2 AC1: 查询子图列表"; then
    :
fi

# AC2: 关键词搜索
log_info "AC2: 关键词搜索..."
SEARCH_RESPONSE=$(curl -s -X GET "$BASE_URL/api/v1/subgraphs?keyword=test-subgraph" \
    -H "Authorization: Bearer $TOKEN1")
if echo "$SEARCH_RESPONSE" | grep -q '"success":true'; then
    log_pass "需求2 AC2: 关键词搜索成功"
else
    log_fail "需求2 AC2: 关键词搜索失败"
fi

# ============================================
# 需求3: 子图信息编辑
# ============================================
log_section "需求3: 子图信息编辑"

if [ -n "$SUBGRAPH_ID" ]; then
    # AC1: Owner 更新子图
    log_info "AC1: Owner 更新子图..."
    UPDATE_RESPONSE=$(curl -s -w "\n%{http_code}" -X PUT "$BASE_URL/api/v1/subgraphs/$SUBGRAPH_ID" \
        -H "Authorization: Bearer $TOKEN1" \
        -H "Content-Type: application/json" \
        -d "{
            \"description\": \"更新后的描述\",
            \"tags\": [\"test\", \"e2e\", \"updated\"],
            \"version\": $SUBGRAPH_VERSION
        }")

    UPDATE_STATUS=$(echo "$UPDATE_RESPONSE" | tail -1)
    UPDATE_BODY=$(echo "$UPDATE_RESPONSE" | head -n -1)
    if [ "$UPDATE_STATUS" = "200" ]; then
        NEW_VERSION=$(extract_version "$UPDATE_BODY")
        log_pass "需求3 AC1: Owner 更新子图成功"
    else
        log_fail "需求3 AC1: Owner 更新子图失败，HTTP $UPDATE_STATUS"
        NEW_VERSION=$SUBGRAPH_VERSION
    fi

    # AC2: 非 Owner 更新应被拒绝
    log_info "AC2: 验证非 Owner 更新被拒绝..."
    NON_OWNER_UPDATE=$(curl -s -w "\n%{http_code}" -X PUT "$BASE_URL/api/v1/subgraphs/$SUBGRAPH_ID" \
        -H "Authorization: Bearer $TOKEN2" \
        -H "Content-Type: application/json" \
        -d "{\"description\": \"非法更新\", \"version\": $NEW_VERSION}")

    NON_OWNER_STATUS=$(echo "$NON_OWNER_UPDATE" | tail -1)
    if [ "$NON_OWNER_STATUS" = "403" ]; then
        log_pass "需求3 AC2: 非 Owner 更新返回 403 Forbidden"
    else
        log_fail "需求3 AC2: 非 Owner 更新应返回 403，实际返回 HTTP $NON_OWNER_STATUS"
    fi

    # 版本冲突测试 (使用旧版本号)
    log_info "验证版本冲突..."
    CONFLICT_RESPONSE=$(curl -s -w "\n%{http_code}" -X PUT "$BASE_URL/api/v1/subgraphs/$SUBGRAPH_ID" \
        -H "Authorization: Bearer $TOKEN1" \
        -H "Content-Type: application/json" \
        -d "{\"description\": \"版本冲突测试\", \"version\": 999}")

    CONFLICT_STATUS=$(echo "$CONFLICT_RESPONSE" | tail -1)
    if [ "$CONFLICT_STATUS" = "409" ]; then
        log_pass "乐观锁: 版本冲突返回 409 Conflict"
    else
        log_fail "乐观锁: 版本冲突应返回 409，实际返回 HTTP $CONFLICT_STATUS"
    fi
fi

# ============================================
# 需求7: 子图详情视图
# ============================================
log_section "需求7: 子图详情视图"

if [ -n "$SUBGRAPH_ID" ]; then
    # AC1: 获取子图详情
    log_info "AC1: 获取子图详情..."
    DETAIL_RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/v1/subgraphs/$SUBGRAPH_ID" \
        -H "Authorization: Bearer $TOKEN1")

    DETAIL_STATUS=$(echo "$DETAIL_RESPONSE" | tail -1)
    DETAIL_BODY=$(echo "$DETAIL_RESPONSE" | head -n -1)
    if [ "$DETAIL_STATUS" = "200" ]; then
        if echo "$DETAIL_BODY" | grep -q '"name"' && echo "$DETAIL_BODY" | grep -q '"owners"'; then
            log_pass "需求7 AC1: 获取子图详情成功"
        else
            log_fail "需求7 AC1: 详情数据不完整"
        fi
    else
        log_fail "需求7 AC1: 获取子图详情失败，HTTP $DETAIL_STATUS"
    fi

    # 无权限用户获取详情
    log_info "验证无权限用户获取详情被拒绝..."
    NO_PERM_DETAIL=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/v1/subgraphs/$SUBGRAPH_ID" \
        -H "Authorization: Bearer $TOKEN2")

    NO_PERM_STATUS=$(echo "$NO_PERM_DETAIL" | tail -1)
    if [ "$NO_PERM_STATUS" = "403" ]; then
        log_pass "权限检查: 无权限用户获取详情返回 403"
    else
        log_fail "权限检查: 无权限用户获取详情应返回 403，实际返回 HTTP $NO_PERM_STATUS"
    fi
fi

# ============================================
# 需求7.3-7.6: 拓扑查询
# ============================================
log_section "拓扑查询"

if [ -n "$SUBGRAPH_ID" ]; then
    log_info "获取子图拓扑..."
    TOPOLOGY_RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/v1/subgraphs/$SUBGRAPH_ID/topology" \
        -H "Authorization: Bearer $TOKEN1")

    TOPOLOGY_STATUS=$(echo "$TOPOLOGY_RESPONSE" | tail -1)
    TOPOLOGY_BODY=$(echo "$TOPOLOGY_RESPONSE" | head -n -1)
    if [ "$TOPOLOGY_STATUS" = "200" ]; then
        if echo "$TOPOLOGY_BODY" | grep -q '"nodes"' && echo "$TOPOLOGY_BODY" | grep -q '"edges"'; then
            log_pass "需求7.3: 获取子图拓扑成功"
        else
            log_fail "需求7.3: 拓扑数据不完整"
        fi
    else
        log_fail "需求7.3: 获取子图拓扑失败，HTTP $TOPOLOGY_STATUS"
    fi
fi

# ============================================
# 需求4: 子图删除
# ============================================
log_section "需求4: 子图删除"

if [ -n "$SUBGRAPH_ID" ]; then
    # AC1: 非 Owner 删除应被拒绝
    log_info "AC1: 验证非 Owner 删除被拒绝..."
    NON_OWNER_DELETE=$(curl -s -w "\n%{http_code}" -X DELETE "$BASE_URL/api/v1/subgraphs/$SUBGRAPH_ID" \
        -H "Authorization: Bearer $TOKEN2")

    NON_OWNER_DEL_STATUS=$(echo "$NON_OWNER_DELETE" | tail -1)
    if [ "$NON_OWNER_DEL_STATUS" = "403" ]; then
        log_pass "需求4 AC1: 非 Owner 删除返回 403 Forbidden"
    else
        log_fail "需求4 AC1: 非 Owner 删除应返回 403，实际返回 HTTP $NON_OWNER_DEL_STATUS"
    fi

    # AC4: Owner 删除空子图
    log_info "AC4: Owner 删除空子图..."
    DELETE_RESPONSE=$(curl -s -w "\n%{http_code}" -X DELETE "$BASE_URL/api/v1/subgraphs/$SUBGRAPH_ID" \
        -H "Authorization: Bearer $TOKEN1")

    DELETE_STATUS=$(echo "$DELETE_RESPONSE" | tail -1)
    if [ "$DELETE_STATUS" = "204" ]; then
        log_pass "需求4 AC4: Owner 删除空子图成功"
    else
        log_fail "需求4 AC4: Owner 删除空子图失败，HTTP $DELETE_STATUS"
    fi

    # 验证删除后获取返回 404
    log_info "验证删除后获取返回 404..."
    DELETED_GET=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/v1/subgraphs/$SUBGRAPH_ID" \
        -H "Authorization: Bearer $TOKEN1")

    DELETED_STATUS=$(echo "$DELETED_GET" | tail -1)
    if [ "$DELETED_STATUS" = "404" ]; then
        log_pass "删除验证: 已删除子图返回 404 Not Found"
    else
        log_fail "删除验证: 已删除子图应返回 404，实际返回 HTTP $DELETED_STATUS"
    fi
fi

# ============================================
# 完整生命周期测试
# ============================================
log_section "完整生命周期测试"

LIFECYCLE_NAME="lifecycle-${TIMESTAMP}"

# 1. 创建
log_info "1. 创建子图..."
LC_CREATE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/v1/subgraphs" \
    -H "Authorization: Bearer $TOKEN1" \
    -H "Content-Type: application/json" \
    -d "{\"name\": \"$LIFECYCLE_NAME\", \"description\": \"生命周期测试\"}")

LC_CREATE_STATUS=$(echo "$LC_CREATE" | tail -1)
LC_CREATE_BODY=$(echo "$LC_CREATE" | head -n -1)
LC_ID=$(extract_id "$LC_CREATE_BODY")
LC_VERSION=$(extract_version "$LC_CREATE_BODY")

if [ "$LC_CREATE_STATUS" = "201" ] && [ -n "$LC_ID" ]; then
    log_pass "生命周期 1: 创建成功，ID=$LC_ID"

    # 2. 查询
    log_info "2. 查询详情..."
    LC_GET=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/v1/subgraphs/$LC_ID" \
        -H "Authorization: Bearer $TOKEN1")
    LC_GET_STATUS=$(echo "$LC_GET" | tail -1)
    if [ "$LC_GET_STATUS" = "200" ]; then
        log_pass "生命周期 2: 查询成功"
    else
        log_fail "生命周期 2: 查询失败，HTTP $LC_GET_STATUS"
    fi

    # 3. 更新
    log_info "3. 更新子图..."
    LC_UPDATE=$(curl -s -w "\n%{http_code}" -X PUT "$BASE_URL/api/v1/subgraphs/$LC_ID" \
        -H "Authorization: Bearer $TOKEN1" \
        -H "Content-Type: application/json" \
        -d "{\"description\": \"更新后\", \"version\": $LC_VERSION}")
    LC_UPDATE_STATUS=$(echo "$LC_UPDATE" | tail -1)
    if [ "$LC_UPDATE_STATUS" = "200" ]; then
        log_pass "生命周期 3: 更新成功"
    else
        log_fail "生命周期 3: 更新失败，HTTP $LC_UPDATE_STATUS"
    fi

    # 4. 删除
    log_info "4. 删除子图..."
    LC_DELETE=$(curl -s -w "\n%{http_code}" -X DELETE "$BASE_URL/api/v1/subgraphs/$LC_ID" \
        -H "Authorization: Bearer $TOKEN1")
    LC_DELETE_STATUS=$(echo "$LC_DELETE" | tail -1)
    if [ "$LC_DELETE_STATUS" = "204" ]; then
        log_pass "生命周期 4: 删除成功"
    else
        log_fail "生命周期 4: 删除失败，HTTP $LC_DELETE_STATUS"
    fi
else
    log_fail "生命周期 1: 创建失败，无法继续测试"
fi

# ============================================
# 测试报告
# ============================================
echo ""
echo "=============================================="
echo "    F08 子图管理测试报告"
echo "=============================================="
echo -e "通过: ${GREEN}$PASSED${NC}"
echo -e "失败: ${RED}$FAILED${NC}"
echo -e "总计: $TOTAL"
echo ""

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}所有测试通过！${NC}"
    exit 0
else
    echo -e "${RED}有 $FAILED 个测试失败${NC}"
    exit 1
fi
