package com.catface996.aiops.domain.service.topology2;

import java.util.List;

/**
 * 拓扑图-报告模板绑定领域服务接口
 *
 * <p>提供拓扑图与报告模板绑定关系的核心业务逻辑。</p>
 *
 * @author AI Assistant
 * @since 2025-12-29
 */
public interface TopologyReportTemplateDomainService {

    /**
     * 绑定操作结果
     */
    record BindResult(int successCount, int skipCount) {}

    /**
     * 解绑操作结果
     */
    record UnbindResult(int successCount, int skipCount) {}

    /**
     * 批量绑定报告模板到拓扑图
     *
     * @param topologyId        拓扑图ID
     * @param reportTemplateIds 报告模板ID列表
     * @param operatorId        操作人ID
     * @return 绑定结果（成功/跳过数量）
     * @throws IllegalArgumentException if topologyId or reportTemplateIds is invalid
     */
    BindResult bindReportTemplates(Long topologyId, List<Long> reportTemplateIds, Long operatorId);

    /**
     * 批量解绑报告模板
     *
     * @param topologyId        拓扑图ID
     * @param reportTemplateIds 报告模板ID列表
     * @param operatorId        操作人ID
     * @return 解绑结果（成功/跳过数量）
     * @throws IllegalArgumentException if topologyId is invalid
     */
    UnbindResult unbindReportTemplates(Long topologyId, List<Long> reportTemplateIds, Long operatorId);

    /**
     * 检查绑定关系是否存在
     *
     * @param topologyId       拓扑图ID
     * @param reportTemplateId 报告模板ID
     * @return true if binding exists
     */
    boolean isTemplateBound(Long topologyId, Long reportTemplateId);

    /**
     * 统计拓扑图绑定的模板数量
     *
     * @param topologyId 拓扑图ID
     * @return 绑定数量
     */
    int countBoundTemplates(Long topologyId);
}
