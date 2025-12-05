package com.catface996.aiops.repository.mysql.impl.subgraph;

import com.catface996.aiops.domain.model.subgraph.PermissionRole;
import com.catface996.aiops.domain.model.subgraph.Subgraph;
import com.catface996.aiops.domain.model.subgraph.SubgraphPermission;
import com.catface996.aiops.repository.mysql.mapper.subgraph.SubgraphMapper;
import com.catface996.aiops.repository.mysql.mapper.subgraph.SubgraphPermissionMapper;
import com.catface996.aiops.repository.mysql.po.subgraph.SubgraphPO;
import com.catface996.aiops.repository.mysql.po.subgraph.SubgraphPermissionPO;
import com.catface996.aiops.repository.subgraph.SubgraphRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 子图仓储实现类
 *
 * <p>使用 MyBatis 实现子图数据访问</p>
 * <p>负责领域对象与持久化对象之间的转换</p>
 * <p>包含子图权限的操作</p>
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
@Repository
public class SubgraphRepositoryImpl implements SubgraphRepository {

    private final SubgraphMapper subgraphMapper;
    private final SubgraphPermissionMapper permissionMapper;
    private final ObjectMapper objectMapper;

    public SubgraphRepositoryImpl(SubgraphMapper subgraphMapper,
                                   SubgraphPermissionMapper permissionMapper,
                                   ObjectMapper objectMapper) {
        this.subgraphMapper = subgraphMapper;
        this.permissionMapper = permissionMapper;
        this.objectMapper = objectMapper;
    }

    // ==================== 子图 CRUD 操作 ====================

    @Override
    public Subgraph save(Subgraph subgraph) {
        if (subgraph == null) {
            throw new IllegalArgumentException("子图实体不能为null");
        }
        SubgraphPO po = toPO(subgraph);
        subgraphMapper.insert(po);
        return toEntity(subgraphMapper.selectById(po.getId()));
    }

