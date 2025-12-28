package com.catface996.aiops.repository.node;

import com.catface996.aiops.domain.model.node2node.Node2Node;
import com.catface996.aiops.domain.model.relationship.RelationshipStatus;
import com.catface996.aiops.domain.model.relationship.RelationshipType;

import java.util.List;
import java.util.Optional;

/**
 * 节点关系仓储接口
 *
 * <p>提供节点之间关系（边）的数据访问操作。</p>
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
public interface Node2NodeRepository {

    /**
     * 节点关系信息（用于拓扑图查询）
     */
    record RelationshipInfo(
            Long id,
            Long sourceId,
            Long targetId,
            String relationshipType,
            String direction,
            String strength,
            String status,
            String description
    ) {}

    // ==================== 拓扑图查询方法（原有）====================

    /**
     * 查询指定节点集合之间的关系
     *
     * @param nodeIds 节点ID列表
     * @return 关系列表
     */
    List<RelationshipInfo> findRelationshipsByNodeIds(List<Long> nodeIds);

    /**
     * 查询节点的出边关系（RelationshipInfo 版本，用于拓扑图）
     *
     * @param sourceId 源节点ID
     * @return 关系列表
     */
    List<RelationshipInfo> findOutgoingBySourceId(Long sourceId);

    /**
     * 查询节点的入边关系（RelationshipInfo 版本，用于拓扑图）
     *
     * @param targetId 目标节点ID
     * @return 关系列表
     */
    List<RelationshipInfo> findIncomingByTargetId(Long targetId);

    /**
     * 删除节点相关的所有关系
     *
     * @param nodeId 节点ID
     * @return 删除的记录数
     */
    int deleteByNodeId(Long nodeId);

    // ==================== 完整 CRUD 方法（新增）====================

    /**
     * 保存节点关系
     *
     * @param node2Node 关系实体
     * @return 保存后的关系实体（含ID）
     */
    Node2Node save(Node2Node node2Node);

    /**
     * 更新节点关系
     *
     * @param node2Node 关系实体
     * @return 更新后的关系实体
     */
    Node2Node update(Node2Node node2Node);

    /**
     * 根据ID查询关系
     *
     * @param id 关系ID
     * @return 关系实体
     */
    Optional<Node2Node> findById(Long id);

    /**
     * 根据ID删除关系
     *
     * @param id 关系ID
     */
    void deleteById(Long id);

    /**
     * 检查关系是否已存在
     *
     * @param sourceId 源节点ID
     * @param targetId 目标节点ID
     * @param type 关系类型
     * @return 是否存在
     */
    boolean existsBySourceAndTargetAndType(Long sourceId, Long targetId, RelationshipType type);

    /**
     * 根据条件分页查询关系
     *
     * @param sourceId 源节点ID（可选）
     * @param targetId 目标节点ID（可选）
     * @param type 关系类型（可选）
     * @param status 关系状态（可选）
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 关系列表
     */
    List<Node2Node> findByConditions(Long sourceId, Long targetId,
                                      RelationshipType type, RelationshipStatus status,
                                      int pageNum, int pageSize);

    /**
     * 根据条件统计关系数量
     *
     * @param sourceId 源节点ID（可选）
     * @param targetId 目标节点ID（可选）
     * @param type 关系类型（可选）
     * @param status 关系状态（可选）
     * @return 数量
     */
    long countByConditions(Long sourceId, Long targetId,
                           RelationshipType type, RelationshipStatus status);

    /**
     * 查询源节点的所有关系（Node2Node 版本）
     *
     * @param sourceId 源节点ID
     * @return 关系列表
     */
    List<Node2Node> findBySourceId(Long sourceId);

    /**
     * 查询目标节点的所有关系（Node2Node 版本）
     *
     * @param targetId 目标节点ID
     * @return 关系列表
     */
    List<Node2Node> findByTargetId(Long targetId);

    /**
     * 删除指定的关系（用于双向关系删除）
     *
     * @param sourceId 源节点ID
     * @param targetId 目标节点ID
     * @param type 关系类型
     */
    void deleteBySourceAndTargetAndType(Long sourceId, Long targetId, RelationshipType type);
}
