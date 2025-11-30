package com.catface996.aiops.application.api.service.resource;

import com.catface996.aiops.application.api.dto.common.PageResult;
import com.catface996.aiops.application.api.dto.resource.ResourceAuditLogDTO;
import com.catface996.aiops.application.api.dto.resource.ResourceDTO;
import com.catface996.aiops.application.api.dto.resource.ResourceTypeDTO;
import com.catface996.aiops.application.api.dto.resource.request.CreateResourceRequest;
import com.catface996.aiops.application.api.dto.resource.request.DeleteResourceRequest;
import com.catface996.aiops.application.api.dto.resource.request.ListResourcesRequest;
import com.catface996.aiops.application.api.dto.resource.request.UpdateResourceRequest;
import com.catface996.aiops.application.api.dto.resource.request.UpdateResourceStatusRequest;

import java.util.List;

/**
 * 资源应用服务接口
 *
 * <p>提供资源管理的应用层服务，协调领域层和基础设施层。</p>
 *
 * <p>职责：</p>
 * <ul>
 *   <li>接收和验证请求参数</li>
 *   <li>调用领域服务执行业务逻辑</li>
 *   <li>转换领域对象为DTO返回</li>
 *   <li>处理事务边界</li>
 * </ul>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>REQ-FR-001~028: 资源管理功能</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-11-30
 */
public interface ResourceApplicationService {

    /**
     * 创建资源
     *
     * @param request 创建资源请求
     * @param operatorId 操作人ID
     * @param operatorName 操作人姓名
     * @return 创建的资源DTO
     */
    ResourceDTO createResource(CreateResourceRequest request, Long operatorId, String operatorName);

    /**
     * 查询资源列表（分页）
     *
     * @param request 查询请求
     * @return 分页结果
     */
    PageResult<ResourceDTO> listResources(ListResourcesRequest request);

    /**
     * 根据ID获取资源详情
     *
     * @param resourceId 资源ID
     * @return 资源DTO（如果存在）
     */
    ResourceDTO getResourceById(Long resourceId);

    /**
     * 更新资源
     *
     * @param resourceId 资源ID
     * @param request 更新请求
     * @param operatorId 操作人ID
     * @param operatorName 操作人姓名
     * @return 更新后的资源DTO
     */
    ResourceDTO updateResource(Long resourceId, UpdateResourceRequest request,
                               Long operatorId, String operatorName);

    /**
     * 删除资源
     *
     * @param resourceId 资源ID
     * @param request 删除请求
     * @param operatorId 操作人ID
     * @param operatorName 操作人姓名
     */
    void deleteResource(Long resourceId, DeleteResourceRequest request,
                        Long operatorId, String operatorName);

    /**
     * 更新资源状态
     *
     * @param resourceId 资源ID
     * @param request 更新状态请求
     * @param operatorId 操作人ID
     * @param operatorName 操作人姓名
     * @return 更新后的资源DTO
     */
    ResourceDTO updateResourceStatus(Long resourceId, UpdateResourceStatusRequest request,
                                     Long operatorId, String operatorName);

    /**
     * 获取资源的审计日志
     *
     * @param resourceId 资源ID
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 分页结果
     */
    PageResult<ResourceAuditLogDTO> getResourceAuditLogs(Long resourceId, int page, int size);

    /**
     * 获取所有资源类型
     *
     * @return 资源类型列表
     */
    List<ResourceTypeDTO> getAllResourceTypes();

    /**
     * 根据ID获取资源类型
     *
     * @param resourceTypeId 资源类型ID
     * @return 资源类型DTO（如果存在）
     */
    ResourceTypeDTO getResourceTypeById(Long resourceTypeId);

    /**
     * 检查用户是否有资源操作权限
     *
     * @param resourceId 资源ID
     * @param userId 用户ID
     * @param isAdmin 是否为管理员
     * @return true如果有权限
     */
    boolean checkPermission(Long resourceId, Long userId, boolean isAdmin);
}
