package com.catface996.aiops.application.impl.service.relationship;

import com.catface996.aiops.application.api.dto.common.PageResult;
import com.catface996.aiops.application.api.dto.relationship.*;
import com.catface996.aiops.application.api.dto.relationship.request.CreateRelationshipRequest;
import com.catface996.aiops.application.api.dto.relationship.request.UpdateRelationshipRequest;
import com.catface996.aiops.application.api.service.relationship.RelationshipApplicationService;
import com.catface996.aiops.common.enums.RelationshipErrorCode;
import com.catface996.aiops.common.enums.ResourceErrorCode;
import com.catface996.aiops.common.exception.BusinessException;
import com.catface996.aiops.domain.model.node.Node;
import com.catface996.aiops.domain.model.node2node.Node2Node;
import com.catface996.aiops.domain.model.relationship.*;
import com.catface996.aiops.domain.service.node2node.Node2NodeDomainService;
import com.catface996.aiops.repository.node.NodeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 资源关系应用服务实现
 *
 * <p>注意：内部实现已改为使用 Node2NodeDomainService，但 API 保持不变。</p>
 *
 * @author AI Assistant
 * @since 2025-12-03
 */
@Service
public class RelationshipApplicationServiceImpl implements RelationshipApplicationService {

    private final Node2NodeDomainService node2NodeDomainService;
    private final NodeRepository nodeRepository;

    public RelationshipApplicationServiceImpl(Node2NodeDomainService node2NodeDomainService,
                                               NodeRepository nodeRepository) {
        this.node2NodeDomainService = node2NodeDomainService;
        this.nodeRepository = nodeRepository;
    }

    @Override
    @Transactional
    public RelationshipDTO createRelationship(CreateRelationshipRequest request, Long operatorId) {
        RelationshipType type;
        RelationshipDirection direction;
        RelationshipStrength strength;

        try {
            type = RelationshipType.valueOf(request.getRelationshipType());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(RelationshipErrorCode.INVALID_RELATIONSHIP_TYPE, request.getRelationshipType());
        }
        try {
            direction = RelationshipDirection.valueOf(request.getDirection());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(RelationshipErrorCode.INVALID_RELATIONSHIP_DIRECTION, request.getDirection());
        }
        try {
            strength = RelationshipStrength.valueOf(request.getStrength());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(RelationshipErrorCode.INVALID_RELATIONSHIP_STRENGTH, request.getStrength());
        }

        Node2Node node2Node = node2NodeDomainService.createRelationship(
                request.getSourceResourceId(),
                request.getTargetResourceId(),
                type,
                direction,
                strength,
                request.getDescription(),
                request.getTopologyId(),
                operatorId);

        return toDTO(node2Node);
    }

    @Override
    public PageResult<RelationshipDTO> listRelationships(Long sourceResourceId, Long targetResourceId,
                                                          String relationshipType, String status,
                                                          int pageNum, int pageSize) {
        RelationshipType type = relationshipType != null ? RelationshipType.valueOf(relationshipType) : null;
        RelationshipStatus statusEnum = status != null ? RelationshipStatus.valueOf(status) : null;

        List<Node2Node> relationships = node2NodeDomainService.listRelationships(
                sourceResourceId, targetResourceId, type, statusEnum, pageNum, pageSize);

        long total = node2NodeDomainService.countRelationships(
                sourceResourceId, targetResourceId, type, statusEnum);

        List<RelationshipDTO> dtos = relationships.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return PageResult.of(dtos, pageNum, pageSize, total);
    }

    @Override
    public ResourceRelationshipsDTO getResourceRelationships(Long resourceId) {
        // 获取节点信息
        Node node = nodeRepository.findById(resourceId)
                .orElseThrow(() -> new BusinessException(ResourceErrorCode.RESOURCE_NOT_FOUND, "资源不存在: " + resourceId));

        // 获取上游和下游依赖
        List<Node2Node> upstreams = node2NodeDomainService.getUpstreamDependencies(resourceId);
        List<Node2Node> downstreams = node2NodeDomainService.getDownstreamDependencies(resourceId);

        // 转换为DTO
        List<RelationshipDTO> upstreamDTOs = upstreams.stream().map(this::toDTO).collect(Collectors.toList());
        List<RelationshipDTO> downstreamDTOs = downstreams.stream().map(this::toDTO).collect(Collectors.toList());

        // 按类型分组
        Map<String, List<RelationshipDTO>> upstreamByType = upstreamDTOs.stream()
                .collect(Collectors.groupingBy(RelationshipDTO::getRelationshipType));
        Map<String, List<RelationshipDTO>> downstreamByType = downstreamDTOs.stream()
                .collect(Collectors.groupingBy(RelationshipDTO::getRelationshipType));

        return ResourceRelationshipsDTO.builder()
                .resourceId(resourceId)
                .resourceName(node.getName())
                .upstreamDependencies(upstreamDTOs)
                .downstreamDependencies(downstreamDTOs)
                .upstreamByType(upstreamByType)
                .downstreamByType(downstreamByType)
                .upstreamCount(upstreamDTOs.size())
                .downstreamCount(downstreamDTOs.size())
                .build();
    }

