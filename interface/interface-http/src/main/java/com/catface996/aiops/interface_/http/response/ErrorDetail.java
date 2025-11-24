package com.catface996.aiops.interface_.http.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 错误详情
 *
 * <p>用于表示字段验证错误的详细信息。</p>
 *
 * <p>示例：</p>
 * <pre>
 * {
 *   "field": "password",
 *   "message": "密码长度至少为8个字符"
 * }
 * </pre>
 *
 * @author AI Assistant
 * @since 2025-11-24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 错误字段名
     */
    private String field;

    /**
     * 错误消息
     */
    private String message;

    /**
     * 创建错误详情
     *
     * @param field   字段名
     * @param message 错误消息
     * @return 错误详情对象
     */
    public static ErrorDetail of(String field, String message) {
        return new ErrorDetail(field, message);
    }
}
