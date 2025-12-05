package com.catface996.aiops.repository.mysql.mapper.subgraph;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.catface996.aiops.repository.mysql.po.subgraph.SubgraphPermissionPO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 子图权限 Mapper 接口
 *
 * <p>提供子图权限数据的数据库访问操作</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F08: 子图管理功能</li>
 *   <li>需求1.2: 创建者自动成为第一个 Owner</li>
 *   <li>需求3.5: Owner 可以添加或移除其他用户</li>
 *   <li>需求3.6: 不能移除最后一个 Owner</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-04
 */
public interface SubgraphPermissionMapper extends BaseMapper<SubgraphPermissionPO> {

    /**
     * 根据子图ID查询所有权限记录
     *
     * @param subgraphId 子图ID
     * @return 权限列表
     */
    List<SubgraphPermissionPO> selectBySubgraphId(@Param("subgraphId") Long subgraphId);

    /**
     * 根据子图ID和用户ID查询权限记录
     *
     * @param subgraphId 子图ID
     * @param userId 用户ID
     * @return 权限PO对象
     */
    SubgraphPermissionPO selectBySubgraphIdAndUserId(@Param("subgraphId") Long subgraphId,
                                                      @Param("userId") Long userId);

    /**
     * 统计子图的Owner数量
     *
     * @param subgraphId 子图ID
     * @return Owner数量
     */
    int countOwnersBySubgraphId(@Param("subgraphId") Long subgraphId);

    /**
     * 删除权限记录
     *
     * @param subgraphId 子图ID
     * @param userId 用户ID
     * @return 删除的行数
     */
    int deleteBySubgraphIdAndUserId(@Param("subgraphId") Long subgraphId,
                                     @Param("userId") Long userId);

    /**
     * 检查用户是否有指定角色的权限
     *
     * @param subgraphId 子图ID
     * @param userId 用户ID
     * @param role 权限角色
     * @return 存在返回1，不存在返回0
     */
    int hasPermission(@Param("subgraphId") Long subgraphId,
                      @Param("userId") Long userId,
                      @Param("role") String role);

    /**
     * 检查用户是否有任何权限
     *
     * @param subgraphId 子图ID
     * @param userId 用户ID
     * @return 存在返回1，不存在返回0
     */
    int hasAnyPermission(@Param("subgraphId") Long subgraphId, @Param("userId") Long userId);
}
