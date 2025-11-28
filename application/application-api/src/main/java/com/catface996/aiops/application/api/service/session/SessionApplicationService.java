package com.catface996.aiops.application.api.service.session;

import com.catface996.aiops.application.api.dto.session.DeviceInfoDTO;
import com.catface996.aiops.application.api.dto.session.SessionDTO;
import com.catface996.aiops.application.api.dto.session.SessionValidationResultDTO;

import java.util.List;

/**
 * 会话应用服务接口
 *
 * <p>提供会话管理的应用层方法，包括：</p>
 * <ul>
 *   <li>会话创建、验证、销毁</li>
 *   <li>多设备会话管理</li>
 *   <li>权限检查</li>
 * </ul>
 *
 * <p>本接口作为应用层的统一入口，协调领域服务完成业务流程。</p>
 *
 * <p>职责：</p>
 * <ul>
 *   <li>编排会话管理业务流程</li>
 *   <li>控制事务边界</li>
 *   <li>执行权限检查</li>
 *   <li>转换DTO和领域模型</li>
 *   <li>记录审计日志</li>
 * </ul>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F01-4: 会话管理功能</li>
 *   <li>REQ 1.1, 1.2, 1.3, 1.4, 1.5: 会话生命周期管理</li>
 *   <li>REQ 3.1, 3.2, 3.3: 多设备会话管理</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-01-28
 */
public interface SessionApplicationService {

    /**
     * 创建会话
     *
     * <p>为用户创建新的会话，包括以下流程：</p>
     * <ol>
     *   <li>检查并清理超限会话</li>
     *   <li>生成UUID作为会话标识符</li>
     *   <li>保存会话到MySQL和Redis</li>
     *   <li>记录审计日志</li>
     * </ol>
     *
     * @param userId 用户ID
     * @param deviceInfo 设备信息DTO
     * @param rememberMe 是否记住我
     * @return 创建的会话DTO
     * @throws IllegalArgumentException 当参数无效时
     */
    SessionDTO createSession(Long userId, DeviceInfoDTO deviceInfo, boolean rememberMe);

    /**
     * 验证会话
     *
     * <p>验证会话有效性，包括以下流程：</p>
     * <ol>
     *   <li>从缓存或数据库获取会话</li>
     *   <li>检查绝对超时和空闲超时</li>
     *   <li>更新最后活动时间</li>
     *   <li>返回验证结果（可能包含警告信息）</li>
     * </ol>
     *
     * @param sessionId 会话ID
     * @return 验证结果DTO
     */
    SessionValidationResultDTO validateSession(String sessionId);

    /**
     * 销毁会话
     *
     * <p>销毁指定的会话，包括以下流程：</p>
     * <ol>
     *   <li>从MySQL删除会话</li>
     *   <li>从Redis删除会话缓存</li>
     *   <li>记录审计日志</li>
     * </ol>
     *
     * @param sessionId 会话ID
     * @throws IllegalArgumentException 当sessionId为空时
     */
    void destroySession(String sessionId);

    /**
     * 获取当前用户的所有会话
     *
     * <p>查询用户的所有活跃会话，用于多设备会话管理。</p>
     *
     * @param userId 用户ID
     * @return 会话DTO列表
     * @throws IllegalArgumentException 当userId为null时
     */
    List<SessionDTO> getUserSessions(Long userId);

    /**
     * 终止指定会话
     *
     * <p>终止指定的会话，需要权限检查。</p>
     *
     * @param sessionId 要终止的会话ID
     * @param currentUserId 当前用户ID（用于权限检查）
     * @throws IllegalArgumentException 当参数无效时
     * @throws com.catface996.aiops.common.exception.BusinessException 当无权限终止该会话时（AUTHZ_001）
     */
    void terminateSession(String sessionId, Long currentUserId);

    /**
     * 终止当前用户的其他会话
     *
     * <p>终止用户除当前会话外的所有会话。</p>
     *
     * @param currentSessionId 当前会话ID（保留）
     * @param userId 用户ID
     * @return 终止的会话数量
     * @throws IllegalArgumentException 当参数无效时
     */
    int terminateOtherSessions(String currentSessionId, Long userId);

    /**
     * 刷新访问令牌
     *
     * <p>使用刷新令牌获取新的访问令牌。</p>
     *
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @param username 用户名
     * @param role 用户角色
     * @param rememberMe 是否记住我
     * @return 新的访问令牌
     * @throws com.catface996.aiops.common.exception.BusinessException 当会话无效或已过期时
     */
    String refreshAccessToken(String sessionId, Long userId, String username, String role, boolean rememberMe);
}
