package com.catface996.aiops.common.exception;

/**
 * 业务异常
 * <p>
 * 用于业务逻辑验证失败、业务规则不满足等场景
 *
 * @author catface996
 * @since 2025-11-21
 */
public class BusinessException extends BaseException {

    public BusinessException(String errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }

    public BusinessException(String errorCode, String errorMessage, Throwable cause) {
        super(errorCode, errorMessage, cause);
    }
}
