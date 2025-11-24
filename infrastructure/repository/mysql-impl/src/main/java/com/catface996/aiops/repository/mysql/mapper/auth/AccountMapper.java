package com.catface996.aiops.repository.mysql.mapper.auth;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.catface996.aiops.repository.mysql.po.auth.AccountPO;
import org.apache.ibatis.annotations.Param;

/**
 * 账号 Mapper 接口
 * 
 * <p>提供账号数据的数据库访问操作</p>
 * <p>继承 MyBatis-Plus BaseMapper，自动提供基础 CRUD 方法</p>
 * 
 * @author AI Assistant
 * @since 2025-01-23
 */
public interface AccountMapper extends BaseMapper<AccountPO> {
    
    /**
     * 根据用户名查询账号
     * 
     * @param username 用户名
     * @return 账号PO对象，如果不存在返回null
     */
    AccountPO selectByUsername(@Param("username") String username);
    
    /**
     * 根据邮箱查询账号
     * 
     * @param email 邮箱
     * @return 账号PO对象，如果不存在返回null
     */
    AccountPO selectByEmail(@Param("email") String email);
}
