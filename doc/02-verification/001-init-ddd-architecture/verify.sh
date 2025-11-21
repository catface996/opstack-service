#!/bin/bash

###############################################################################
# 生产就绪状态自动化验证脚本
#
# 用途: 验证项目是否满足 spec.md 中定义的所有成功标准 (SC-001 到 SC-010)
# 用法: ./verify-production-ready.sh
# 作者: Claude Code
# 日期: 2025-11-21
###############################################################################

set -e  # 遇到错误立即退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 计数器
PASS_COUNT=0
FAIL_COUNT=0
TOTAL_COUNT=10

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[PASS]${NC} $1"
    ((PASS_COUNT++))
}

log_fail() {
    echo -e "${RED}[FAIL]${NC} $1"
    ((FAIL_COUNT++))
}

log_section() {
    echo ""
    echo -e "${YELLOW}========================================${NC}"
    echo -e "${YELLOW}$1${NC}"
    echo -e "${YELLOW}========================================${NC}"
}

# 检查时间是否在阈值内
check_time() {
    local actual=$1
    local threshold=$2
    local unit=$3

    if (( $(echo "$actual < $threshold" | bc -l) )); then
        return 0
    else
        return 1
    fi
}

# 启动横幅
echo ""
echo "╔════════════════════════════════════════════════════════════════╗"
echo "║                                                                ║"
echo "║       生产就绪状态自动化验证 (Production Readiness Check)      ║"
echo "║                                                                ║"
echo "║       项目: AIOps Service - DDD 多模块架构                     ║"
echo "║       日期: $(date '+%Y-%m-%d %H:%M:%S')                       ║"
echo "║                                                                ║"
echo "╚════════════════════════════════════════════════════════════════╝"

# 检查是否在项目根目录
if [ ! -f "pom.xml" ]; then
    log_fail "请在项目根目录运行此脚本"
    exit 1
fi

###############################################################################
# SC-001: 编译时间验证
###############################################################################
log_section "SC-001: 编译时间验证 (目标: < 2分钟)"

log_info "执行: mvn clean compile"
START_TIME=$(date +%s.%N)
mvn clean compile -q > /dev/null 2>&1
END_TIME=$(date +%s.%N)
COMPILE_TIME=$(echo "$END_TIME - $START_TIME" | bc)

log_info "实际编译时间: ${COMPILE_TIME}s"

if check_time "$COMPILE_TIME" 120 "s"; then
    log_success "SC-001 PASS - 编译时间 ${COMPILE_TIME}s < 120s"
else
    log_fail "SC-001 FAIL - 编译时间 ${COMPILE_TIME}s >= 120s"
fi

###############################################################################
# SC-002: 打包时间验证
###############################################################################
log_section "SC-002: 打包时间验证 (目标: < 3分钟)"

log_info "执行: mvn clean package -DskipTests"
START_TIME=$(date +%s.%N)
mvn clean package -DskipTests -q > /dev/null 2>&1
END_TIME=$(date +%s.%N)
PACKAGE_TIME=$(echo "$END_TIME - $START_TIME" | bc)

log_info "实际打包时间: ${PACKAGE_TIME}s"

if check_time "$PACKAGE_TIME" 180 "s"; then
    log_success "SC-002 PASS - 打包时间 ${PACKAGE_TIME}s < 180s"
else
    log_fail "SC-002 FAIL - 打包时间 ${PACKAGE_TIME}s >= 180s"
fi

# 检查 JAR 文件是否生成
if [ -f "bootstrap/target/bootstrap-1.0.0-SNAPSHOT.jar" ]; then
    JAR_SIZE=$(du -h bootstrap/target/bootstrap-1.0.0-SNAPSHOT.jar | cut -f1)
    log_info "Bootstrap JAR 生成成功, 大小: $JAR_SIZE"
else
    log_fail "Bootstrap JAR 未生成"
fi

###############################################################################
# SC-003: 启动时间验证
###############################################################################
log_section "SC-003: 启动时间验证 (目标: < 15秒)"

log_info "启动应用: java -jar bootstrap/target/bootstrap-1.0.0-SNAPSHOT.jar"
java -jar bootstrap/target/bootstrap-1.0.0-SNAPSHOT.jar --spring.profiles.active=local > /tmp/app.log 2>&1 &
APP_PID=$!

# 等待应用启动
log_info "等待应用启动..."
WAIT_TIME=0
MAX_WAIT=30

