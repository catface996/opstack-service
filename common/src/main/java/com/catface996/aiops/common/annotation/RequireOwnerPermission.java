package com.catface996.aiops.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 要求资源所有者权限注解
 *
 * <p>标注在方法上，表示该方法需要验证当前用户是否为资源的所有者。</p>
 * <p>只有资源的创建者（Owner）才能执行被标注的操作。</p>
 *
 * <p>使用示例：</p>
 * <pre>{@code
 * @RequireOwnerPermission
 * public void updateResource(Long resourceId, UpdateResourceCommand command) {
 *     // 只有资源所有者才能执行此方法
 * }
 * }</pre>
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
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireOwnerPermission {

    /**
     * 资源ID参数名称
     *
     * <p>指定方法参数中表示资源ID的参数名称。</p>
     * <p>默认为"resourceId"，也可以是"id"。</p>
     *
     * @return 资源ID参数名称
     */
    String resourceIdParam() default "resourceId";

    /**
     * 是否允许管理员跳过权限检查
     *
     * <p>如果设置为true，管理员（ROLE_ADMIN）可以操作任何资源。</p>
     * <p>默认为false，即管理员也需要是资源所有者。</p>
     *
     * @return 是否允许管理员跳过
     */
    boolean allowAdmin() default false;
}
