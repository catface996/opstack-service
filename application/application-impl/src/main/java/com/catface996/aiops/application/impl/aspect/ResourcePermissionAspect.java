package com.catface996.aiops.application.impl.aspect;

import com.catface996.aiops.common.annotation.RequireOwnerPermission;
import com.catface996.aiops.common.enums.ResourceErrorCode;
import com.catface996.aiops.common.exception.BusinessException;
import com.catface996.aiops.domain.model.resource.Resource;
import com.catface996.aiops.repository.resource.ResourceRepository;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Optional;

/**
 * 资源权限验证切面
 *
 * <p>拦截标注了 @RequireOwnerPermission 的方法，验证当前用户是否为资源所有者。</p>
 * <p>如果验证失败，抛出403 Forbidden异常。</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>REQ-FR-011: 资源更新权限控制</li>
 *   <li>REQ-FR-012: 资源删除权限控制</li>
 *   <li>REQ-FR-016: 删除资源前验证当前用户是否为该资源的Owner</li>
 *   <li>REQ-NFR-009: 权限控制要求</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-11-30
 */
@Aspect
@Component
public class ResourcePermissionAspect {

    private static final Logger logger = LoggerFactory.getLogger(ResourcePermissionAspect.class);
    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final ResourceRepository resourceRepository;

    public ResourcePermissionAspect(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    /**
     * 在目标方法执行前验证资源所有者权限
     *
     * @param joinPoint 切入点
     * @param requireOwnerPermission 注解
     * @throws BusinessException 如果权限验证失败
     */
    @Before("@annotation(requireOwnerPermission)")
    public void checkOwnerPermission(JoinPoint joinPoint, RequireOwnerPermission requireOwnerPermission) {
        // 获取当前用户信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("权限验证失败：用户未认证");
            throw new BusinessException(ResourceErrorCode.UNAUTHORIZED);
        }

        // 检查是否允许管理员跳过
        if (requireOwnerPermission.allowAdmin() && isAdmin(authentication)) {
            logger.debug("管理员用户跳过所有者权限检查");
            return;
        }

        // 获取资源ID
        Long resourceId = extractResourceId(joinPoint, requireOwnerPermission.resourceIdParam());
        if (resourceId == null) {
            logger.warn("权限验证失败：无法获取资源ID");
            throw new BusinessException(ResourceErrorCode.INVALID_PARAMETER, "资源ID不能为空");
        }

        // 获取当前用户ID
        Long currentUserId = getCurrentUserId(authentication);
        if (currentUserId == null) {
            logger.warn("权限验证失败：无法获取当前用户ID");
            throw new BusinessException(ResourceErrorCode.UNAUTHORIZED);
        }

        // 查询资源并验证所有者
        Optional<Resource> resourceOpt = resourceRepository.findById(resourceId);
        if (resourceOpt.isEmpty()) {
            logger.warn("权限验证失败：资源不存在，resourceId={}", resourceId);
            throw new BusinessException(ResourceErrorCode.RESOURCE_NOT_FOUND);
        }

        Resource resource = resourceOpt.get();
        if (!resource.isOwner(currentUserId)) {
            logger.warn("权限验证失败：用户 {} 不是资源 {} 的所有者", currentUserId, resourceId);
            throw new BusinessException(ResourceErrorCode.FORBIDDEN, "您没有权限操作此资源");
        }

        logger.debug("权限验证通过：用户 {} 是资源 {} 的所有者", currentUserId, resourceId);
    }

    /**
     * 从方法参数中提取资源ID
     *
     * @param joinPoint 切入点
     * @param paramName 参数名称
     * @return 资源ID，如果无法提取返回null
     */
    private Long extractResourceId(JoinPoint joinPoint, String paramName) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Parameter[] parameters = method.getParameters();
        Object[] args = joinPoint.getArgs();

        // 尝试通过参数名匹配
        for (int i = 0; i < parameters.length; i++) {
            String name = parameters[i].getName();
            if (name.equals(paramName) || name.equals("id") || name.equals("resourceId")) {
                Object arg = args[i];
                if (arg instanceof Long) {
                    return (Long) arg;
                } else if (arg instanceof String) {
                    try {
                        return Long.parseLong((String) arg);
                    } catch (NumberFormatException e) {
                        logger.warn("无法将参数 {} 转换为Long: {}", name, arg);
                    }
                }
            }
        }

        // 尝试使用第一个Long类型参数
        for (Object arg : args) {
            if (arg instanceof Long) {
                return (Long) arg;
            }
        }

        return null;
    }

    /**
     * 从Authentication中获取当前用户ID
     *
     * @param authentication 认证信息
     * @return 用户ID，如果无法获取返回null
     */
    private Long getCurrentUserId(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        // 如果principal是字符串（通常是用户ID或用户名）
        if (principal instanceof String) {
            try {
                return Long.parseLong((String) principal);
            } catch (NumberFormatException e) {
                logger.debug("Principal不是数字ID: {}", principal);
            }
        }

        // 如果在JWT token的subject中存储了用户ID
        // 通常JwtAuthenticationToken会将userId放在principal或details中
        if (authentication.getDetails() instanceof Long) {
            return (Long) authentication.getDetails();
        }

        // 尝试从name中获取（某些情况下name可能是userId）
        String name = authentication.getName();
        if (name != null) {
            try {
                return Long.parseLong(name);
            } catch (NumberFormatException e) {
                logger.debug("Authentication name不是数字ID: {}", name);
            }
        }

        return null;
    }

    /**
     * 检查用户是否为管理员
     *
     * @param authentication 认证信息
     * @return true如果是管理员
     */
    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(ROLE_ADMIN::equals);
    }
}
