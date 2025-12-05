package com.catface996.aiops.repository.subgraph;

import com.catface996.aiops.domain.model.subgraph.PermissionRole;
import com.catface996.aiops.domain.model.subgraph.Subgraph;
import com.catface996.aiops.domain.model.subgraph.SubgraphPermission;

import java.util.List;
import java.util.Optional;

/**
 * 子图仓储接口
 *
 * <p>提供子图实体的数据访问操作，遵循DDD仓储模式。</p>
 * <p>仓储负责领域对象的持久化和重建，隔离领域层与基础设施层。</p>
 *
 * <p>实现说明：</p>
 * <ul>
 *   <li>使用MyBatis实现数据访问</li>
 *   <li>数据存储在MySQL数据库</li>
 *   <li>支持乐观锁（version字段）</li>
 *   <li>支持事务管理</li>
 * </ul>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F08: 子图管理功能</li>
 *   <li>需求1: 子图创建</li>
 *   <li>需求2: 子图列表视图</li>
 *   <li>需求3: 子图信息编辑</li>
 *   <li>需求4: 子图删除</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-04
 */
public interface SubgraphRepository {

    /**
     * 保存子图
     *
     * @param subgraph 子图实体
     * @return 保存后的子图实体（包含生成的ID）
     * @throws IllegalArgumentException 如果subgraph为null
     */
    Subgraph save(Subgraph subgraph);

    /**
     * 根据ID查询子图
     *
     * @param id 子图ID
     * @return 子图实体（如果存在）
     * @throws IllegalArgumentException 如果id为null
     */
    Optional<Subgraph> findById(Long id);

    /**
     * 根据名称查询子图
     *
     * @param name 子图名称
     * @return 子图实体（如果存在）
     */
    Optional<Subgraph> findByName(String name);

    /**
     * 查询用户有权限访问的子图列表
     * <p>通过关联权限表查询用户作为Owner或Viewer的所有子图</p>
     *
     * @param userId 用户ID
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 子图列表
     */
    List<Subgraph> findByUserId(Long userId, int page, int size);

    /**
     * 统计用户有权限访问的子图数量
     *
     * @param userId 用户ID
     * @return 子图数量
     */
    long countByUserId(Long userId);

    /**
     * 按关键词搜索子图（名称和描述）
     * <p>使用MySQL FULLTEXT索引进行全文搜索</p>
     *
     * @param keyword 搜索关键词
     * @param userId 当前用户ID（用于权限过滤）
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 子图列表
     */
    List<Subgraph> searchByKeyword(String keyword, Long userId, int page, int size);

    /**
     * 统计按关键词搜索的子图数量
     *
     * @param keyword 搜索关键词
     * @param userId 当前用户ID（用于权限过滤）
     * @return 子图数量
     */
    long countByKeyword(String keyword, Long userId);

    /**
     * 按标签过滤子图
     *
     * @param tags 标签列表
     * @param userId 当前用户ID（用于权限过滤）
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 子图列表
     */
    List<Subgraph> filterByTags(List<String> tags, Long userId, int page, int size);

    /**
     * 统计按标签过滤的子图数量
     *
     * @param tags 标签列表
     * @param userId 当前用户ID（用于权限过滤）
     * @return 子图数量
     */
    long countByTags(List<String> tags, Long userId);

    /**
     * 按所有者过滤子图
     *
     * @param ownerId 所有者用户ID
     * @param currentUserId 当前用户ID（用于权限过滤）
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 子图列表
     */
    List<Subgraph> filterByOwner(Long ownerId, Long currentUserId, int page, int size);

    /**
     * 统计按所有者过滤的子图数量
     *
     * @param ownerId 所有者用户ID
     * @param currentUserId 当前用户ID（用于权限过滤）
     * @return 子图数量
     */
    long countByOwner(Long ownerId, Long currentUserId);

    /**
     * 更新子图（使用乐观锁）
     *
     * @param subgraph 子图实体
     * @return 更新是否成功（乐观锁冲突时返回false）
     * @throws IllegalArgumentException 如果subgraph为null或subgraph.id为null
     */
    boolean update(Subgraph subgraph);

    /**
     * 删除子图
     *
     * @param id 子图ID
     * @throws IllegalArgumentException 如果id为null
     */
    void delete(Long id);

    /**
     * 检查子图是否存在
     *
     * @param id 子图ID
     * @return true if subgraph exists
     */
    boolean existsById(Long id);

    /**
     * 检查子图名称是否已存在
     *
     * @param name 子图名称
     * @return true if name exists
     */
    boolean existsByName(String name);

    /**
     * 检查子图名称是否已存在（排除指定ID）
     * <p>用于更新时检查名称唯一性</p>
     *
     * @param name 子图名称
     * @param excludeId 要排除的子图ID
     * @return true if name exists (excluding the specified ID)
     */
    boolean existsByNameExcludeId(String name, Long excludeId);

    // ==================== 权限相关操作 ====================

    /**
     * 保存权限记录
     *
     * @param permission 权限实体
     * @return 保存后的权限实体（包含生成的ID）
     */
    SubgraphPermission savePermission(SubgraphPermission permission);

    /**
     * 根据子图ID查询所有权限记录
     *
     * @param subgraphId 子图ID
     * @return 权限列表
     */
    List<SubgraphPermission> findPermissionsBySubgraphId(Long subgraphId);

    /**
     * 根据子图ID和用户ID查询权限记录
     *
     * @param subgraphId 子图ID
     * @param userId 用户ID
     * @return 权限实体（如果存在）
     */
    Optional<SubgraphPermission> findPermissionBySubgraphIdAndUserId(Long subgraphId, Long userId);

    /**
     * 统计子图的Owner数量
     *
     * @param subgraphId 子图ID
     * @return Owner数量
     */
    int countOwnersBySubgraphId(Long subgraphId);

    /**
     * 删除权限记录
     *
     * @param subgraphId 子图ID
     * @param userId 用户ID
     */
    void deletePermission(Long subgraphId, Long userId);

    /**
     * 检查用户是否对子图有指定角色的权限
     *
     * @param subgraphId 子图ID
     * @param userId 用户ID
     * @param role 权限角色
     * @return true 如果用户有该角色权限
     */
    boolean hasPermission(Long subgraphId, Long userId, PermissionRole role);

    /**
     * 检查用户是否对子图有任何权限（Owner或Viewer）
     *
     * @param subgraphId 子图ID
     * @param userId 用户ID
     * @return true 如果用户有任何权限
     */
    boolean hasAnyPermission(Long subgraphId, Long userId);
}
