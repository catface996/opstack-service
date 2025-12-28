package com.catface996.aiops.domain.service.node2node;

import com.catface996.aiops.domain.model.node2node.Node2Node;
import com.catface996.aiops.domain.model.relationship.*;

import java.util.List;
import java.util.Optional;

/**
 * 节点关系领域服务接口
 *
 * <p>提供节点关系管理的核心业务逻辑，包括：</p>
 * <ul>
 *   <li>关系创建：验证、双向处理、保存</li>
 *   <li>关系查询：分页、条件过滤、上下游依赖</li>
 *   <li>关系更新：权限检查、属性更新</li>
 *   <li>关系删除：权限检查、双向删除</li>
 *   <li>图遍历：循环依赖检测、广度优先遍历</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
public interface Node2NodeDomainService {

    /**
     * 创建节点关系
     *
     * @param sourceId 源节点ID
     * @param targetId 目标节点ID
     * @param type 关系类型
     * @param direction 关系方向
     * @param strength 关系强度
     * @param description 关系描述
     * @param topologyId 拓扑ID（用于权限验证）
     * @param operatorId 操作人ID
     * @return 创建的关系实体
     */
    Node2Node createRelationship(Long sourceId, Long targetId,
                                  RelationshipType type, RelationshipDirection direction,
                                  RelationshipStrength strength, String description,
                                  Long topologyId, Long operatorId);

    /**
     * 分页查询关系列表
     *
     * @param sourceId 源节点ID（可选）
     * @param targetId 目标节点ID（可选）
     * @param type 关系类型（可选）
     * @param status 关系状态（可选）
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页大小
     * @return 关系列表
     */
    List<Node2Node> listRelationships(Long sourceId, Long targetId,
                                       RelationshipType type, RelationshipStatus status,
                                       int pageNum, int pageSize);

    /**
     * 统计关系数量
     *
     * @param sourceId 源节点ID（可选）
     * @param targetId 目标节点ID（可选）
     * @param type 关系类型（可选）
     * @param status 关系状态（可选）
     * @return 关系数量
     */
    long countRelationships(Long sourceId, Long targetId,
                            RelationshipType type, RelationshipStatus status);

    /**
     * 根据ID获取关系详情
     *
     * @param relationshipId 关系ID
     * @return 关系实体（如果存在）
     */
    Optional<Node2Node> getRelationshipById(Long relationshipId);

    /**
     * 获取节点的上游依赖（指向该节点的关系）
     *
     * @param nodeId 节点ID
     * @return 上游依赖列表
     */
    List<Node2Node> getUpstreamDependencies(Long nodeId);

    /**
     * 获取节点的下游依赖（从该节点发起的关系）
     *
     * @param nodeId 节点ID
     * @return 下游依赖列表
     */
    List<Node2Node> getDownstreamDependencies(Long nodeId);

    /**
     * 更新关系
     *
     * @param relationshipId 关系ID
     * @param type 新关系类型（可选，null表示不修改）
     * @param strength 新关系强度（可选）
     * @param status 新关系状态（可选）
     * @param description 新关系描述（可选）
     * @param operatorId 操作人ID
     * @return 更新后的关系实体
     */
    Node2Node updateRelationship(Long relationshipId, RelationshipType type,
                                  RelationshipStrength strength, RelationshipStatus status,
                                  String description, Long operatorId);

    /**
     * 删除关系
     *
     * @param relationshipId 关系ID
     * @param operatorId 操作人ID
     */
    void deleteRelationship(Long relationshipId, Long operatorId);

    /**
     * 删除节点的所有关系（级联删除）
     *
     * @param nodeId 节点ID
     */
    void deleteRelationshipsByNode(Long nodeId);

    /**
     * 检测循环依赖
     *
     * @param nodeId 起始节点ID
     * @return 循环依赖检测结果
     */
    CycleDetectionResult detectCycle(Long nodeId);

    /**
     * 广度优先遍历
     *
     * @param nodeId 起始节点ID
     * @param maxDepth 最大遍历深度
     * @return 遍历结果
     */
    TraverseResult traverse(Long nodeId, int maxDepth);
}
