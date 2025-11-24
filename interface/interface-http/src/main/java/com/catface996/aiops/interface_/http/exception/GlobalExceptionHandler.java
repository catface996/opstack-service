package com.catface996.aiops.interface_.http.exception;

import com.catface996.aiops.common.exception.BusinessException;
import com.catface996.aiops.common.exception.SystemException;
import com.catface996.aiops.domain.api.exception.auth.AccountLockedException;
import com.catface996.aiops.domain.api.exception.auth.AccountNotFoundException;
import com.catface996.aiops.domain.api.exception.auth.AuthenticationException;
import com.catface996.aiops.domain.api.exception.auth.DuplicateEmailException;
import com.catface996.aiops.domain.api.exception.auth.DuplicateUsernameException;
import com.catface996.aiops.domain.api.exception.auth.InvalidPasswordException;
import com.catface996.aiops.domain.api.exception.auth.InvalidTokenException;
import com.catface996.aiops.domain.api.exception.auth.SessionExpiredException;
import com.catface996.aiops.domain.api.exception.auth.SessionNotFoundException;
import com.catface996.aiops.interface_.http.response.ApiResponse;
import com.catface996.aiops.interface_.http.response.ErrorDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 全局异常处理器
 *
 * <p>统一处理应用中的所有异常，将异常映射到标准的HTTP响应。</p>
 *
 * <p>异常映射规则：</p>
 * <ul>
 *   <li>AuthenticationException → 401 Unauthorized（认证失败）</li>
 *   <li>SessionExpiredException/SessionNotFoundException → 401 Unauthorized（会话无效）</li>
 *   <li>InvalidTokenException → 401 Unauthorized（Token无效）</li>
 *   <li>AccountNotFoundException → 404 Not Found（账号不存在）</li>
 *   <li>DuplicateUsernameException/DuplicateEmailException → 409 Conflict（资源冲突）</li>
 *   <li>InvalidPasswordException → 400 Bad Request（密码不符合要求）</li>
 *   <li>AccountLockedException → 423 Locked（账号被锁定）</li>
 *   <li>MethodArgumentNotValidException → 400 Bad Request（参数验证失败）</li>
 *   <li>BusinessException → 200 OK（业务异常）</li>
 *   <li>SystemException → 500 Internal Server Error（系统异常）</li>
 *   <li>Exception → 500 Internal Server Error（未知异常）</li>
 * </ul>
 *
 * <p>错误码规范：</p>
 * <ul>
 *   <li>400xxx - 客户端错误</li>
 *   <li>401xxx - 认证错误</li>
 *   <li>403xxx - 权限错误</li>
 *   <li>404xxx - 资源不存在</li>
 *   <li>409xxx - 冲突错误</li>
 *   <li>423xxx - 资源被锁定</li>
 *   <li>500xxx - 服务器内部错误</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-11-24
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ==================== 认证相关异常 (401) ====================

    /**
     * 处理认证失败异常
     *
     * @param e 认证异常
     * @return 401 错误响应
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException e) {
        log.warn("[全局异常处理] 认证失败: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(401001, e.getMessage()));
    }

    /**
     * 处理会话过期异常
     *
     * @param e 会话过期异常
     * @return 401 错误响应
     */
    @ExceptionHandler(SessionExpiredException.class)
    public ResponseEntity<ApiResponse<Void>> handleSessionExpiredException(SessionExpiredException e) {
        log.warn("[全局异常处理] 会话已过期: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(401002, "会话已过期，请重新登录"));
    }

    /**
     * 处理会话不存在异常
     *
     * @param e 会话不存在异常
     * @return 401 错误响应
     */
    @ExceptionHandler(SessionNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleSessionNotFoundException(SessionNotFoundException e) {
        log.warn("[全局异常处理] 会话不存在: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(401003, "会话不存在或已失效，请重新登录"));
    }

    /**
     * 处理Token无效异常
     *
     * @param e Token无效异常
     * @return 401 错误响应
     */
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidTokenException(InvalidTokenException e) {
        log.warn("[全局异常处理] Token无效: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(401004, "Token无效，请重新登录"));
    }

    // ==================== 客户端错误 (400) ====================

    /**
     * 处理密码强度不符合要求异常
     *
     * @param e 密码异常
     * @return 400 错误响应
     */
    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ApiResponse<List<String>>> handleInvalidPasswordException(InvalidPasswordException e) {
        log.warn("[全局异常处理] 密码不符合要求: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400001, "密码不符合强度要求", e.getValidationErrors()));
    }

    /**
     * 处理参数验证失败异常
     *
     * @param e 参数验证异常
     * @return 400 错误响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<List<ErrorDetail>>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e) {
        log.warn("[全局异常处理] 参数验证失败: {} 个字段错误", e.getBindingResult().getFieldErrorCount());

        List<ErrorDetail> errors = new ArrayList<>();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            errors.add(ErrorDetail.of(fieldError.getField(), fieldError.getDefaultMessage()));
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400002, "请求参数无效", errors));
    }

    // ==================== 资源不存在 (404) ====================

    /**
     * 处理账号不存在异常
     *
     * @param e 账号不存在异常
     * @return 404 错误响应
     */
    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccountNotFoundException(AccountNotFoundException e) {
        log.warn("[全局异常处理] 账号不存在: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(404001, e.getMessage()));
    }

    // ==================== 资源冲突 (409) ====================

    /**
     * 处理用户名重复异常
     *
     * @param e 用户名重复异常
     * @return 409 错误响应
     */
    @ExceptionHandler(DuplicateUsernameException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateUsernameException(DuplicateUsernameException e) {
        log.warn("[全局异常处理] 用户名已存在: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(409001, "用户名已存在"));
    }

    /**
     * 处理邮箱重复异常
     *
     * @param e 邮箱重复异常
     * @return 409 错误响应
     */
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateEmailException(DuplicateEmailException e) {
        log.warn("[全局异常处理] 邮箱已存在: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(409002, "邮箱已存在"));
    }

    // ==================== 资源被锁定 (423) ====================

    /**
     * 处理账号锁定异常
     *
     * @param e 账号锁定异常
     * @return 423 错误响应
     */
    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleAccountLockedException(AccountLockedException e) {
        log.warn("[全局异常处理] 账号已锁定: {}", e.getMessage());

        // 构建锁定信息
        Map<String, Object> lockInfo = new HashMap<>();
        lockInfo.put("remainingMinutes", e.getRemainingMinutes());

        return ResponseEntity.status(HttpStatus.LOCKED)
                .body(ApiResponse.error(423001, e.getMessage(), lockInfo));
    }

    // ==================== 业务异常 (200 with error code) ====================

    /**
     * 处理业务异常
     *
     * @param e 业务异常
     * @return 错误响应
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.warn("[全局异常处理] 业务异常: code={}, message={}", e.getErrorCode(), e.getErrorMessage());
        // 业务异常使用200状态码，通过响应体中的code区分
        return ResponseEntity.ok()
                .body(ApiResponse.error(Integer.parseInt(e.getErrorCode()), e.getErrorMessage()));
    }

    // ==================== 系统异常 (500) ====================

    /**
     * 处理系统异常
     *
     * @param e 系统异常
     * @return 500 错误响应
     */
    @ExceptionHandler(SystemException.class)
    public ResponseEntity<ApiResponse<Void>> handleSystemException(SystemException e) {
        log.error("[全局异常处理] 系统异常: code={}, message={}", e.getErrorCode(), e.getErrorMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500001, "系统异常，请稍后重试"));
    }

    /**
     * 处理未知异常
     *
     * @param e 未知异常
     * @return 500 错误响应
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("[全局异常处理] 未知异常", e);
        // 不暴露内部实现细节
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500002, "系统错误，请稍后重试"));
    }
}
