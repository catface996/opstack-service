package com.catface996.aiops.application.impl.service.topology;

import com.catface996.aiops.application.api.dto.common.PageResult;
import com.catface996.aiops.application.api.service.topology.TopologyReportTemplateApplicationService;
import com.catface996.aiops.domain.service.topology2.TopologyReportTemplateDomainService;
import com.catface996.aiops.repository.topology2.TopologyReportTemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 拓扑图-报告模板绑定应用服务实现
 *
 * @author AI Assistant
 * @since 2025-12-29
 */
@Service
public class TopologyReportTemplateApplicationServiceImpl implements TopologyReportTemplateApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(TopologyReportTemplateApplicationServiceImpl.class);

    private final TopologyReportTemplateDomainService topologyReportTemplateDomainService;
    private final TopologyReportTemplateRepository topologyReportTemplateRepository;

    public TopologyReportTemplateApplicationServiceImpl(
            TopologyReportTemplateDomainService topologyReportTemplateDomainService,
            TopologyReportTemplateRepository topologyReportTemplateRepository) {
        this.topologyReportTemplateDomainService = topologyReportTemplateDomainService;
        this.topologyReportTemplateRepository = topologyReportTemplateRepository;
    }

    @Override
    public BindResultDTO bindReportTemplates(Long topologyId, List<Long> reportTemplateIds, Long operatorId) {
        logger.info("绑定报告模板，topologyId: {}, templateIds: {}, operatorId: {}",
                topologyId, reportTemplateIds, operatorId);

        TopologyReportTemplateDomainService.BindResult result =
                topologyReportTemplateDomainService.bindReportTemplates(topologyId, reportTemplateIds, operatorId);

        return new BindResultDTO(result.successCount(), result.skipCount());
    }

    @Override
    public UnbindResultDTO unbindReportTemplates(Long topologyId, List<Long> reportTemplateIds, Long operatorId) {
        logger.info("解绑报告模板，topologyId: {}, templateIds: {}, operatorId: {}",
                topologyId, reportTemplateIds, operatorId);

        TopologyReportTemplateDomainService.UnbindResult result =
                topologyReportTemplateDomainService.unbindReportTemplates(topologyId, reportTemplateIds, operatorId);

        return new UnbindResultDTO(result.successCount(), result.skipCount());
    }

    @Override
    public PageResult<BoundTemplateDTO> queryBoundTemplates(Long topologyId, String keyword, int page, int size) {
        logger.info("查询已绑定模板，topologyId: {}, keyword: {}, page: {}, size: {}",
                topologyId, keyword, page, size);

        TopologyReportTemplateRepository.PageResult<TopologyReportTemplateRepository.BoundTemplateInfo> result =
                topologyReportTemplateRepository.findBoundTemplates(topologyId, keyword, page, size);

        List<BoundTemplateDTO> content = result.content().stream()
                .map(info -> new BoundTemplateDTO(
                        info.reportTemplateId(),
                        info.templateName(),
                        info.templateDescription(),
                        info.templateCategory(),
                        info.boundAt(),
                        info.boundBy()
                ))
                .collect(Collectors.toList());

        return PageResult.of(content, result.page(), result.size(), result.totalElements());
    }

    @Override
    public PageResult<UnboundTemplateDTO> queryUnboundTemplates(Long topologyId, String keyword, int page, int size) {
        logger.info("查询未绑定模板，topologyId: {}, keyword: {}, page: {}, size: {}",
                topologyId, keyword, page, size);

        TopologyReportTemplateRepository.PageResult<TopologyReportTemplateRepository.UnboundTemplateInfo> result =
                topologyReportTemplateRepository.findUnboundTemplates(topologyId, keyword, page, size);

        List<UnboundTemplateDTO> content = result.content().stream()
                .map(info -> new UnboundTemplateDTO(
                        info.id(),
                        info.name(),
                        info.description(),
                        info.category()
                ))
                .collect(Collectors.toList());

        return PageResult.of(content, result.page(), result.size(), result.totalElements());
    }
}
