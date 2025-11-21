package com.catface996.aiops.common.exception;

/**
 * 系统异常
 * <p>
 * 用于系统级错误，如数据库连接失败、第三方服务调用失败等
 *
 * @author catface996
 * @since 2025-11-21
 */
public class SystemException extends BaseException {

    public SystemException(String errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }

    public SystemException(String errorCode, String errorMessage, Throwable cause) {
        super(errorCode, errorMessage, cause);
    }
}
