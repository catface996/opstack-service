package com.catface996.aiops.repository.relationship;

import com.catface996.aiops.domain.model.relationship.Relationship;
import com.catface996.aiops.domain.model.relationship.RelationshipStatus;
import com.catface996.aiops.domain.model.relationship.RelationshipType;

import java.util.List;
import java.util.Optional;

/**
 * 资源关系仓储接口
 *
 * <p>提供资源关系实体的数据访问操作，遵循DDD仓储模式。</p>
 * <p>仓储负责领域对象的持久化和重建，隔离领域层与基础设施层。</p>
 *
 * @author AI Assistant
 * @since 2025-12-03
 */
public interface RelationshipRepository {

    /**
     * 保存关系
     *
     * @param relationship 关系实体
     * @return 保存后的关系实体（包含生成的ID）
     * @throws IllegalArgumentException 如果relationship为null
     */
    Relationship save(Relationship relationship);

    /**
     * 更新关系
     *
     * @param relationship 关系实体
     * @return 更新后的关系实体
     * @throws IllegalArgumentException 如果relationship为null或relationship.id为null
     */
    Relationship update(Relationship relationship);

    /**
     * 根据ID查询关系
     *
     * @param id 关系ID
     * @return 关系实体（如果存在）
     * @throws IllegalArgumentException 如果id为null
     */
    Optional<Relationship> findById(Long id);

    /**
     * 根据源资源ID查询关系（下游依赖查询）
     *
     * @param sourceResourceId 源资源ID
     * @return 关系列表
     */
    List<Relationship> findBySourceResourceId(Long sourceResourceId);

    /**
     * 根据目标资源ID查询关系（上游依赖查询）
     *
     * @param targetResourceId 目标资源ID
     * @return 关系列表
     */
    List<Relationship> findByTargetResourceId(Long targetResourceId);

    /**
     * 分页查询关系列表（支持多条件筛选）
     *
     * @param sourceResourceId 源资源ID（可选，为null则不过滤）
     * @param targetResourceId 目标资源ID（可选，为null则不过滤）
     * @param type 关系类型（可选，为null则不过滤）
     * @param status 关系状态（可选，为null则不过滤）
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页大小
     * @return 关系列表
     */
    List<Relationship> findByConditions(Long sourceResourceId, Long targetResourceId,
                                         RelationshipType type, RelationshipStatus status,
                                         int pageNum, int pageSize);

    /**
     * 按条件统计关系数量
     *
     * @param sourceResourceId 源资源ID（可选）
     * @param targetResourceId 目标资源ID（可选）
     * @param type 关系类型（可选）
     * @param status 关系状态（可选）
     * @return 关系数量
     */
    long countByConditions(Long sourceResourceId, Long targetResourceId,
                           RelationshipType type, RelationshipStatus status);

    /**
     * 检查关系是否已存在
     *
     * @param sourceResourceId 源资源ID
     * @param targetResourceId 目标资源ID
     * @param type 关系类型
     * @return true if relationship exists
     */
    boolean existsBySourceAndTargetAndType(Long sourceResourceId, Long targetResourceId,
                                            RelationshipType type);

    /**
     * 根据ID删除关系
     *
     * @param id 关系ID
     * @throws IllegalArgumentException 如果id为null
     */
    void deleteById(Long id);

    /**
     * 根据源资源ID、目标资源ID和类型删除关系
     *
     * @param sourceResourceId 源资源ID
     * @param targetResourceId 目标资源ID
     * @param type 关系类型
     */
    void deleteBySourceAndTargetAndType(Long sourceResourceId, Long targetResourceId,
                                         RelationshipType type);

    /**
     * 删除与指定资源关联的所有关系（级联删除）
     *
     * @param resourceId 资源ID
     */
    void deleteByResourceId(Long resourceId);

    /**
     * 统计所有关系数量
     *
     * @return 关系总数
     */
    long count();

    /**
     * 批量查询多个资源的下游关系（用于图遍历优化）
     *
     * @param sourceResourceIds 源资源ID列表
     * @return 关系列表
     */
    List<Relationship> findBySourceResourceIds(List<Long> sourceResourceIds);
}
