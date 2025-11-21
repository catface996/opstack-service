package com.catface996.aiops.interface_.http.exception;

import com.catface996.aiops.common.exception.BusinessException;
import com.catface996.aiops.common.exception.SystemException;
import com.catface996.aiops.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 *
 * @author catface996
 * @since 2025-11-21
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     *
     * @param e 业务异常
     * @return 错误响应
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: code={}, message={}", e.getErrorCode(), e.getErrorMessage());
        return Result.failure(e.getErrorCode(), e.getErrorMessage());
    }

    /**
     * 处理系统异常
     *
     * @param e 系统异常
     * @return 错误响应
     */
    @ExceptionHandler(SystemException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleSystemException(SystemException e) {
        log.error("系统异常: code={}, message={}", e.getErrorCode(), e.getErrorMessage(), e);
        return Result.failure(e.getErrorCode(), e.getErrorMessage());
    }

    /**
     * 处理未知异常
     *
     * @param e 未知异常
     * @return 错误响应
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        log.error("未知异常", e);
        // 不暴露内部实现细节
        return Result.failure("SYSTEM_ERROR", "系统错误，请稍后重试");
    }
}
