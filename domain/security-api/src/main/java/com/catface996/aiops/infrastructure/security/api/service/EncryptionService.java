package com.catface996.aiops.infrastructure.security.api.service;

/**
 * 加密服务接口
 *
 * <p>提供数据加密和解密功能，用于保护敏感配置信息。</p>
 * <p>实现使用 AES-256 加密算法。</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>REQ-FR-004: 资源敏感配置加密存储</li>
 *   <li>REQ-FR-014: 资源敏感信息更新时重新加密</li>
 *   <li>REQ-NFR-008: 数据安全要求</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-11-30
 */
public interface EncryptionService {

    /**
     * 加密数据
     *
     * <p>使用 AES-256 算法对明文进行加密。</p>
     *
     * @param plaintext 明文数据
     * @return 加密后的密文（Base64编码）
     * @throws IllegalArgumentException 如果输入为null
     * @throws RuntimeException 如果加密过程中发生错误
     */
    String encrypt(String plaintext);

    /**
     * 解密数据
     *
     * <p>使用 AES-256 算法对密文进行解密。</p>
     *
     * @param ciphertext 密文数据（Base64编码）
     * @return 解密后的明文
     * @throws IllegalArgumentException 如果输入为null或格式无效
     * @throws RuntimeException 如果解密过程中发生错误
     */
    String decrypt(String ciphertext);

    /**
     * 检查数据是否已加密
     *
     * <p>通过检查数据格式判断是否为加密后的密文。</p>
     *
     * @param data 待检查的数据
     * @return true 如果数据已加密，否则 false
     */
    boolean isEncrypted(String data);
}
