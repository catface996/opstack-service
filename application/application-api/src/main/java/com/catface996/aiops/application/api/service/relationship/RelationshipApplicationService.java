package com.catface996.aiops.application.api.service.relationship;

import com.catface996.aiops.application.api.dto.common.PageResult;
import com.catface996.aiops.application.api.dto.relationship.*;
import com.catface996.aiops.application.api.dto.relationship.request.CreateRelationshipRequest;
import com.catface996.aiops.application.api.dto.relationship.request.UpdateRelationshipRequest;

/**
 * 资源关系应用服务接口
 *
 * <p>提供资源关系管理的应用层服务，协调领域层和基础设施层。</p>
 *
 * @author AI Assistant
 * @since 2025-12-03
 */
public interface RelationshipApplicationService {

    /**
     * 创建关系
     *
     * @param request 创建关系请求
     * @param operatorId 操作人ID
     * @return 创建的关系DTO
     */
    RelationshipDTO createRelationship(CreateRelationshipRequest request, Long operatorId);

    /**
     * 查询关系列表（分页）
     *
     * @param sourceResourceId 源资源ID（可选）
     * @param targetResourceId 目标资源ID（可选）
     * @param relationshipType 关系类型（可选）
     * @param status 关系状态（可选）
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    PageResult<RelationshipDTO> listRelationships(Long sourceResourceId, Long targetResourceId,
                                                   String relationshipType, String status,
                                                   int pageNum, int pageSize);

    /**
     * 获取资源的所有关系
     *
     * @param resourceId 资源ID
     * @return 资源关系汇总DTO
     */
    ResourceRelationshipsDTO getResourceRelationships(Long resourceId);

    /**
     * 根据ID获取关系详情
     *
     * @param relationshipId 关系ID
     * @return 关系DTO
     */
    RelationshipDTO getRelationshipById(Long relationshipId);

    /**
     * 更新关系
     *
     * @param relationshipId 关系ID
     * @param request 更新请求
     * @param operatorId 操作人ID
     * @return 更新后的关系DTO
     */
    RelationshipDTO updateRelationship(Long relationshipId, UpdateRelationshipRequest request,
                                        Long operatorId);

    /**
     * 删除关系
     *
     * @param relationshipId 关系ID
     * @param operatorId 操作人ID
     */
    void deleteRelationship(Long relationshipId, Long operatorId);

    /**
     * 检测循环依赖
     *
     * @param resourceId 资源ID
     * @return 循环依赖检测结果
     */
    CycleDetectionDTO detectCycle(Long resourceId);

    /**
     * 广度优先遍历
     *
     * @param resourceId 起始资源ID
     * @param maxDepth 最大深度
     * @return 遍历结果
     */
    TraverseDTO traverse(Long resourceId, int maxDepth);
}
