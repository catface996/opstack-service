package com.catface996.aiops.repository.mysql.impl.node;

import com.catface996.aiops.domain.model.node2node.Node2Node;
import com.catface996.aiops.domain.model.relationship.*;
import com.catface996.aiops.repository.mysql.mapper.node.Node2NodeMapper;
import com.catface996.aiops.repository.mysql.po.node.Node2NodePO;
import com.catface996.aiops.repository.node.Node2NodeRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 节点关系仓储实现
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
@Repository
public class Node2NodeRepositoryImpl implements Node2NodeRepository {

    private final Node2NodeMapper node2NodeMapper;

    public Node2NodeRepositoryImpl(Node2NodeMapper node2NodeMapper) {
        this.node2NodeMapper = node2NodeMapper;
    }

    // ==================== 拓扑图查询方法（原有）====================

    @Override
    public List<RelationshipInfo> findRelationshipsByNodeIds(List<Long> nodeIds) {
        if (nodeIds == null || nodeIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Node2NodePO> pos = node2NodeMapper.selectByNodeIds(nodeIds);
        return pos.stream()
                .map(this::toRelationshipInfo)
                .collect(Collectors.toList());
    }

    @Override
    public List<RelationshipInfo> findOutgoingBySourceId(Long sourceId) {
        List<Node2NodePO> pos = node2NodeMapper.selectOutgoingBySourceId(sourceId);
        return pos.stream()
                .map(this::toRelationshipInfo)
                .collect(Collectors.toList());
    }

    @Override
    public List<RelationshipInfo> findIncomingByTargetId(Long targetId) {
        List<Node2NodePO> pos = node2NodeMapper.selectIncomingByTargetId(targetId);
        return pos.stream()
                .map(this::toRelationshipInfo)
                .collect(Collectors.toList());
    }

    @Override
    public int deleteByNodeId(Long nodeId) {
        return node2NodeMapper.deleteByNodeId(nodeId);
    }

    // ==================== 完整 CRUD 方法（新增）====================

    @Override
    public Node2Node save(Node2Node node2Node) {
        Node2NodePO po = toPO(node2Node);
        po.setCreatedAt(LocalDateTime.now());
        po.setUpdatedAt(LocalDateTime.now());
        node2NodeMapper.insert(po);
        node2Node.setId(po.getId());
        node2Node.setCreatedAt(po.getCreatedAt());
        node2Node.setUpdatedAt(po.getUpdatedAt());
        return node2Node;
    }

    @Override
    public Node2Node update(Node2Node node2Node) {
        Node2NodePO po = toPO(node2Node);
        po.setUpdatedAt(LocalDateTime.now());
        node2NodeMapper.updateById(po);
        node2Node.setUpdatedAt(po.getUpdatedAt());
        return node2Node;
    }

    @Override
    public Optional<Node2Node> findById(Long id) {
        Node2NodePO po = node2NodeMapper.selectById(id);
        return Optional.ofNullable(po).map(this::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        node2NodeMapper.deleteById(id);
    }

    @Override
    public boolean existsBySourceAndTargetAndType(Long sourceId, Long targetId, RelationshipType type) {
        return node2NodeMapper.existsBySourceAndTargetAndType(sourceId, targetId, type.name()) > 0;
    }

    @Override
    public List<Node2Node> findByConditions(Long sourceId, Long targetId,
                                             RelationshipType type, RelationshipStatus status,
                                             int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        String typeStr = type != null ? type.name() : null;
        String statusStr = status != null ? status.name() : null;
        List<Node2NodePO> pos = node2NodeMapper.selectByConditions(sourceId, targetId, typeStr, statusStr, offset, pageSize);
        return pos.stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public long countByConditions(Long sourceId, Long targetId,
                                  RelationshipType type, RelationshipStatus status) {
        String typeStr = type != null ? type.name() : null;
        String statusStr = status != null ? status.name() : null;
        return node2NodeMapper.countByConditions(sourceId, targetId, typeStr, statusStr);
    }

    @Override
    public List<Node2Node> findBySourceId(Long sourceId) {
        List<Node2NodePO> pos = node2NodeMapper.selectBySourceId(sourceId);
        return pos.stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Node2Node> findByTargetId(Long targetId) {
        List<Node2NodePO> pos = node2NodeMapper.selectByTargetId(targetId);
        return pos.stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteBySourceAndTargetAndType(Long sourceId, Long targetId, RelationshipType type) {
        node2NodeMapper.deleteBySourceAndTargetAndType(sourceId, targetId, type.name());
    }

    // ==================== 转换方法 ====================

    private RelationshipInfo toRelationshipInfo(Node2NodePO po) {
        return new RelationshipInfo(
                po.getId(),
                po.getSourceId(),
                po.getTargetId(),
                po.getRelationshipType(),
                po.getDirection(),
                po.getStrength(),
                po.getStatus(),
                po.getDescription()
        );
    }

    private Node2Node toDomain(Node2NodePO po) {
        Node2Node domain = new Node2Node();
        domain.setId(po.getId());
        domain.setSourceId(po.getSourceId());
        domain.setTargetId(po.getTargetId());
        domain.setRelationshipType(RelationshipType.valueOf(po.getRelationshipType()));
        domain.setDirection(RelationshipDirection.valueOf(po.getDirection()));
        domain.setStrength(RelationshipStrength.valueOf(po.getStrength()));
        domain.setStatus(RelationshipStatus.valueOf(po.getStatus()));
        domain.setDescription(po.getDescription());
        domain.setCreatedAt(po.getCreatedAt());
        domain.setUpdatedAt(po.getUpdatedAt());
        domain.setSourceName(po.getSourceName());
        domain.setTargetName(po.getTargetName());
        return domain;
    }

    private Node2NodePO toPO(Node2Node domain) {
        Node2NodePO po = new Node2NodePO();
        po.setId(domain.getId());
        po.setSourceId(domain.getSourceId());
        po.setTargetId(domain.getTargetId());
        po.setRelationshipType(domain.getRelationshipType().name());
        po.setDirection(domain.getDirection().name());
        po.setStrength(domain.getStrength().name());
        po.setStatus(domain.getStatus().name());
        po.setDescription(domain.getDescription());
        po.setCreatedAt(domain.getCreatedAt());
        po.setUpdatedAt(domain.getUpdatedAt());
        return po;
    }
}
