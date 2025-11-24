package com.catface996.aiops.repository.mysql.po.auth;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 账号持久化对象
 * 
 * <p>数据库表 t_account 的映射对象</p>
 * 
 * @author AI Assistant
 * @since 2025-01-23
 */
@Data
@TableName("t_account")
public class AccountPO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 账号ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户名（3-20个字符，字母数字下划线）
     */
    @TableField("username")
    private String username;
    
    /**
     * 邮箱（最大100字符）
     */
    @TableField("email")
    private String email;
    
    /**
     * 加密后的密码（BCrypt加密，60字符）
     */
    @TableField("password")
    private String password;
    
    /**
     * 角色（ROLE_USER, ROLE_ADMIN）
     */
    @TableField("role")
    private String role;
    
    /**
     * 账号状态（ACTIVE, LOCKED, DISABLED）
     */
    @TableField("status")
    private String status;
    
    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
