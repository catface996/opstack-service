package com.catface996.aiops.application.impl.service.agentbound;

import com.catface996.aiops.application.api.dto.agent.AgentDTO;
import com.catface996.aiops.application.api.dto.agentbound.AgentBoundDTO;
import com.catface996.aiops.application.api.dto.agentbound.HierarchyStructureDTO;
import com.catface996.aiops.application.api.dto.agentbound.HierarchyTeamDTO;
import com.catface996.aiops.application.api.service.agentbound.AgentBoundApplicationService;
import com.catface996.aiops.common.enums.AgentErrorCode;
import com.catface996.aiops.common.exception.BusinessException;
import com.catface996.aiops.domain.model.agent.Agent;
import com.catface996.aiops.domain.model.agent.AgentHierarchyLevel;
import com.catface996.aiops.domain.model.agentbound.AgentBound;
import com.catface996.aiops.domain.model.agentbound.BoundEntityType;
import com.catface996.aiops.domain.service.agentbound.AgentBoundDomainService;
import com.catface996.aiops.repository.agent.AgentRepository;
import com.catface996.aiops.repository.node.NodeRepository;
import com.catface996.aiops.repository.topology2.TopologyRepository;
import com.catface996.aiops.domain.model.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Agent 绑定关系应用服务实现
 *
 * <p>协调领域层完成 Agent 绑定关系管理业务逻辑，负责 DTO 转换和 Agent 存在性验证。</p>
 *
 * @author AI Assistant
 * @since 2025-12-29
 */
@Service
public class AgentBoundApplicationServiceImpl implements AgentBoundApplicationService {

    private static final Logger log = LoggerFactory.getLogger(AgentBoundApplicationServiceImpl.class);

    private final AgentBoundDomainService agentBoundDomainService;
    private final AgentRepository agentRepository;
    private final TopologyRepository topologyRepository;
    private final NodeRepository nodeRepository;

    public AgentBoundApplicationServiceImpl(AgentBoundDomainService agentBoundDomainService,
                                             AgentRepository agentRepository,
                                             TopologyRepository topologyRepository,
                                             NodeRepository nodeRepository) {
        this.agentBoundDomainService = agentBoundDomainService;
        this.agentRepository = agentRepository;
        this.topologyRepository = topologyRepository;
        this.nodeRepository = nodeRepository;
    }

    @Override
    @Transactional
    public AgentBoundDTO bindAgent(Long agentId, Long entityId, String entityType) {
        log.info("绑定 Agent: agentId={}, entityId={}, entityType={}", agentId, entityId, entityType);

        // 1. 解析实体类型
        BoundEntityType boundEntityType = BoundEntityType.fromName(entityType);
        if (boundEntityType == null) {
            throw new IllegalArgumentException("无效的实体类型: " + entityType);
        }

        // 2. 验证 Agent 存在并获取层级
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new BusinessException(AgentErrorCode.AGENT_NOT_FOUND, agentId));

        AgentHierarchyLevel hierarchyLevel = agent.getHierarchyLevel();
        if (hierarchyLevel == null) {
            throw new IllegalArgumentException("Agent 未设置层级: agentId=" + agentId);
        }

        // 3. 验证层级与实体类型匹配
        if (!boundEntityType.supportsHierarchyLevel(hierarchyLevel)) {
            throw new IllegalArgumentException(
                    String.format("Agent 层级 %s 不能绑定到 %s", hierarchyLevel, boundEntityType));
        }

        // 4. 调用领域服务执行绑定
        AgentBound binding = agentBoundDomainService.bindAgent(
                agentId, hierarchyLevel, entityId, boundEntityType);

        // 5. 补充 Agent 名称和角色
        binding.setDerivedFields(agent.getName(),
                agent.getRole() != null ? agent.getRole().name() : null,
                null);

