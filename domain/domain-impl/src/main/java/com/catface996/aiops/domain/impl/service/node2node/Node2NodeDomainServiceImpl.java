package com.catface996.aiops.domain.impl.service.node2node;

import com.catface996.aiops.common.enums.RelationshipErrorCode;
import com.catface996.aiops.common.enums.ResourceErrorCode;
import com.catface996.aiops.common.exception.BusinessException;
import com.catface996.aiops.domain.model.node.Node;
import com.catface996.aiops.domain.model.node2node.Node2Node;
import com.catface996.aiops.domain.model.relationship.*;
import com.catface996.aiops.domain.model.topology.Topology;
import com.catface996.aiops.domain.service.node2node.Node2NodeDomainService;
import com.catface996.aiops.repository.node.Node2NodeRepository;
import com.catface996.aiops.repository.node.NodeRepository;
import com.catface996.aiops.repository.topology2.TopologyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 节点关系领域服务实现
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
@Service
public class Node2NodeDomainServiceImpl implements Node2NodeDomainService {

    private static final Logger log = LoggerFactory.getLogger(Node2NodeDomainServiceImpl.class);

    private static final int DEFAULT_MAX_DEPTH = 10;
    private static final int MAX_TRAVERSE_NODES = 1000;

    private final Node2NodeRepository node2NodeRepository;
    private final NodeRepository nodeRepository;
    private final TopologyRepository topologyRepository;

    public Node2NodeDomainServiceImpl(Node2NodeRepository node2NodeRepository,
                                       NodeRepository nodeRepository,
                                       TopologyRepository topologyRepository) {
        this.node2NodeRepository = node2NodeRepository;
        this.nodeRepository = nodeRepository;
        this.topologyRepository = topologyRepository;
    }

    @Override
    @Transactional
    public Node2Node createRelationship(Long sourceId, Long targetId,
                                         RelationshipType type, RelationshipDirection direction,
                                         RelationshipStrength strength, String description,
                                         Long topologyId, Long operatorId) {
        // 1. 参数验证
        if (sourceId == null || targetId == null) {
            throw new BusinessException(RelationshipErrorCode.SOURCE_RESOURCE_NOT_FOUND, "源节点ID和目标节点ID不能为空");
        }
        if (sourceId.equals(targetId)) {
            throw new BusinessException(RelationshipErrorCode.SELF_REFERENCE_NOT_ALLOWED);
        }
        if (type == null || direction == null || strength == null) {
            throw new BusinessException(RelationshipErrorCode.INVALID_RELATIONSHIP_TYPE, "关系类型、方向和强度不能为空");
        }

        // 2. 验证源节点存在
        Node sourceNode = nodeRepository.findById(sourceId)
                .orElseThrow(() -> new BusinessException(RelationshipErrorCode.SOURCE_RESOURCE_NOT_FOUND, sourceId));

        // 3. 验证目标节点存在
        Node targetNode = nodeRepository.findById(targetId)
                .orElseThrow(() -> new BusinessException(RelationshipErrorCode.TARGET_RESOURCE_NOT_FOUND, targetId));

        // 4. 验证拓扑存在和用户权限
        Topology topology = topologyRepository.findById(topologyId)
                .orElseThrow(() -> new BusinessException(ResourceErrorCode.RESOURCE_NOT_FOUND, "拓扑不存在: " + topologyId));

        if (!topology.isOwner(operatorId)) {
            throw new BusinessException(ResourceErrorCode.FORBIDDEN);
        }

        // 5. 检查关系是否已存在
        if (node2NodeRepository.existsBySourceAndTargetAndType(sourceId, targetId, type)) {
            throw new BusinessException(RelationshipErrorCode.RELATIONSHIP_ALREADY_EXISTS,
                    sourceId, targetId, type.name());
        }

        // 6. 创建关系
        Node2Node node2Node = Node2Node.create(sourceId, targetId, type, direction, strength, description);
        node2Node = node2NodeRepository.save(node2Node);

        log.info("关系创建成功: {} -> {} [{}]", sourceId, targetId, type);

        // 7. 如果是双向关系，创建反向关系
        if (direction.isBidirectional()) {
            if (!node2NodeRepository.existsBySourceAndTargetAndType(targetId, sourceId, type)) {
                Node2Node reverseRelationship = node2Node.createReverseRelationship();
                node2NodeRepository.save(reverseRelationship);
                log.info("反向关系创建成功: {} -> {} [{}]", targetId, sourceId, type);
            }
        }

        // 设置节点名称用于返回
        node2Node.setSourceName(sourceNode.getName());
        node2Node.setTargetName(targetNode.getName());

        return node2Node;
    }

