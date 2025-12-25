package com.catface996.aiops.interface_.http.controller;

import com.catface996.aiops.application.api.dto.common.PageResult;
import com.catface996.aiops.application.api.dto.resource.ResourceAuditLogDTO;
import com.catface996.aiops.application.api.dto.resource.ResourceDTO;
import com.catface996.aiops.application.api.dto.resource.ResourceTypeDTO;
import com.catface996.aiops.application.api.dto.resource.request.CreateResourceRequest;
import com.catface996.aiops.application.api.dto.resource.request.DeleteResourceRequest;
import com.catface996.aiops.application.api.dto.resource.request.GetResourceAuditLogsRequest;
import com.catface996.aiops.application.api.dto.resource.request.GetResourceRequest;
import com.catface996.aiops.application.api.dto.resource.request.ListResourcesRequest;
import com.catface996.aiops.application.api.dto.resource.request.UpdateResourceRequest;
import com.catface996.aiops.application.api.dto.resource.request.UpdateResourceStatusRequest;
import com.catface996.aiops.application.api.service.resource.ResourceApplicationService;
import com.catface996.aiops.interface_.http.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 资源管理控制器
 *
 * <p>提供IT资源管理相关的HTTP接口。</p>
 *
 * <p>接口列表（统一POST方式）：</p>
 * <ul>
 *   <li>POST /api/v1/resources/create - 创建资源</li>
 *   <li>POST /api/v1/resources/list - 查询资源列表</li>
 *   <li>POST /api/v1/resources/detail - 查询资源详情</li>
 *   <li>POST /api/v1/resources/update - 更新资源</li>
 *   <li>POST /api/v1/resources/delete - 删除资源</li>
 *   <li>POST /api/v1/resources/update-status - 更新资源状态</li>
 *   <li>POST /api/v1/resources/audit-logs - 查询审计日志</li>
 *   <li>POST /api/v1/resource-types/list - 查询资源类型列表</li>
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
@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "资源管理", description = "IT资源管理接口：创建、查询、更新、删除、状态管理、审计日志")
public class ResourceController {

    private final ResourceApplicationService resourceApplicationService;

