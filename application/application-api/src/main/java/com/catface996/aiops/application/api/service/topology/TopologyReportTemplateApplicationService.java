package com.catface996.aiops.application.api.service.topology;

import com.catface996.aiops.application.api.dto.common.PageResult;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 拓扑图-报告模板绑定应用服务接口
 *
 * <p>提供拓扑图与报告模板绑定关系的应用层接口。</p>
 *
 * @author AI Assistant
 * @since 2025-12-29
 */
public interface TopologyReportTemplateApplicationService {

    /**
     * 绑定操作结果 DTO
     */
    record BindResultDTO(int successCount, int skipCount) {}

    /**
     * 解绑操作结果 DTO
     */
    record UnbindResultDTO(int successCount, int skipCount) {}

    /**
     * 已绑定报告模板 DTO
     */
    record BoundTemplateDTO(
            Long id,
            String name,
            String description,
            String category,
            LocalDateTime boundAt,
            Long boundBy
    ) {}

    /**
     * 未绑定报告模板 DTO
     */
    record UnboundTemplateDTO(
            Long id,
            String name,
            String description,
            String category
    ) {}

    /**
     * 批量绑定报告模板到拓扑图
     *
     * @param topologyId        拓扑图ID
     * @param reportTemplateIds 报告模板ID列表
     * @param operatorId        操作人ID
     * @return 绑定结果
     */
    BindResultDTO bindReportTemplates(Long topologyId, List<Long> reportTemplateIds, Long operatorId);

    /**
     * 批量解绑报告模板
     *
     * @param topologyId        拓扑图ID
     * @param reportTemplateIds 报告模板ID列表
     * @param operatorId        操作人ID
     * @return 解绑结果
     */
    UnbindResultDTO unbindReportTemplates(Long topologyId, List<Long> reportTemplateIds, Long operatorId);

    /**
     * 分页查询已绑定的报告模板
     *
     * @param topologyId 拓扑图ID
     * @param keyword    搜索关键词（可选）
     * @param page       页码（从1开始）
     * @param size       每页大小
     * @return 分页结果
     */
    PageResult<BoundTemplateDTO> queryBoundTemplates(Long topologyId, String keyword, int page, int size);

    /**
     * 分页查询未绑定的报告模板
     *
     * @param topologyId 拓扑图ID
     * @param keyword    搜索关键词（可选）
     * @param page       页码（从1开始）
     * @param size       每页大小
     * @return 分页结果
     */
    PageResult<UnboundTemplateDTO> queryUnboundTemplates(Long topologyId, String keyword, int page, int size);
}
