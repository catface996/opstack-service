package com.catface996.aiops.domain.impl.service.topology2;

import com.catface996.aiops.domain.service.topology2.TopologyReportTemplateDomainService;
import com.catface996.aiops.repository.report.ReportTemplateRepository;
import com.catface996.aiops.repository.topology2.TopologyReportTemplateRepository;
import com.catface996.aiops.repository.topology2.TopologyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 拓扑图-报告模板绑定领域服务实现
 *
 * @author AI Assistant
 * @since 2025-12-29
 */
@Service
public class TopologyReportTemplateDomainServiceImpl implements TopologyReportTemplateDomainService {

    private static final Logger logger = LoggerFactory.getLogger(TopologyReportTemplateDomainServiceImpl.class);

    private static final int MAX_BATCH_SIZE = 100;

    private final TopologyRepository topologyRepository;
    private final ReportTemplateRepository reportTemplateRepository;
    private final TopologyReportTemplateRepository topologyReportTemplateRepository;

    public TopologyReportTemplateDomainServiceImpl(
            TopologyRepository topologyRepository,
            ReportTemplateRepository reportTemplateRepository,
            TopologyReportTemplateRepository topologyReportTemplateRepository) {
        this.topologyRepository = topologyRepository;
        this.reportTemplateRepository = reportTemplateRepository;
        this.topologyReportTemplateRepository = topologyReportTemplateRepository;
    }

    @Override
    @Transactional
    public BindResult bindReportTemplates(Long topologyId, List<Long> reportTemplateIds, Long operatorId) {
        logger.info("绑定报告模板到拓扑图，topologyId: {}, templateIds: {}, operatorId: {}",
                topologyId, reportTemplateIds, operatorId);

        // 参数验证
        if (topologyId == null) {
            throw new IllegalArgumentException("拓扑图ID不能为空");
        }
        if (reportTemplateIds == null || reportTemplateIds.isEmpty()) {
            throw new IllegalArgumentException("报告模板ID列表不能为空");
        }
        if (reportTemplateIds.size() > MAX_BATCH_SIZE) {
            throw new IllegalArgumentException("单次最多绑定 " + MAX_BATCH_SIZE + " 个模板");
        }

        // 验证拓扑图存在
        if (!topologyRepository.existsById(topologyId)) {
            throw new IllegalArgumentException("拓扑图不存在");
        }

        // 验证所有模板存在
        List<Long> existingIds = reportTemplateRepository.findExistingIds(reportTemplateIds);
        if (existingIds.size() != reportTemplateIds.size()) {
            List<Long> missingIds = reportTemplateIds.stream()
                    .filter(id -> !existingIds.contains(id))
                    .toList();
            throw new IllegalArgumentException("报告模板不存在: " + missingIds);
        }

        // 执行批量绑定
        int successCount = topologyReportTemplateRepository.bindBatch(topologyId, reportTemplateIds, operatorId);
        int skipCount = reportTemplateIds.size() - successCount;

        logger.info("绑定完成，成功: {}, 跳过（已存在）: {}", successCount, skipCount);
        return new BindResult(successCount, skipCount);
    }

    @Override
    @Transactional
    public UnbindResult unbindReportTemplates(Long topologyId, List<Long> reportTemplateIds, Long operatorId) {
        logger.info("解绑报告模板，topologyId: {}, templateIds: {}, operatorId: {}",
                topologyId, reportTemplateIds, operatorId);

        // 参数验证
        if (topologyId == null) {
            throw new IllegalArgumentException("拓扑图ID不能为空");
        }
        if (reportTemplateIds == null || reportTemplateIds.isEmpty()) {
            throw new IllegalArgumentException("报告模板ID列表不能为空");
        }
        if (reportTemplateIds.size() > MAX_BATCH_SIZE) {
            throw new IllegalArgumentException("单次最多解绑 " + MAX_BATCH_SIZE + " 个模板");
        }

        // 验证拓扑图存在
        if (!topologyRepository.existsById(topologyId)) {
            throw new IllegalArgumentException("拓扑图不存在");
        }

        // 执行批量解绑
        int successCount = topologyReportTemplateRepository.unbindBatch(topologyId, reportTemplateIds);
        int skipCount = reportTemplateIds.size() - successCount;

        logger.info("解绑完成，成功: {}, 跳过（不存在）: {}", successCount, skipCount);
        return new UnbindResult(successCount, skipCount);
    }

    @Override
    public boolean isTemplateBound(Long topologyId, Long reportTemplateId) {
        return topologyReportTemplateRepository.existsByTopologyIdAndTemplateId(topologyId, reportTemplateId);
    }

    @Override
    public int countBoundTemplates(Long topologyId) {
        return topologyReportTemplateRepository.countByTopologyId(topologyId);
    }
}