        return toDTO(binding);
    }

    @Override
    @Transactional
    public void unbindAgent(Long agentId, Long entityId, String entityType) {
        log.info("解绑 Agent: agentId={}, entityId={}, entityType={}", agentId, entityId, entityType);

        BoundEntityType boundEntityType = BoundEntityType.fromName(entityType);
        if (boundEntityType == null) {
            throw new IllegalArgumentException("无效的实体类型: " + entityType);
        }

        int count = agentBoundDomainService.unbind(agentId, entityId, boundEntityType);
        if (count == 0) {
            log.warn("未找到绑定关系: agentId={}, entityId={}, entityType={}",
                    agentId, entityId, entityType);
        }
    }

    @Override
    public List<AgentBoundDTO> queryByEntity(String entityType, Long entityId, String hierarchyLevel) {
        log.info("按实体查询绑定: entityType={}, entityId={}, hierarchyLevel={}",
                entityType, entityId, hierarchyLevel);

        BoundEntityType boundEntityType = BoundEntityType.fromName(entityType);
        if (boundEntityType == null) {
            throw new IllegalArgumentException("无效的实体类型: " + entityType);
        }

        AgentHierarchyLevel level = hierarchyLevel != null
                ? AgentHierarchyLevel.fromName(hierarchyLevel)
                : null;

        List<AgentBound> bindings = agentBoundDomainService.findByEntity(
                boundEntityType, entityId, level);

        return bindings.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AgentBoundDTO> queryByAgent(Long agentId, String entityType) {
        log.info("按 Agent 查询绑定: agentId={}, entityType={}", agentId, entityType);

        BoundEntityType boundEntityType = entityType != null
                ? BoundEntityType.fromName(entityType)
                : null;

        List<AgentBound> bindings = agentBoundDomainService.findByAgentId(agentId, boundEntityType);

        return bindings.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public HierarchyStructureDTO queryHierarchy(Long topologyId) {
        log.info("查询层级结构: topologyId={}", topologyId);

        // 1. 验证 Topology 存在
        var topology = topologyRepository.findById(topologyId)
                .orElseThrow(() -> new IllegalArgumentException("Topology 不存在: " + topologyId));

        // 2. 查询 Topology 下的所有节点（不分页，获取全部）
        List<Node> allNodes = nodeRepository.findByCondition(null, null, null, null, topologyId, 1, Integer.MAX_VALUE);
        log.info("Topology {} 下共有 {} 个节点", topologyId, allNodes.size());

        // 3. 查询所有绑定
        List<AgentBound> allBindings = agentBoundDomainService.queryHierarchyByTopology(topologyId);

        // 4. 分离 Global Supervisor 和 Node 团队绑定
        AgentDTO globalSupervisor = null;
        Map<Long, List<AgentBound>> nodeBindingsMap = new LinkedHashMap<>();

        for (AgentBound binding : allBindings) {
            if (binding.isGlobalSupervisorBinding()) {
                // 一个 Topology 只有一个 Global Supervisor
                if (globalSupervisor == null) {
                    globalSupervisor = toAgentDTO(binding);
                }
            } else {
                // Node 绑定
                Long nodeId = binding.getEntityId();
                nodeBindingsMap.computeIfAbsent(nodeId, k -> new ArrayList<>()).add(binding);
            }
        }

        // 5. 构建团队列表（即使节点没有绑定 Agent 也会返回）
        List<HierarchyTeamDTO> teams = new ArrayList<>();
        for (Node node : allNodes) {
            Long nodeId = node.getId();
            String nodeName = node.getName();

            AgentDTO supervisor = null;
            List<AgentDTO> workers = new ArrayList<>();

            // 如果该节点有绑定记录，则从绑定中获取 Agent 信息
            List<AgentBound> nodeBindings = nodeBindingsMap.get(nodeId);
            if (nodeBindings != null) {
                for (AgentBound b : nodeBindings) {
                    if (b.isTeamSupervisorBinding()) {
                        supervisor = toAgentDTO(b);
                    } else if (b.isWorkerBinding()) {
                        workers.add(toAgentDTO(b));
                    }
                }
            }

            teams.add(HierarchyTeamDTO.builder()
                    .nodeId(nodeId)
                    .nodeName(nodeName)
                    .supervisor(supervisor)
                    .workers(workers)
                    .build());
        }

        // 6. 构建返回结果
        return HierarchyStructureDTO.builder()
                .topologyId(topologyId)
                .topologyName(topology.getName())
                .globalSupervisor(globalSupervisor)
                .teams(teams)
                .build();
    }

    // ===== DTO 转换方法 =====

    private AgentBoundDTO toDTO(AgentBound binding) {
        if (binding == null) {
            return null;
        }
        return AgentBoundDTO.builder()
                .id(binding.getId())
                .agentId(binding.getAgentId())
                .agentName(binding.getAgentName())
                .agentRole(binding.getAgentRole())
                .hierarchyLevel(binding.getHierarchyLevel() != null
                        ? binding.getHierarchyLevel().name() : null)
                .entityId(binding.getEntityId())
                .entityType(binding.getEntityType() != null
                        ? binding.getEntityType().name() : null)
                .entityName(binding.getEntityName())
                .createdAt(binding.getCreatedAt())
                .build();
    }

    private AgentDTO toAgentDTO(AgentBound binding) {
        if (binding == null) {
            return null;
        }
        return AgentDTO.builder()
                .id(binding.getAgentId())
                .name(binding.getAgentName())
                .role(binding.getAgentRole())
                .hierarchyLevel(binding.getHierarchyLevel() != null
                        ? binding.getHierarchyLevel().name() : null)
                .specialty(binding.getAgentSpecialty())
                .modelName(binding.getAgentModelName())
                .providerModelId(binding.getAgentProviderModelId())
                .temperature(binding.getAgentTemperature())
                .topP(binding.getAgentTopP())
                .maxTokens(binding.getAgentMaxTokens())
                .boundId(binding.getId())
                .promptTemplateContent(binding.getPromptTemplateContent())
                .build();
    }
}