    /**
     * 创建资源
     */
    @PostMapping("/resources/create")
    @Operation(summary = "创建资源", description = "创建新的IT资源，敏感配置将自动加密存储")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "创建成功",
                    content = @Content(schema = @Schema(implementation = ResourceDTO.class))),
            @ApiResponse(responseCode = "400", description = "参数无效"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "409", description = "资源名称已存在")
    })
    public ResponseEntity<Result<ResourceDTO>> createResource(
            @Valid @RequestBody CreateResourceRequest request) {
        log.info("创建资源请求，name: {}", request.getName());

        Long operatorId = getCurrentUserId();
        String operatorName = getCurrentUsername();

        ResourceDTO resource = resourceApplicationService.createResource(request, operatorId, operatorName);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Result.success("资源创建成功", resource));
    }

    /**
     * 查询资源列表
     */
    @PostMapping("/resources/list")
    @Operation(summary = "查询资源列表", description = "分页查询资源列表，支持按类型、状态、关键词过滤")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "401", description = "未认证")
    })
    public ResponseEntity<Result<PageResult<ResourceDTO>>> listResources(
            @Valid @RequestBody ListResourcesRequest request) {

        PageResult<ResourceDTO> result = resourceApplicationService.listResources(request);

        return ResponseEntity.ok(Result.success(result));
    }

    /**
     * 查询资源详情
     */
    @PostMapping("/resources/detail")
    @Operation(summary = "查询资源详情", description = "根据ID查询资源详细信息")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "404", description = "资源不存在")
    })
    public ResponseEntity<Result<ResourceDTO>> getResourceById(
            @Valid @RequestBody GetResourceRequest request) {

        ResourceDTO resource = resourceApplicationService.getResourceById(request.getId());

        if (resource == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Result.error(404001, "资源不存在"));
        }

        return ResponseEntity.ok(Result.success(resource));
    }

    /**
     * 更新资源
     */
    @PostMapping("/resources/update")
    @Operation(summary = "更新资源", description = "更新资源信息，需要Owner或Admin权限")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "400", description = "参数无效"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "403", description = "无权限"),
            @ApiResponse(responseCode = "404", description = "资源不存在"),
            @ApiResponse(responseCode = "409", description = "版本冲突")
    })
    public ResponseEntity<Result<ResourceDTO>> updateResource(
            @Valid @RequestBody UpdateResourceRequest request) {
        log.info("更新资源请求，resourceId: {}", request.getId());

        Long operatorId = getCurrentUserId();
        String operatorName = getCurrentUsername();

        // 权限检查
        if (!resourceApplicationService.checkPermission(request.getId(), operatorId, isAdmin())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Result.error(403001, "无权限操作此资源"));
        }

        ResourceDTO resource = resourceApplicationService.updateResource(request.getId(), request, operatorId, operatorName);

        return ResponseEntity.ok(Result.success("资源更新成功", resource));
    }

    /**
     * 删除资源
     */
    @PostMapping("/resources/delete")
    @Operation(summary = "删除资源", description = "删除资源，需要输入资源名称确认")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "400", description = "资源名称确认不匹配"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "403", description = "无权限"),
            @ApiResponse(responseCode = "404", description = "资源不存在")
    })
    public ResponseEntity<Result<Void>> deleteResource(
            @Valid @RequestBody DeleteResourceRequest request) {
        log.info("删除资源请求，resourceId: {}", request.getId());

        Long operatorId = getCurrentUserId();
        String operatorName = getCurrentUsername();

        // 权限检查
        if (!resourceApplicationService.checkPermission(request.getId(), operatorId, isAdmin())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Result.error(403001, "无权限操作此资源"));
        }

        resourceApplicationService.deleteResource(request.getId(), request, operatorId, operatorName);

        return ResponseEntity.ok(Result.success("资源删除成功", null));
    }

    /**
     * 更新资源状态
     */
    @PostMapping("/resources/update-status")
    @Operation(summary = "更新资源状态", description = "更新资源状态（RUNNING/STOPPED/MAINTENANCE/OFFLINE）")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "400", description = "无效的状态值"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "404", description = "资源不存在"),
            @ApiResponse(responseCode = "409", description = "版本冲突")
    })
    public ResponseEntity<Result<ResourceDTO>> updateResourceStatus(
            @Valid @RequestBody UpdateResourceStatusRequest request) {
        log.info("更新资源状态请求，resourceId: {}, newStatus: {}", request.getId(), request.getStatus());

        Long operatorId = getCurrentUserId();
        String operatorName = getCurrentUsername();

        ResourceDTO resource = resourceApplicationService.updateResourceStatus(request.getId(), request, operatorId, operatorName);

        return ResponseEntity.ok(Result.success("资源状态更新成功", resource));
    }

    /**
     * 查询资源审计日志
     */
    @PostMapping("/resources/audit-logs")
    @Operation(summary = "查询审计日志", description = "查询资源的操作审计日志")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "404", description = "资源不存在")
    })
    public ResponseEntity<Result<PageResult<ResourceAuditLogDTO>>> getResourceAuditLogs(
            @Valid @RequestBody GetResourceAuditLogsRequest request) {

        PageResult<ResourceAuditLogDTO> result = resourceApplicationService.getResourceAuditLogs(
                request.getId(), request.getPage(), request.getSize());

        return ResponseEntity.ok(Result.success(result));
    }

    /**
     * 查询资源类型列表
     */
    @PostMapping("/resource-types/list")
    @Operation(summary = "查询资源类型列表", description = "获取所有可用的资源类型")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "401", description = "未认证")
    })
    public ResponseEntity<Result<List<ResourceTypeDTO>>> getAllResourceTypes() {
        List<ResourceTypeDTO> types = resourceApplicationService.getAllResourceTypes();

        return ResponseEntity.ok(Result.success(types));
    }

    // ===== 私有辅助方法 =====

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() != null) {
            try {
                return Long.parseLong(authentication.getName());
            } catch (NumberFormatException e) {
                log.warn("无法解析用户ID: {}", authentication.getName());
            }
        }
        return null;
    }

    /**
     * 获取当前登录用户名
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getCredentials() != null) {
            return authentication.getCredentials().toString();
        }
        return "unknown";
    }

    /**
     * 检查当前用户是否为管理员
     */
    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getAuthorities().stream()
                    .anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority()));
        }
        return false;
    }
}