    @Override
    public Optional<Subgraph> findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("子图ID不能为null");
        }
        SubgraphPO po = subgraphMapper.selectById(id);
        return Optional.ofNullable(toEntity(po));
    }

    @Override
    public Optional<Subgraph> findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return Optional.empty();
        }
        SubgraphPO po = subgraphMapper.selectByName(name);
        return Optional.ofNullable(toEntity(po));
    }

    @Override
    public List<Subgraph> findByUserId(Long userId, int page, int size) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为null");
        }
        int offset = (page - 1) * size;
        List<SubgraphPO> poList = subgraphMapper.selectByUserId(userId, offset, size);
        return poList.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public long countByUserId(Long userId) {
        if (userId == null) {
            return 0;
        }
        return subgraphMapper.countByUserId(userId);
    }

    @Override
    public List<Subgraph> searchByKeyword(String keyword, Long userId, int page, int size) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为null");
        }
        int offset = (page - 1) * size;
        List<SubgraphPO> poList = subgraphMapper.searchByKeyword(keyword, userId, offset, size);
        return poList.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public long countByKeyword(String keyword, Long userId) {
        if (userId == null) {
            return 0;
        }
        return subgraphMapper.countByKeyword(keyword, userId);
    }

    @Override
    public List<Subgraph> filterByTags(List<String> tags, Long userId, int page, int size) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为null");
        }
        int offset = (page - 1) * size;
        List<SubgraphPO> poList = subgraphMapper.filterByTags(tags, userId, offset, size);
        return poList.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public long countByTags(List<String> tags, Long userId) {
        if (userId == null) {
            return 0;
        }
        return subgraphMapper.countByTags(tags, userId);
    }

    @Override
    public List<Subgraph> filterByOwner(Long ownerId, Long currentUserId, int page, int size) {
        if (ownerId == null || currentUserId == null) {
            throw new IllegalArgumentException("所有者ID和当前用户ID不能为null");
        }
        int offset = (page - 1) * size;
        List<SubgraphPO> poList = subgraphMapper.filterByOwner(ownerId, currentUserId, offset, size);
        return poList.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public long countByOwner(Long ownerId, Long currentUserId) {
        if (ownerId == null || currentUserId == null) {
            return 0;
        }
        return subgraphMapper.countByOwner(ownerId, currentUserId);
    }

    @Override
    public boolean update(Subgraph subgraph) {
        if (subgraph == null) {
            throw new IllegalArgumentException("子图实体不能为null");
        }
        if (subgraph.getId() == null) {
            throw new IllegalArgumentException("子图ID不能为null");
        }
        SubgraphPO po = toPO(subgraph);
        int rows = subgraphMapper.updateWithVersion(po);
        return rows > 0;
    }

    @Override
    public void delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("子图ID不能为null");
        }
        subgraphMapper.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        if (id == null) {
            return false;
        }
        return subgraphMapper.selectById(id) != null;
    }

    @Override
    public boolean existsByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return subgraphMapper.selectByName(name) != null;
    }

    @Override
    public boolean existsByNameExcludeId(String name, Long excludeId) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return subgraphMapper.existsByNameExcludeId(name, excludeId) > 0;
    }

    // ==================== 权限操作 ====================

    @Override
    public SubgraphPermission savePermission(SubgraphPermission permission) {
        if (permission == null) {
            throw new IllegalArgumentException("权限实体不能为null");
        }
        SubgraphPermissionPO po = toPermissionPO(permission);
        permissionMapper.insert(po);
        return toPermissionEntity(permissionMapper.selectById(po.getId()));
    }

    @Override
    public List<SubgraphPermission> findPermissionsBySubgraphId(Long subgraphId) {
        if (subgraphId == null) {
            throw new IllegalArgumentException("子图ID不能为null");
        }
        List<SubgraphPermissionPO> poList = permissionMapper.selectBySubgraphId(subgraphId);
        return poList.stream()
                .map(this::toPermissionEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<SubgraphPermission> findPermissionBySubgraphIdAndUserId(Long subgraphId, Long userId) {
        if (subgraphId == null || userId == null) {
            return Optional.empty();
        }
        SubgraphPermissionPO po = permissionMapper.selectBySubgraphIdAndUserId(subgraphId, userId);
        return Optional.ofNullable(toPermissionEntity(po));
    }

    @Override
    public int countOwnersBySubgraphId(Long subgraphId) {
        if (subgraphId == null) {
            return 0;
        }
        return permissionMapper.countOwnersBySubgraphId(subgraphId);
    }

    @Override
    public void deletePermission(Long subgraphId, Long userId) {
        if (subgraphId == null || userId == null) {
            throw new IllegalArgumentException("子图ID和用户ID不能为null");
        }
        permissionMapper.deleteBySubgraphIdAndUserId(subgraphId, userId);
    }

    @Override
    public boolean hasPermission(Long subgraphId, Long userId, PermissionRole role) {
        if (subgraphId == null || userId == null || role == null) {
            return false;
        }
        return permissionMapper.hasPermission(subgraphId, userId, role.name()) > 0;
    }

    @Override
    public boolean hasAnyPermission(Long subgraphId, Long userId) {
        if (subgraphId == null || userId == null) {
            return false;
        }
        return permissionMapper.hasAnyPermission(subgraphId, userId) > 0;
    }

    // ==================== 对象转换方法 ====================

    /**
     * 将领域实体转换为持久化对象
     */
    private SubgraphPO toPO(Subgraph entity) {
        if (entity == null) {
            return null;
        }
        SubgraphPO po = new SubgraphPO();
        po.setId(entity.getId());
        po.setName(entity.getName());
        po.setDescription(entity.getDescription());
        po.setTags(toJson(entity.getTags()));
        po.setMetadata(toJson(entity.getMetadata()));
        po.setCreatedBy(entity.getCreatedBy());
        po.setCreatedAt(entity.getCreatedAt());
        po.setUpdatedAt(entity.getUpdatedAt());
        po.setVersion(entity.getVersion());
        return po;
    }

    /**
     * 将持久化对象转换为领域实体
     */
    private Subgraph toEntity(SubgraphPO po) {
        if (po == null) {
            return null;
        }
        Subgraph entity = new Subgraph();
        entity.setId(po.getId());
        entity.setName(po.getName());
        entity.setDescription(po.getDescription());
        entity.setTags(fromJsonList(po.getTags()));
        entity.setMetadata(fromJsonMap(po.getMetadata()));
        entity.setCreatedBy(po.getCreatedBy());
        entity.setCreatedAt(po.getCreatedAt());
        entity.setUpdatedAt(po.getUpdatedAt());
        entity.setVersion(po.getVersion());
        return entity;
    }

    /**
     * 将权限领域实体转换为持久化对象
     */
    private SubgraphPermissionPO toPermissionPO(SubgraphPermission entity) {
        if (entity == null) {
            return null;
        }
        SubgraphPermissionPO po = new SubgraphPermissionPO();
        po.setId(entity.getId());
        po.setSubgraphId(entity.getSubgraphId());
        po.setUserId(entity.getUserId());
        po.setRole(entity.getRole() != null ? entity.getRole().name() : null);
        po.setGrantedAt(entity.getGrantedAt());
        po.setGrantedBy(entity.getGrantedBy());
        return po;
    }

    /**
     * 将权限持久化对象转换为领域实体
     */
    private SubgraphPermission toPermissionEntity(SubgraphPermissionPO po) {
        if (po == null) {
            return null;
        }
        SubgraphPermission entity = new SubgraphPermission();
        entity.setId(po.getId());
        entity.setSubgraphId(po.getSubgraphId());
        entity.setUserId(po.getUserId());
        entity.setRole(po.getRole() != null ? PermissionRole.valueOf(po.getRole()) : null);
        entity.setGrantedAt(po.getGrantedAt());
        entity.setGrantedBy(po.getGrantedBy());
        return entity;
    }

    // ==================== JSON 转换辅助方法 ====================

    private String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON序列化失败", e);
        }
    }

    private List<String> fromJsonList(String json) {
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }

    private Map<String, String> fromJsonMap(String json) {
        if (json == null || json.isEmpty()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, String>>() {});
        } catch (JsonProcessingException e) {
            return new HashMap<>();
        }
    }
}
