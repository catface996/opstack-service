package com.catface996.aiops.domain.model.subgraph;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 子图聚合根
 *
 * <p>子图是资源节点的逻辑分组，代表特定的业务域、系统模块或运维范围。</p>
 * <p>子图名称在系统中必须全局唯一。</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F08: 子图管理功能</li>
 *   <li>需求1: 子图创建</li>
 *   <li>需求2: 子图列表视图</li>
 *   <li>需求3: 子图信息编辑</li>
 *   <li>需求7: 子图详情视图</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-04
 */
public class Subgraph {

    /**
     * 子图ID（主键）
     */
    private Long id;

    /**
     * 子图名称（全局唯一，1-255字符）
     */
    private String name;

    /**
     * 子图描述
     */
    private String description;

    /**
     * 标签列表（用于分类和过滤）
     */
    private List<String> tags;

    /**
     * 元数据（键值对形式，如业务域、环境、团队等）
     */
    private Map<String, String> metadata;

    /**
     * 创建者ID
     */
    private Long createdBy;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 版本号（用于乐观锁）
     */
    private Integer version;

    // 构造函数
    public Subgraph() {
        this.tags = new ArrayList<>();
        this.metadata = new HashMap<>();
        this.version = 0;
    }

    public Subgraph(Long id, String name, String description, List<String> tags,
                    Map<String, String> metadata, Long createdBy,
                    LocalDateTime createdAt, LocalDateTime updatedAt, Integer version) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.tags = tags != null ? new ArrayList<>(tags) : new ArrayList<>();
        this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.version = version;
    }

    // 业务方法

    /**
     * 创建新子图的工厂方法
     *
     * @param name 子图名称
     * @param description 子图描述
     * @param tags 标签列表
     * @param metadata 元数据
     * @param createdBy 创建者ID
     * @return 新创建的子图实例
     */
    public static Subgraph create(String name, String description, List<String> tags,
                                   Map<String, String> metadata, Long createdBy) {
        Subgraph subgraph = new Subgraph();
        subgraph.setName(name);
        subgraph.setDescription(description);
        subgraph.setTags(tags != null ? new ArrayList<>(tags) : new ArrayList<>());
        subgraph.setMetadata(metadata != null ? new HashMap<>(metadata) : new HashMap<>());
        subgraph.setCreatedBy(createdBy);
        subgraph.setCreatedAt(LocalDateTime.now());
        subgraph.setUpdatedAt(LocalDateTime.now());
        subgraph.setVersion(0);
        return subgraph;
    }

    /**
     * 更新子图基本信息
     *
     * @param name 新的子图名称（可为null表示不更新）
     * @param description 新的子图描述（可为null表示不更新）
     * @param tags 新的标签列表（可为null表示不更新）
     */
    public void updateBasicInfo(String name, String description, List<String> tags) {
        if (name != null) {
            this.name = name;
        }
        if (description != null) {
            this.description = description;
        }
        if (tags != null) {
            this.tags = new ArrayList<>(tags);
        }
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 更新元数据
     *
     * @param metadata 新的元数据（替换现有元数据）
     */
    public void updateMetadata(Map<String, String> metadata) {
        if (metadata != null) {
            this.metadata = new HashMap<>(metadata);
        }
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 检查用户是否可以编辑此子图
     *
     * @param userId 用户ID
     * @param permissions 子图权限列表
     * @return true 如果用户是 Owner
     */
    public boolean canBeEditedBy(Long userId, List<SubgraphPermission> permissions) {
        if (userId == null || permissions == null) {
            return false;
        }
        return permissions.stream()
                .anyMatch(p -> userId.equals(p.getUserId()) && p.getRole().canEdit());
    }

    /**
     * 检查用户是否可以删除此子图
     *
     * @param userId 用户ID
     * @param permissions 子图权限列表
     * @return true 如果用户是 Owner
     */
    public boolean canBeDeletedBy(Long userId, List<SubgraphPermission> permissions) {
        if (userId == null || permissions == null) {
            return false;
        }
        return permissions.stream()
                .anyMatch(p -> userId.equals(p.getUserId()) && p.getRole().canDelete());
    }

    /**
     * 检查用户是否可以查看此子图
     *
     * @param userId 用户ID
     * @param permissions 子图权限列表
     * @return true 如果用户有任何权限（Owner 或 Viewer）
     */
    public boolean canBeViewedBy(Long userId, List<SubgraphPermission> permissions) {
        if (userId == null || permissions == null) {
            return false;
        }
        return permissions.stream()
                .anyMatch(p -> userId.equals(p.getUserId()) && p.getRole().canView());
    }

    /**
     * 检查用户是否可以管理资源节点
     *
     * @param userId 用户ID
     * @param permissions 子图权限列表
     * @return true 如果用户是 Owner
     */
    public boolean canManageResourcesBy(Long userId, List<SubgraphPermission> permissions) {
        if (userId == null || permissions == null) {
            return false;
        }
        return permissions.stream()
                .anyMatch(p -> userId.equals(p.getUserId()) && p.getRole().canManageResources());
    }

    /**
     * 增加版本号（用于乐观锁）
     */
    public void incrementVersion() {
        this.version = (this.version == null) ? 1 : this.version + 1;
    }

    /**
     * 添加标签
     *
     * @param tag 要添加的标签
     */
    public void addTag(String tag) {
        if (tag != null && !tag.trim().isEmpty()) {
            if (this.tags == null) {
                this.tags = new ArrayList<>();
            }
            if (!this.tags.contains(tag.trim())) {
                this.tags.add(tag.trim());
                this.updatedAt = LocalDateTime.now();
            }
        }
    }

    /**
     * 移除标签
     *
     * @param tag 要移除的标签
     */
    public void removeTag(String tag) {
        if (this.tags != null && tag != null) {
            if (this.tags.remove(tag.trim())) {
                this.updatedAt = LocalDateTime.now();
            }
        }
    }

    /**
     * 设置元数据
     *
     * @param key 键
     * @param value 值
     */
    public void setMetadataValue(String key, String value) {
        if (key != null && !key.trim().isEmpty()) {
            if (this.metadata == null) {
                this.metadata = new HashMap<>();
            }
            this.metadata.put(key.trim(), value);
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * 移除元数据
     *
     * @param key 要移除的键
     */
    public void removeMetadataValue(String key) {
        if (this.metadata != null && key != null) {
            if (this.metadata.remove(key.trim()) != null) {
                this.updatedAt = LocalDateTime.now();
            }
        }
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags != null ? new ArrayList<>(tags) : new ArrayList<>();
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "Subgraph{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", tags=" + tags +
                ", metadata=" + metadata +
                ", createdBy=" + createdBy +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", version=" + version +
                '}';
    }
}