    @Override
    public List<Node2Node> listRelationships(Long sourceId, Long targetId,
                                              RelationshipType type, RelationshipStatus status,
                                              int pageNum, int pageSize) {
        List<Node2Node> relationships = node2NodeRepository.findByConditions(
                sourceId, targetId, type, status, pageNum, pageSize);

        // 填充节点名称
        enrichWithNodeNames(relationships);

        return relationships;
    }

    @Override
    public long countRelationships(Long sourceId, Long targetId,
                                   RelationshipType type, RelationshipStatus status) {
        return node2NodeRepository.countByConditions(sourceId, targetId, type, status);
    }

    @Override
    public Optional<Node2Node> getRelationshipById(Long relationshipId) {
        Optional<Node2Node> relationshipOpt = node2NodeRepository.findById(relationshipId);
        relationshipOpt.ifPresent(r -> enrichWithNodeNames(Collections.singletonList(r)));
        return relationshipOpt;
    }

    @Override
    public List<Node2Node> getUpstreamDependencies(Long nodeId) {
        if (nodeId == null) {
            throw new IllegalArgumentException("节点ID不能为空");
        }
        List<Node2Node> relationships = node2NodeRepository.findByTargetId(nodeId);
        enrichWithNodeNames(relationships);
        return relationships;
    }

    @Override
    public List<Node2Node> getDownstreamDependencies(Long nodeId) {
        if (nodeId == null) {
            throw new IllegalArgumentException("节点ID不能为空");
        }
        List<Node2Node> relationships = node2NodeRepository.findBySourceId(nodeId);
        enrichWithNodeNames(relationships);
        return relationships;
    }

    @Override
    @Transactional
    public Node2Node updateRelationship(Long relationshipId, RelationshipType type,
                                         RelationshipStrength strength, RelationshipStatus status,
                                         String description, Long operatorId) {
        // 1. 验证关系存在
        Node2Node relationship = node2NodeRepository.findById(relationshipId)
                .orElseThrow(() -> new BusinessException(RelationshipErrorCode.RELATIONSHIP_NOT_FOUND, relationshipId));

        // 2. 验证用户权限（只需要源节点的权限）
        Node sourceNode = nodeRepository.findById(relationship.getSourceId())
                .orElse(null);
        if (sourceNode == null || !sourceNode.isOwner(operatorId)) {
            throw new BusinessException(ResourceErrorCode.FORBIDDEN);
        }

        // 3. 更新关系
        relationship.update(type, strength, status, description);
        relationship = node2NodeRepository.update(relationship);

        log.info("关系更新成功: {}", relationshipId);

        // 设置节点名称
        relationship.setSourceName(sourceNode.getName());
        Node targetNode = nodeRepository.findById(relationship.getTargetId())
                .orElse(null);
        if (targetNode != null) {
            relationship.setTargetName(targetNode.getName());
        }

        return relationship;
    }

    @Override
    @Transactional
    public void deleteRelationship(Long relationshipId, Long operatorId) {
        // 1. 验证关系存在
        Node2Node relationship = node2NodeRepository.findById(relationshipId)
                .orElseThrow(() -> new BusinessException(RelationshipErrorCode.RELATIONSHIP_NOT_FOUND, relationshipId));

        // 2. 验证用户权限（只需要源节点的权限）
        Node sourceNode = nodeRepository.findById(relationship.getSourceId())
                .orElse(null);
        if (sourceNode == null || !sourceNode.isOwner(operatorId)) {
            throw new BusinessException(ResourceErrorCode.FORBIDDEN);
        }

        // 3. 删除关系
        node2NodeRepository.deleteById(relationshipId);
        log.info("关系删除成功: {}", relationshipId);

        // 4. 如果是双向关系，删除反向关系
        if (relationship.isBidirectional()) {
            node2NodeRepository.deleteBySourceAndTargetAndType(
                    relationship.getTargetId(),
                    relationship.getSourceId(),
                    relationship.getRelationshipType());
            log.info("反向关系删除成功");
        }
    }

    @Override
    @Transactional
    public void deleteRelationshipsByNode(Long nodeId) {
        if (nodeId == null) {
            return;
        }
        node2NodeRepository.deleteByNodeId(nodeId);
        log.info("节点关联的所有关系已删除: {}", nodeId);
    }

    @Override
    public CycleDetectionResult detectCycle(Long nodeId) {
        if (nodeId == null) {
            throw new IllegalArgumentException("节点ID不能为空");
        }

        Set<Long> visited = new HashSet<>();
        Set<Long> recursionStack = new HashSet<>();
        List<Long> cyclePath = new ArrayList<>();

        boolean hasCycle = dfsDetectCycle(nodeId, visited, recursionStack, cyclePath);

        if (hasCycle) {
            return CycleDetectionResult.withCycle(cyclePath);
        }
        return CycleDetectionResult.noCycle();
    }

