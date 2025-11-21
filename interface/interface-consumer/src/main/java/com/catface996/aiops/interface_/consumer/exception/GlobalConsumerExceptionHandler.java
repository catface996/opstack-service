package com.catface996.aiops.interface_.consumer.exception;

import com.catface996.aiops.common.exception.BusinessException;
import com.catface996.aiops.common.exception.SystemException;
import lombok.extern.slf4j.Slf4j;

/**
 * 消息消费者全局异常处理工具类
 * <p>
 * 提供统一的异常处理方法供消息消费者使用
 *
 * @author catface996
 * @since 2025-11-21
 */
@Slf4j
public class GlobalConsumerExceptionHandler {

    /**
     * 处理业务异常
     *
     * @param e 业务异常
     */
    public static void handleBusinessException(BusinessException e) {
        log.warn("消息消费业务异常: code={}, message={}", e.getErrorCode(), e.getErrorMessage());
        // 业务异常不重试，记录日志即可
    }

    /**
     * 处理系统异常
     *
     * @param e 系统异常
     */
    public static void handleSystemException(SystemException e) {
        log.error("消息消费系统异常: code={}, message={}", e.getErrorCode(), e.getErrorMessage(), e);
        // 系统异常可能需要重试，根据具体情况处理
    }

    /**
     * 处理未知异常
     *
     * @param e 未知异常
     */
    public static void handleException(Exception e) {
        log.error("消息消费未知异常", e);
        // 未知异常记录日志，可能需要告警
    }

    /**
     * 统一异常处理入口
     * <p>
     * 根据异常类型调用相应的处理方法
     *
     * @param e 异常
     */
    public static void handle(Exception e) {
        if (e instanceof BusinessException) {
            handleBusinessException((BusinessException) e);
        } else if (e instanceof SystemException) {
            handleSystemException((SystemException) e);
        } else {
            handleException(e);
        }
    }
}