    @Override
    public RelationshipDTO getRelationshipById(Long relationshipId) {
        Node2Node node2Node = node2NodeDomainService.getRelationshipById(relationshipId)
                .orElseThrow(() -> new BusinessException(RelationshipErrorCode.RELATIONSHIP_NOT_FOUND, relationshipId));
        return toDTO(node2Node);
    }

    @Override
    @Transactional
    public RelationshipDTO updateRelationship(Long relationshipId, UpdateRelationshipRequest request,
                                               Long operatorId) {
        RelationshipType type = request.getRelationshipType() != null ?
                RelationshipType.valueOf(request.getRelationshipType()) : null;
        RelationshipStrength strength = request.getStrength() != null ?
                RelationshipStrength.valueOf(request.getStrength()) : null;
        RelationshipStatus status = request.getStatus() != null ?
                RelationshipStatus.valueOf(request.getStatus()) : null;

        Node2Node node2Node = node2NodeDomainService.updateRelationship(
                relationshipId, type, strength, status, request.getDescription(), operatorId);

        return toDTO(node2Node);
    }

    @Override
    @Transactional
    public void deleteRelationship(Long relationshipId, Long operatorId) {
        node2NodeDomainService.deleteRelationship(relationshipId, operatorId);
    }

    @Override
    public CycleDetectionDTO detectCycle(Long resourceId) {
        CycleDetectionResult result = node2NodeDomainService.detectCycle(resourceId);

        return CycleDetectionDTO.builder()
                .hasCycle(result.hasCycle())
                .cyclePath(result.getCyclePath())
                .message(result.hasCycle() ? "检测到循环依赖" : "未检测到循环依赖")
                .build();
    }

    @Override
    public TraverseDTO traverse(Long resourceId, int maxDepth) {
        TraverseResult result = node2NodeDomainService.traverse(resourceId, maxDepth);

        List<RelationshipDTO> relationshipDTOs = result.getRelationships().stream()
                .map(this::toDTOFromRelationship)
                .collect(Collectors.toList());

        return TraverseDTO.builder()
                .startResourceId(result.getStartResourceId())
                .nodesByLevel(result.getNodesByLevel())
                .relationships(relationshipDTOs)
                .actualDepth(result.getActualDepth())
                .totalNodes(result.getTotalNodes())
                .build();
    }

    /**
     * 将 Node2Node 领域对象转换为 DTO
     */
    private RelationshipDTO toDTO(Node2Node node2Node) {
        return RelationshipDTO.builder()
                .id(node2Node.getId())
                .sourceResourceId(node2Node.getSourceId())
                .sourceResourceName(node2Node.getSourceName())
                .targetResourceId(node2Node.getTargetId())
                .targetResourceName(node2Node.getTargetName())
                .relationshipType(node2Node.getRelationshipType() != null ?
                        node2Node.getRelationshipType().name() : null)
                .relationshipTypeDesc(node2Node.getRelationshipType() != null ?
                        node2Node.getRelationshipType().getDescription() : null)
                .direction(node2Node.getDirection() != null ?
                        node2Node.getDirection().name() : null)
                .directionDesc(node2Node.getDirection() != null ?
                        node2Node.getDirection().getDescription() : null)
                .strength(node2Node.getStrength() != null ?
                        node2Node.getStrength().name() : null)
                .strengthDesc(node2Node.getStrength() != null ?
                        node2Node.getStrength().getDescription() : null)
                .status(node2Node.getStatus() != null ?
                        node2Node.getStatus().name() : null)
                .statusDesc(node2Node.getStatus() != null ?
                        node2Node.getStatus().getDescription() : null)
                .description(node2Node.getDescription())
                .createdAt(node2Node.getCreatedAt())
                .updatedAt(node2Node.getUpdatedAt())
                .build();
    }

    /**
     * 将 Relationship 领域对象转换为 DTO（用于 TraverseResult 兼容）
     */
    private RelationshipDTO toDTOFromRelationship(Relationship relationship) {
        return RelationshipDTO.builder()
                .id(relationship.getId())
                .sourceResourceId(relationship.getSourceResourceId())
                .sourceResourceName(relationship.getSourceResourceName())
                .targetResourceId(relationship.getTargetResourceId())
                .targetResourceName(relationship.getTargetResourceName())
                .relationshipType(relationship.getRelationshipType() != null ?
                        relationship.getRelationshipType().name() : null)
                .relationshipTypeDesc(relationship.getRelationshipType() != null ?
                        relationship.getRelationshipType().getDescription() : null)
                .direction(relationship.getDirection() != null ?
                        relationship.getDirection().name() : null)
                .directionDesc(relationship.getDirection() != null ?
                        relationship.getDirection().getDescription() : null)
                .strength(relationship.getStrength() != null ?
                        relationship.getStrength().name() : null)
                .strengthDesc(relationship.getStrength() != null ?
                        relationship.getStrength().getDescription() : null)
                .status(relationship.getStatus() != null ?
                        relationship.getStatus().name() : null)
                .statusDesc(relationship.getStatus() != null ?
                        relationship.getStatus().getDescription() : null)
                .description(relationship.getDescription())
                .createdAt(relationship.getCreatedAt())
                .updatedAt(relationship.getUpdatedAt())
                .build();
    }
}