    private boolean dfsDetectCycle(Long nodeId, Set<Long> visited,
                                    Set<Long> recursionStack, List<Long> cyclePath) {
        if (recursionStack.contains(nodeId)) {
            // 找到循环，构建循环路径
            cyclePath.add(nodeId);
            return true;
        }

        if (visited.contains(nodeId)) {
            return false;
        }

        visited.add(nodeId);
        recursionStack.add(nodeId);
        cyclePath.add(nodeId);

        // 获取下游依赖
        List<Node2Node> downstreams = node2NodeRepository.findBySourceId(nodeId);
        for (Node2Node rel : downstreams) {
            if (dfsDetectCycle(rel.getTargetId(), visited, recursionStack, cyclePath)) {
                return true;
            }
        }

        recursionStack.remove(nodeId);
        cyclePath.remove(cyclePath.size() - 1);
        return false;
    }

    @Override
    public TraverseResult traverse(Long nodeId, int maxDepth) {
        if (nodeId == null) {
            throw new IllegalArgumentException("节点ID不能为空");
        }
        if (maxDepth <= 0) {
            maxDepth = DEFAULT_MAX_DEPTH;
        }

        Map<Integer, List<Long>> nodesByLevel = new HashMap<>();
        List<Relationship> allRelationships = new ArrayList<>();
        Set<Long> visited = new HashSet<>();

        // BFS 遍历
        Queue<Long> queue = new LinkedList<>();
        Queue<Integer> levelQueue = new LinkedList<>();

        queue.offer(nodeId);
        levelQueue.offer(0);
        visited.add(nodeId);

        while (!queue.isEmpty() && visited.size() < MAX_TRAVERSE_NODES) {
            Long currentId = queue.poll();
            int currentLevel = levelQueue.poll();

            // 添加到当前层级
            nodesByLevel.computeIfAbsent(currentLevel, k -> new ArrayList<>()).add(currentId);

            // 如果还没到最大深度，继续遍历
            if (currentLevel < maxDepth) {
                List<Node2Node> downstreams = node2NodeRepository.findBySourceId(currentId);
                for (Node2Node rel : downstreams) {
                    // 转换为 Relationship 以兼容 TraverseResult
                    Relationship r = toRelationship(rel);
                    allRelationships.add(r);

                    Long targetId = rel.getTargetId();
                    if (!visited.contains(targetId)) {
                        visited.add(targetId);
                        queue.offer(targetId);
                        levelQueue.offer(currentLevel + 1);
                    }
                }
            }
        }

        return new TraverseResult(nodeId, nodesByLevel, allRelationships);
    }

    /**
     * 为关系列表填充节点名称
     */
    private void enrichWithNodeNames(List<Node2Node> relationships) {
        if (relationships == null || relationships.isEmpty()) {
            return;
        }

        // 收集所有节点ID
        Set<Long> nodeIds = new HashSet<>();
        for (Node2Node rel : relationships) {
            nodeIds.add(rel.getSourceId());
            nodeIds.add(rel.getTargetId());
        }

        // 查询节点名称
        Map<Long, String> nodeNameMap = new HashMap<>();
        for (Long id : nodeIds) {
            nodeRepository.findById(id)
                    .ifPresent(n -> nodeNameMap.put(id, n.getName()));
        }

        // 填充名称
        for (Node2Node rel : relationships) {
            rel.setSourceName(nodeNameMap.get(rel.getSourceId()));
            rel.setTargetName(nodeNameMap.get(rel.getTargetId()));
        }
    }

    /**
     * 将 Node2Node 转换为 Relationship（用于 TraverseResult 兼容）
     */
    private Relationship toRelationship(Node2Node node2Node) {
        Relationship r = new Relationship();
        r.setId(node2Node.getId());
        r.setSourceResourceId(node2Node.getSourceId());
        r.setTargetResourceId(node2Node.getTargetId());
        r.setRelationshipType(node2Node.getRelationshipType());
        r.setDirection(node2Node.getDirection());
        r.setStrength(node2Node.getStrength());
        r.setStatus(node2Node.getStatus());
        r.setDescription(node2Node.getDescription());
        r.setCreatedAt(node2Node.getCreatedAt());
        r.setUpdatedAt(node2Node.getUpdatedAt());
        r.setSourceResourceName(node2Node.getSourceName());
        r.setTargetResourceName(node2Node.getTargetName());
        return r;
    }
}
