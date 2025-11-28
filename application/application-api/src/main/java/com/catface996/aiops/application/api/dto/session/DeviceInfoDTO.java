package com.catface996.aiops.application.api.dto.session;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 设备信息数据传输对象
 *
 * <p>用于在应用层和接口层之间传输设备信息。</p>
 *
 * @author AI Assistant
 * @since 2025-01-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceInfoDTO {

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * User Agent
     */
    private String userAgent;

    /**
     * 设备类型（如：Desktop, Mobile, Tablet）
     */
    private String deviceType;

    /**
     * 操作系统
     */
    private String operatingSystem;

    /**
     * 浏览器
     */
    private String browser;
}
