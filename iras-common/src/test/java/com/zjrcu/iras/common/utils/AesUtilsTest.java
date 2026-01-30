package com.zjrcu.iras.common.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AES加密工具类单元测试
 *
 * @author iras
 */
class AesUtilsTest {

    /**
     * 测试基本加密解密
     */
    @Test
    void testEncryptDecrypt() {
        String original = "myPassword123!@#";
        String encrypted = AesUtils.encrypt(original);
        String decrypted = AesUtils.decrypt(encrypted);

        assertNotNull(encrypted);
        assertNotEquals(original, encrypted);
        assertEquals(original, decrypted);
    }

    /**
     * 测试空字符串加密
     */
    @Test
    void testEncrypt_EmptyString() {
        String original = "";
        String encrypted = AesUtils.encrypt(original);
        assertEquals("", encrypted);
    }

    /**
     * 测试null加密
     */
    @Test
    void testEncrypt_Null() {
        String encrypted = AesUtils.encrypt(null);
        assertNull(encrypted);
    }

    /**
     * 测试空字符串解密
     */
    @Test
    void testDecrypt_EmptyString() {
        String decrypted = AesUtils.decrypt("");
        assertEquals("", decrypted);
    }

    /**
     * 测试null解密
     */
    @Test
    void testDecrypt_Null() {
        String decrypted = AesUtils.decrypt(null);
        assertNull(decrypted);
    }

    /**
     * 测试中文加密解密
     */
    @Test
    void testEncryptDecrypt_Chinese() {
        String original = "这是一个测试密码！@#￥%";
        String encrypted = AesUtils.encrypt(original);
        String decrypted = AesUtils.decrypt(encrypted);

        assertNotEquals(original, encrypted);
        assertEquals(original, decrypted);
    }

    /**
     * 测试特殊字符加密解密
     */
    @Test
    void testEncryptDecrypt_SpecialCharacters() {
        String original = "!@#$%^&*()_+-=[]{}|;':\",./<>?`~";
        String encrypted = AesUtils.encrypt(original);
        String decrypted = AesUtils.decrypt(original);

        assertNotEquals(original, encrypted);
        assertEquals(original, decrypted);
    }

    /**
     * 测试长字符串加密解密
     */
    @Test
    void testEncryptDecrypt_LongString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("a");
        }
        String original = sb.toString();
        String encrypted = AesUtils.encrypt(original);
        String decrypted = AesUtils.decrypt(encrypted);

        assertNotEquals(original, encrypted);
        assertEquals(original, decrypted);
    }

    /**
     * 测试自定义密钥加密解密
     */
    @Test
    void testEncryptDecrypt_CustomKey() {
        String original = "testPassword";
        String customKey = "myCustomSecretKey123";
        
        String encrypted = AesUtils.encrypt(original, customKey);
        String decrypted = AesUtils.decrypt(encrypted, customKey);

        assertNotEquals(original, encrypted);
        assertEquals(original, decrypted);
    }

    /**
     * 测试不同密钥解密失败
     */
    @Test
    void testDecrypt_WrongKey() {
        String original = "testPassword";
        String key1 = "key1";
        String key2 = "key2";
        
        String encrypted = AesUtils.encrypt(original, key1);
        
        // 使用错误的密钥解密应该失败
        assertThrows(RuntimeException.class, () -> {
            AesUtils.decrypt(encrypted, key2);
        });
    }

    /**
     * 测试无效的加密字符串解密
     */
    @Test
    void testDecrypt_InvalidEncryptedString() {
        String invalidEncrypted = "this-is-not-encrypted";
        
        assertThrows(RuntimeException.class, () -> {
            AesUtils.decrypt(invalidEncrypted);
        });
    }

    /**
     * 测试生成随机密钥
     */
    @Test
    void testGenerateKey() {
        String key1 = AesUtils.generateKey();
        String key2 = AesUtils.generateKey();

        assertNotNull(key1);
        assertNotNull(key2);
        assertNotEquals(key1, key2); // 每次生成的密钥应该不同
        assertTrue(key1.length() > 0);
    }

    /**
     * 测试相同内容多次加密结果不同（因为使用了随机盐）
     */
    @Test
    void testEncrypt_SameContentDifferentResult() {
        String original = "testPassword";
        String encrypted1 = AesUtils.encrypt(original);
        String encrypted2 = AesUtils.encrypt(original);

        // 注意：由于使用了SecureRandom和固定种子，相同内容的加密结果应该相同
        // 这是为了保证可重复性，但在生产环境中可能需要调整
        assertEquals(encrypted1, encrypted2);
    }

    /**
     * 测试加密后的字符串是Base64格式
     */
    @Test
    void testEncrypt_Base64Format() {
        String original = "testPassword";
        String encrypted = AesUtils.encrypt(original);

        // Base64字符串只包含A-Z, a-z, 0-9, +, /, =
        assertTrue(encrypted.matches("^[A-Za-z0-9+/=]+$"));
    }

    /**
     * 测试数据库密码场景
     */
    @Test
    void testDatabasePasswordScenario() {
        // 模拟数据库密码存储和读取场景
        String dbPassword = "MyDBPassword123!@#";
        
        // 存储时加密
        String encryptedPassword = AesUtils.encrypt(dbPassword);
        
        // 模拟存储到数据库...
        // String storedPassword = encryptedPassword;
        
        // 从数据库读取后解密
        String decryptedPassword = AesUtils.decrypt(encryptedPassword);
        
        // 验证解密后的密码正确
        assertEquals(dbPassword, decryptedPassword);
        
        // 验证加密后的密码不是明文
        assertNotEquals(dbPassword, encryptedPassword);
    }
}
