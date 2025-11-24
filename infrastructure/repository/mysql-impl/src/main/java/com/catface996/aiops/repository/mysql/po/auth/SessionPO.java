package com.catface996.aiops.repository.mysql.po.auth;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 会话持久化对象
 * 
 * <p>数据库表 t_session 的映射对象</p>
 * <p>用于Redis不可用时的降级方案</p>
 * 
 * @author AI Assistant
 * @since 2025-01-23
 */
@Data
@TableName("t_session")
public class SessionPO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 会话ID（UUID）
     */
    @TableId(value = "id", type = IdType.INPUT)
    private String id;
    
    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;
    
    /**
     * JWT Token
     */
    @TableField("token")
    private String token;
    
    /**
     * 过期时间
     */
    @TableField("expires_at")
    private LocalDateTime expiresAt;
    
    /**
     * 设备信息（JSON格式）
     */
    @TableField("device_info")
    private String deviceInfo;
    
    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