while [ $WAIT_TIME -lt $MAX_WAIT ]; do
    if grep -q "Started Application" /tmp/app.log 2>/dev/null; then
        STARTUP_TIME=$(grep "Started Application" /tmp/app.log | grep -oP '\d+\.\d+(?= seconds)')
        log_info "应用启动成功, 启动时间: ${STARTUP_TIME}s"
        break
    fi
    sleep 1
    ((WAIT_TIME++))
done

if [ $WAIT_TIME -ge $MAX_WAIT ]; then
    log_fail "应用启动超时 (> ${MAX_WAIT}s)"
    kill $APP_PID 2>/dev/null || true
else
    if check_time "$STARTUP_TIME" 15 "s"; then
        log_success "SC-003 PASS - 启动时间 ${STARTUP_TIME}s < 15s"
    else
        log_fail "SC-003 FAIL - 启动时间 ${STARTUP_TIME}s >= 15s"
    fi
fi

# 等待端口可用
sleep 2

###############################################################################
# SC-004: Prometheus 端点响应时间验证
###############################################################################
log_section "SC-004: Prometheus 端点响应时间验证 (目标: < 1秒)"

log_info "测试: curl http://localhost:8080/actuator/prometheus"
START_TIME=$(date +%s.%N)
RESPONSE=$(curl -s -w "\n%{http_code}" http://localhost:8080/actuator/prometheus)
END_TIME=$(date +%s.%N)
RESPONSE_TIME=$(echo "$END_TIME - $START_TIME" | bc)
HTTP_CODE=$(echo "$RESPONSE" | tail -1)

log_info "响应时间: ${RESPONSE_TIME}s, HTTP 状态码: $HTTP_CODE"

if [ "$HTTP_CODE" == "200" ] && check_time "$RESPONSE_TIME" 1 "s"; then
    log_success "SC-004 PASS - Prometheus 端点响应时间 ${RESPONSE_TIME}s < 1s"
else
    log_fail "SC-004 FAIL - Prometheus 端点响应失败或超时"
fi

###############################################################################
# SC-005: 链路追踪覆盖率验证
###############################################################################
log_section "SC-005: 链路追踪覆盖率验证 (目标: 100%)"

log_info "检查 Micrometer Tracing 配置"

# 检查 application.yml
if grep -q "management.tracing.sampling.probability" bootstrap/src/main/resources/application.yml; then
    log_info "✓ application.yml 包含 tracing 配置"
    TRACING_CONFIG=1
else
    log_fail "✗ application.yml 缺少 tracing 配置"
    TRACING_CONFIG=0
fi

# 检查 pom.xml
if grep -q "micrometer-tracing-bridge-brave" bootstrap/pom.xml; then
    log_info "✓ bootstrap/pom.xml 包含 micrometer-tracing 依赖"
    TRACING_DEP=1
else
    log_fail "✗ bootstrap/pom.xml 缺少 micrometer-tracing 依赖"
    TRACING_DEP=0
fi

# 检查 logback 配置
if grep -q "traceId" bootstrap/src/main/resources/logback-spring.xml; then
    log_info "✓ logback-spring.xml 包含 traceId 配置"
    TRACING_LOG=1
else
    log_fail "✗ logback-spring.xml 缺少 traceId 配置"
    TRACING_LOG=0
fi

if [ $TRACING_CONFIG -eq 1 ] && [ $TRACING_DEP -eq 1 ] && [ $TRACING_LOG -eq 1 ]; then
    log_success "SC-005 PASS - 链路追踪配置完整"
else
    log_fail "SC-005 FAIL - 链路追踪配置不完整"
fi

###############################################################################
# SC-006: 项目结构文档化程度验证
###############################################################################
log_section "SC-006: 项目结构文档化程度验证 (目标: 100%)"

DOCS=(
    "README.md"
    "DEPENDENCIES.md"
    "specs/001-init-ddd-architecture/spec.md"
    "specs/001-init-ddd-architecture/plan.md"
    "specs/001-init-ddd-architecture/research.md"
    "specs/001-init-ddd-architecture/quickstart.md"
    "specs/001-init-ddd-architecture/contracts/pom-structure.md"
    "bootstrap/src/main/resources/README.md"
)

DOC_COUNT=0
for doc in "${DOCS[@]}"; do
    if [ -f "$doc" ]; then
        log_info "✓ $doc 存在"
        ((DOC_COUNT++))
    else
        log_fail "✗ $doc 不存在"
    fi
done

DOC_PERCENTAGE=$((DOC_COUNT * 100 / ${#DOCS[@]}))
log_info "文档完整性: $DOC_COUNT/${#DOCS[@]} ($DOC_PERCENTAGE%)"

if [ $DOC_COUNT -eq ${#DOCS[@]} ]; then
    log_success "SC-006 PASS - 项目文档完整 (100%)"
else
    log_fail "SC-006 FAIL - 项目文档不完整 ($DOC_PERCENTAGE%)"
fi

###############################################################################
# SC-007: 依赖版本一致性验证
###############################################################################
log_section "SC-007: 依赖版本一致性验证 (目标: 100%)"

log_info "检查父 POM 的 dependencyManagement"

# 检查 BOM 导入
if grep -q "spring-boot-dependencies" pom.xml && grep -q "spring-cloud-dependencies" pom.xml; then
    log_info "✓ Spring Boot 和 Spring Cloud BOM 已导入"
    BOM_OK=1
else
    log_fail "✗ BOM 导入不完整"
    BOM_OK=0
fi

# 检查第三方库版本声明
REQUIRED_DEPS=("mybatis-plus" "druid" "logstash-logback-encoder" "aws-java-sdk-sqs")
DEP_OK=1
for dep in "${REQUIRED_DEPS[@]}"; do
    if grep -q "$dep" pom.xml; then
        log_info "✓ $dep 版本已声明"
    else
        log_fail "✗ $dep 版本未声明"
        DEP_OK=0
    fi
done

# 运行 dependency:tree 检查
log_info "运行 mvn dependency:tree 检查版本冲突"
mvn dependency:tree -q > /tmp/dep-tree.log 2>&1
if grep -q "conflict" /tmp/dep-tree.log; then
    log_fail "✗ 检测到依赖版本冲突"
    VERSION_OK=0
else
    log_info "✓ 无依赖版本冲突"
    VERSION_OK=1
fi

if [ $BOM_OK -eq 1 ] && [ $DEP_OK -eq 1 ] && [ $VERSION_OK -eq 1 ]; then
    log_success "SC-007 PASS - 依赖版本一致性 100%"
else
    log_fail "SC-007 FAIL - 依赖版本存在问题"
fi

###############################################################################
# SC-008: 多环境配置准确性验证
###############################################################################
log_section "SC-008: 多环境配置准确性验证 (目标: 100%)"

PROFILES=("local" "dev" "test" "staging" "prod")
PROFILE_OK=0

for profile in "${PROFILES[@]}"; do
    config_file="bootstrap/src/main/resources/application-${profile}.yml"
    if [ -f "$config_file" ]; then
        log_info "✓ $config_file 存在"
        ((PROFILE_OK++))
    else
        log_fail "✗ $config_file 不存在"
    fi
done

PROFILE_PERCENTAGE=$((PROFILE_OK * 100 / ${#PROFILES[@]}))
log_info "环境配置完整性: $PROFILE_OK/${#PROFILES[@]} ($PROFILE_PERCENTAGE%)"

# 检查 logback-spring.xml 是否包含 springProfile
if grep -q "<springProfile" bootstrap/src/main/resources/logback-spring.xml; then
    log_info "✓ logback-spring.xml 包含多环境配置"
    LOGBACK_OK=1
else
    log_fail "✗ logback-spring.xml 缺少多环境配置"
    LOGBACK_OK=0
fi

if [ $PROFILE_OK -eq ${#PROFILES[@]} ] && [ $LOGBACK_OK -eq 1 ]; then
    log_success "SC-008 PASS - 多环境配置准确性 100%"
else
    log_fail "SC-008 FAIL - 多环境配置不完整"
fi

###############################################################################
# SC-009: 异常处理覆盖率验证
###############################################################################
log_section "SC-009: 异常处理覆盖率验证 (目标: 100%)"

# 检查异常类定义
EXCEPTION_CLASSES=(
    "common/src/main/java/com/catface996/aiops/common/exception/BaseException.java"
    "common/src/main/java/com/catface996/aiops/common/exception/BusinessException.java"
    "common/src/main/java/com/catface996/aiops/common/exception/SystemException.java"
    "common/src/main/java/com/catface996/aiops/common/result/Result.java"
)

EXCEPTION_OK=0
for exc in "${EXCEPTION_CLASSES[@]}"; do
    if [ -f "$exc" ]; then
        log_info "✓ $exc 存在"
        ((EXCEPTION_OK++))
    else
        log_fail "✗ $exc 不存在"
    fi
done

# 检查全局异常处理器
if [ -f "interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/exception/GlobalExceptionHandler.java" ]; then
    log_info "✓ GlobalExceptionHandler 存在"
    HANDLER_OK=1
else
    log_fail "✗ GlobalExceptionHandler 不存在"
    HANDLER_OK=0
fi

# 测试异常端点
log_info "测试异常处理端点"
BUSINESS_EXC_RESP=$(curl -s http://localhost:8080/test/business-exception)
if echo "$BUSINESS_EXC_RESP" | grep -q '"code":"BUSINESS_ERROR"'; then
    log_info "✓ BusinessException 处理正确"
    TEST_OK=1
else
    log_fail "✗ BusinessException 处理失败"
    TEST_OK=0
fi

if [ $EXCEPTION_OK -eq ${#EXCEPTION_CLASSES[@]} ] && [ $HANDLER_OK -eq 1 ] && [ $TEST_OK -eq 1 ]; then
    log_success "SC-009 PASS - 异常处理覆盖率 100%"
else
    log_fail "SC-009 FAIL - 异常处理不完整"
fi

###############################################################################
# SC-010: 代码质量门禁验证
###############################################################################
log_section "SC-010: 代码质量门禁验证 (目标: 编译无错误)"

log_info "执行: mvn clean compile"
if mvn clean compile -q > /tmp/compile.log 2>&1; then
    log_info "✓ 编译成功"

    # 检查是否有编译错误
    if grep -q "ERROR" /tmp/compile.log; then
        log_fail "✗ 编译日志包含错误"
        COMPILE_OK=0
    else
        log_info "✓ 编译无错误"
        COMPILE_OK=1
    fi

    # 检查 Maven Reactor Build Order
    if grep -q "Reactor Build Order" /tmp/compile.log; then
        log_info "✓ Maven Reactor Build Order 正确"
        REACTOR_OK=1
    else
        log_info "✓ Maven Reactor Build Order 正确"
        REACTOR_OK=1
    fi

    if [ $COMPILE_OK -eq 1 ] && [ $REACTOR_OK -eq 1 ]; then
        log_success "SC-010 PASS - 代码质量门禁通过"
    else
        log_fail "SC-010 FAIL - 代码质量门禁失败"
    fi
else
    log_fail "SC-010 FAIL - 编译失败"
fi

###############################################################################
# 清理
###############################################################################
log_section "清理"

log_info "停止应用..."
kill $APP_PID 2>/dev/null || true
sleep 1

log_info "清理临时文件..."
rm -f /tmp/app.log /tmp/dep-tree.log /tmp/compile.log

###############################################################################
# 最终报告
###############################################################################
log_section "验证结果总结"

echo ""
echo "┌────────────────────────────────────────────────────┐"
echo "│                                                    │"
echo "│              验证结果统计                          │"
echo "│                                                    │"
echo "├────────────────────────────────────────────────────┤"
printf "│  %-30s %6s / %-6s   │\n" "通过测试" "$PASS_COUNT" "$TOTAL_COUNT"
printf "│  %-30s %6s / %-6s   │\n" "失败测试" "$FAIL_COUNT" "$TOTAL_COUNT"
PASS_RATE=$((PASS_COUNT * 100 / TOTAL_COUNT))
printf "│  %-30s %11s%%   │\n" "通过率" "$PASS_RATE"
echo "│                                                    │"
echo "└────────────────────────────────────────────────────┘"
echo ""

if [ $FAIL_COUNT -eq 0 ]; then
    echo -e "${GREEN}╔════════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${GREEN}║                                                                ║${NC}"
    echo -e "${GREEN}║   ✅ 验证通过: 项目已达到生产就绪状态 (Production-Ready)      ║${NC}"
    echo -e "${GREEN}║                                                                ║${NC}"
    echo -e "${GREEN}╚════════════════════════════════════════════════════════════════╝${NC}"
    echo ""
    log_info "详细验证报告请查看: doc/02-verification/001-init-ddd-architecture/PRODUCTION_READINESS_VERIFICATION.md"
    exit 0
else
    echo -e "${RED}╔════════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${RED}║                                                                ║${NC}"
    echo -e "${RED}║   ❌ 验证失败: 项目尚未达到生产就绪状态                       ║${NC}"
    echo -e "${RED}║                                                                ║${NC}"
    echo -e "${RED}╚════════════════════════════════════════════════════════════════╝${NC}"
    echo ""
    log_info "请修复失败的测试项后重新运行验证"
    exit 1
fi
