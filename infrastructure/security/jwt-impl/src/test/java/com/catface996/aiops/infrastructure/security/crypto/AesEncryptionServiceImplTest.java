package com.catface996.aiops.infrastructure.security.crypto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AES-256加密服务单元测试
 *
 * <p>验证加密服务的正确性和安全性。</p>
 *
 * @author AI Assistant
 * @since 2025-11-30
 */
@DisplayName("AES-256加密服务测试")
class AesEncryptionServiceImplTest {

    private AesEncryptionServiceImpl encryptionService;

    @BeforeEach
    void setUp() {
        // 使用32字符的测试密钥（256位）
        encryptionService = new AesEncryptionServiceImpl("TestEncryptionKey123456789012345");
    }

    @Test
    @DisplayName("加密后可以正确解密")
    void encrypt_decrypt_shouldReturnOriginalText() {
        // Given
        String plaintext = "这是一个敏感配置信息";

        // When
        String encrypted = encryptionService.encrypt(plaintext);
        String decrypted = encryptionService.decrypt(encrypted);

        // Then
        assertEquals(plaintext, decrypted);
    }

    @Test
    @DisplayName("加密结果以ENC:前缀开头")
    void encrypt_shouldAddEncPrefix() {
        // Given
        String plaintext = "test data";

        // When
        String encrypted = encryptionService.encrypt(plaintext);

        // Then
        assertTrue(encrypted.startsWith("ENC:"));
    }

    @Test
    @DisplayName("相同明文每次加密结果不同（由于随机IV）")
    void encrypt_sameText_shouldProduceDifferentCiphertext() {
        // Given
        String plaintext = "same text";

        // When
        String encrypted1 = encryptionService.encrypt(plaintext);
        String encrypted2 = encryptionService.encrypt(plaintext);

        // Then
        assertNotEquals(encrypted1, encrypted2);
        // 但解密后应该相同
        assertEquals(plaintext, encryptionService.decrypt(encrypted1));
        assertEquals(plaintext, encryptionService.decrypt(encrypted2));
    }

    @Test
    @DisplayName("加密空字符串返回空字符串")
    void encrypt_emptyString_shouldReturnEmptyString() {
        // When
        String encrypted = encryptionService.encrypt("");

        // Then
        assertEquals("", encrypted);
    }

    @Test
    @DisplayName("加密null应该抛出异常")
    void encrypt_null_shouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> encryptionService.encrypt(null));
    }

    @Test
    @DisplayName("解密null应该抛出异常")
    void decrypt_null_shouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> encryptionService.decrypt(null));
    }

    @Test
    @DisplayName("解密非加密字符串应返回原字符串")
    void decrypt_nonEncryptedString_shouldReturnOriginal() {
        // Given
        String plaintext = "not encrypted";

        // When
        String result = encryptionService.decrypt(plaintext);

        // Then
        assertEquals(plaintext, result);
    }

    @Test
    @DisplayName("isEncrypted检测加密数据")
    void isEncrypted_encryptedData_shouldReturnTrue() {
        // Given
        String encrypted = encryptionService.encrypt("test");

        // When & Then
        assertTrue(encryptionService.isEncrypted(encrypted));
    }

    @Test
    @DisplayName("isEncrypted检测非加密数据")
    void isEncrypted_plainData_shouldReturnFalse() {
        // When & Then
        assertFalse(encryptionService.isEncrypted("plain text"));
        assertFalse(encryptionService.isEncrypted(""));
        assertFalse(encryptionService.isEncrypted(null));
    }

    @Test
    @DisplayName("加密解密JSON数据")
    void encrypt_decrypt_jsonData() {
        // Given
        String jsonData = "{\"host\":\"192.168.1.1\",\"port\":3306,\"password\":\"secret123\"}";

        // When
        String encrypted = encryptionService.encrypt(jsonData);
        String decrypted = encryptionService.decrypt(encrypted);

        // Then
        assertEquals(jsonData, decrypted);
    }

    @Test
    @DisplayName("加密解密特殊字符")
    void encrypt_decrypt_specialCharacters() {
        // Given
        String specialChars = "!@#$%^&*()_+-=[]{}|;':\",./<>?\\n\\t中文日本語한국어";

        // When
        String encrypted = encryptionService.encrypt(specialChars);
        String decrypted = encryptionService.decrypt(encrypted);

        // Then
        assertEquals(specialChars, decrypted);
    }

    @Test
    @DisplayName("加密解密长文本")
    void encrypt_decrypt_longText() {
        // Given
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("This is a long text for testing. ");
        }
        String longText = sb.toString();

        // When
        String encrypted = encryptionService.encrypt(longText);
        String decrypted = encryptionService.decrypt(encrypted);

        // Then
        assertEquals(longText, decrypted);
    }

    @Test
    @DisplayName("使用短密钥可以正常工作")
    void constructor_shortKey_shouldWork() {
        // Given
        AesEncryptionServiceImpl serviceWithShortKey = new AesEncryptionServiceImpl("shortkey");
        String plaintext = "test data";

        // When
        String encrypted = serviceWithShortKey.encrypt(plaintext);
        String decrypted = serviceWithShortKey.decrypt(encrypted);

        // Then
        assertEquals(plaintext, decrypted);
    }

    @Test
    @DisplayName("使用Base64编码密钥")
    void constructor_base64Key_shouldWork() {
        // Given - 32字节密钥的Base64编码
        String base64Key = "VGVzdEVuY3J5cHRpb25LZXkxMjM0NTY3ODkwMTIzNDU=";
        AesEncryptionServiceImpl serviceWithBase64Key = new AesEncryptionServiceImpl(base64Key);
        String plaintext = "test data";

        // When
        String encrypted = serviceWithBase64Key.encrypt(plaintext);
        String decrypted = serviceWithBase64Key.decrypt(encrypted);

        // Then
        assertEquals(plaintext, decrypted);
    }
}
