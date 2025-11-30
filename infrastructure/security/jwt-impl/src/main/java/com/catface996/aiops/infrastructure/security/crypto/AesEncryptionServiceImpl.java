package com.catface996.aiops.infrastructure.security.crypto;

import com.catface996.aiops.infrastructure.security.api.service.EncryptionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-256 加密服务实现
 *
 * <p>使用 AES-256-GCM 模式进行加密和解密。</p>
 * <p>GCM模式提供认证加密，保证数据的机密性和完整性。</p>
 *
 * <p>加密格式：Base64(IV + 密文 + AuthTag)</p>
 * <ul>
 *   <li>IV: 12字节初始化向量</li>
 *   <li>AuthTag: 128位认证标签（GCM模式自动附加）</li>
 * </ul>
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
@Service
public class AesEncryptionServiceImpl implements EncryptionService {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    private static final String ENCRYPTION_PREFIX = "ENC:";

    private final SecretKey secretKey;
    private final SecureRandom secureRandom;

    /**
     * 构造函数
     *
     * @param encryptionKey AES-256加密密钥（32字节，Base64编码或原始字符串）
     */
    public AesEncryptionServiceImpl(
            @Value("${aiops.security.encryption.key:defaultEncryptionKey12345678901234}") String encryptionKey) {
        this.secretKey = createSecretKey(encryptionKey);
        this.secureRandom = new SecureRandom();
    }

    @Override
    public String encrypt(String plaintext) {
        if (plaintext == null) {
            throw new IllegalArgumentException("明文不能为null");
        }
        if (plaintext.isEmpty()) {
            return plaintext;
        }

        try {
            // 生成随机IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);

            // 初始化加密器
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            // 执行加密
            byte[] encryptedData = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            // 拼接 IV + 密文
            byte[] combined = new byte[iv.length + encryptedData.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedData, 0, combined, iv.length, encryptedData.length);

            // 返回 Base64 编码的结果，带前缀标识
            return ENCRYPTION_PREFIX + Base64.getEncoder().encodeToString(combined);

        } catch (Exception e) {
            throw new RuntimeException("加密失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String decrypt(String ciphertext) {
        if (ciphertext == null) {
            throw new IllegalArgumentException("密文不能为null");
        }
        if (ciphertext.isEmpty()) {
            return ciphertext;
        }

        // 检查是否为加密数据
        if (!isEncrypted(ciphertext)) {
            // 如果不是加密数据，直接返回原文
            return ciphertext;
        }

        try {
            // 移除前缀并解码
            String base64Data = ciphertext.substring(ENCRYPTION_PREFIX.length());
            byte[] combined = Base64.getDecoder().decode(base64Data);

            // 检查数据长度
            if (combined.length < GCM_IV_LENGTH) {
                throw new IllegalArgumentException("密文格式无效：长度不足");
            }

            // 提取 IV 和密文
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] encryptedData = new byte[combined.length - GCM_IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, iv.length);
            System.arraycopy(combined, iv.length, encryptedData, 0, encryptedData.length);

            // 初始化解密器
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            // 执行解密
            byte[] decryptedData = cipher.doFinal(encryptedData);

            return new String(decryptedData, StandardCharsets.UTF_8);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("解密失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isEncrypted(String data) {
        if (data == null || data.isEmpty()) {
            return false;
        }
        return data.startsWith(ENCRYPTION_PREFIX);
    }

    /**
     * 从配置的密钥字符串创建 SecretKey
     *
     * <p>如果密钥不足32字节，会通过填充或截断处理到32字节。</p>
     *
     * @param keyString 密钥字符串
     * @return SecretKey 对象
     */
    private SecretKey createSecretKey(String keyString) {
        byte[] keyBytes;

        // 尝试Base64解码
        try {
            keyBytes = Base64.getDecoder().decode(keyString);
        } catch (IllegalArgumentException e) {
            // 如果不是Base64，使用UTF-8字节
            keyBytes = keyString.getBytes(StandardCharsets.UTF_8);
        }

        // 确保密钥长度为32字节（256位）
        byte[] key32 = new byte[32];
        if (keyBytes.length >= 32) {
            System.arraycopy(keyBytes, 0, key32, 0, 32);
        } else {
            // 密钥不足32字节，进行填充
            System.arraycopy(keyBytes, 0, key32, 0, keyBytes.length);
            // 剩余部分用密钥循环填充
            for (int i = keyBytes.length; i < 32; i++) {
                key32[i] = keyBytes[i % keyBytes.length];
            }
        }

        return new SecretKeySpec(key32, ALGORITHM);
    }
}
