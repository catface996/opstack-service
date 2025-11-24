package com.catface996.aiops.repository.mysql.mapper.auth;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.catface996.aiops.repository.mysql.po.auth.SessionPO;
import org.apache.ibatis.annotations.Param;

/**
 * 会话 Mapper 接口
 * 
 * <p>提供会话数据的数据库访问操作</p>
 * <p>继承 MyBatis-Plus BaseMapper，自动提供基础 CRUD 方法</p>
 * 
 * @author AI Assistant
 * @since 2025-01-23
 */
public interface SessionMapper extends BaseMapper<SessionPO> {
    
    /**
     * 根据用户ID查询会话
     * 
     * @param userId 用户ID
     * @return 会话PO对象，如果不存在返回null
     */
    SessionPO selectByUserId(@Param("userId") Long userId);
    
    /**
     * 根据用户ID删除会话
     * 
     * @param userId 用户ID
     * @return 删除的记录数
     */
    int deleteByUserId(@Param("userId") Long userId);
    
    /**
     * 删除所有过期会话
     * 
     * @return 删除的记录数
     */
    int deleteExpiredSessions();
}
