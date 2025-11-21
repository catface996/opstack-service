#!/bin/bash

###############################################################################
# 生产就绪验证入口脚本
#
# 用途: 快捷调用各 spec 的验证脚本
# 用法: ./verify.sh [spec-id]
# 示例: ./verify.sh 001   (验证 001-init-ddd-architecture)
#       ./verify.sh       (默认验证 001)
###############################################################################

# 默认验证第一个 spec
SPEC_ID=${1:-001}

# 构建完整的 spec 目录名
SPEC_DIR=""
case "$SPEC_ID" in
    001)
        SPEC_DIR="001-init-ddd-architecture"
        ;;
    *)
        echo "错误: 未知的 spec ID: $SPEC_ID"
        echo "用法: ./verify.sh [001|002|003|...]"
        exit 1
        ;;
esac

# 验证脚本路径
VERIFY_SCRIPT="doc/02-verification/${SPEC_DIR}/verify.sh"

# 检查脚本是否存在
if [ ! -f "$VERIFY_SCRIPT" ]; then
    echo "错误: 验证脚本不存在: $VERIFY_SCRIPT"
    exit 1
fi

# 执行验证脚本
echo "执行验证: $SPEC_DIR"
echo "----------------------------------------"
bash "$VERIFY_SCRIPT"
