package com.catface996.aiops.repository.topology2;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 拓扑图-报告模板关联仓储接口
 *
 * <p>提供拓扑图与报告模板关联关系的数据访问操作。</p>
 *
 * @author AI Assistant
 * @since 2025-12-29
 */
public interface TopologyReportTemplateRepository {

    /**
     * 绑定报告模板
     *
     * @param topologyId       拓扑图ID
     * @param reportTemplateId 报告模板ID
     * @param createdBy        创建人ID
     */
    void bind(Long topologyId, Long reportTemplateId, Long createdBy);

    /**
     * 批量绑定报告模板
     *
     * @param topologyId        拓扑图ID
     * @param reportTemplateIds 报告模板ID列表
     * @param createdBy         创建人ID
     * @return 成功绑定的数量
     */
    int bindBatch(Long topologyId, List<Long> reportTemplateIds, Long createdBy);

    /**
     * 解绑报告模板
     *
     * @param topologyId       拓扑图ID
     * @param reportTemplateId 报告模板ID
     */
    void unbind(Long topologyId, Long reportTemplateId);

    /**
     * 批量解绑报告模板
     *
     * @param topologyId        拓扑图ID
     * @param reportTemplateIds 报告模板ID列表
     * @return 成功解绑的数量
     */
    int unbindBatch(Long topologyId, List<Long> reportTemplateIds);

    /**
     * 检查绑定关系是否存在
     *
     * @param topologyId       拓扑图ID
     * @param reportTemplateId 报告模板ID
     * @return true if binding exists
     */
    boolean existsByTopologyIdAndTemplateId(Long topologyId, Long reportTemplateId);

    /**
     * 查询拓扑图已绑定的模板ID列表
     *
     * @param topologyId 拓扑图ID
     * @return 模板ID列表
     */
    List<Long> findBoundTemplateIds(Long topologyId);

    /**
     * 统计拓扑图绑定的模板数量
     *
     * @param topologyId 拓扑图ID
     * @return 绑定数量
     */
    int countByTopologyId(Long topologyId);

    /**
     * 已绑定报告模板信息
     */
    record BoundTemplateInfo(
            Long bindingId,
            Long topologyId,
            Long reportTemplateId,
            String templateName,
            String templateDescription,
            String templateCategory,
            LocalDateTime boundAt,
            Long boundBy
    ) {}

    /**
     * 分页查询已绑定的报告模板
     *
     * @param topologyId 拓扑图ID
     * @param keyword    搜索关键词（可选）
     * @param page       页码（从1开始）
     * @param size       每页大小
     * @return 分页结果
     */
    PageResult<BoundTemplateInfo> findBoundTemplates(Long topologyId, String keyword, int page, int size);

    /**
     * 未绑定报告模板信息
     */
    record UnboundTemplateInfo(
            Long id,
            String name,
            String description,
            String category
    ) {}

    /**
     * 分页查询未绑定的报告模板
     *
     * @param topologyId 拓扑图ID
     * @param keyword    搜索关键词（可选）
     * @param page       页码（从1开始）
     * @param size       每页大小
     * @return 分页结果
     */
    PageResult<UnboundTemplateInfo> findUnboundTemplates(Long topologyId, String keyword, int page, int size);

    /**
     * 分页结果
     */
    record PageResult<T>(
            List<T> content,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean first,
            boolean last
    ) {}
}
