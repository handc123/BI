package com.zjrcu.iras.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES加密解密工具类
 * 用于敏感数据的加密存储，如数据源密码
 *
 * @author iras
 */
public class AesUtils {
    private static final Logger log = LoggerFactory.getLogger(AesUtils.class);

    /**
     * 加密算法
     */
    private static final String ALGORITHM = "AES";

    /**
     * 加密模式和填充方式
     */
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";

    /**
     * 密钥长度（256位）
     */
    private static final int KEY_SIZE = 256;

    /**
     * 默认密钥（实际使用时应从配置文件读取）
     */
    private static final String DEFAULT_KEY = "IRAS_BI_PLATFORM_SECRET_KEY_2024";

    /**
     * 加密字符串
     *
     * @param content 待加密内容
     * @return 加密后的Base64字符串
     */
    public static String encrypt(String content) {
        return encrypt(content, DEFAULT_KEY);
    }

    /**
     * 加密字符串
     *
     * @param content 待加密内容
     * @param key     密钥
     * @return 加密后的Base64字符串
     */
    public static String encrypt(String content, String key) {
        try {
            if (StringUtils.isEmpty(content)) {
                return content;
            }

            SecretKeySpec secretKey = getSecretKey(key);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            byte[] encrypted = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            log.error("AES加密失败: {}", e.getMessage());
            throw new RuntimeException("数据加密失败", e);
        }
    }

    /**
     * 解密字符串
     *
     * @param encryptedContent 加密后的Base64字符串
     * @return 解密后的原始字符串
     */
    public static String decrypt(String encryptedContent) {
        return decrypt(encryptedContent, DEFAULT_KEY);
    }

    /**
     * 解密字符串
     *
     * @param encryptedContent 加密后的Base64字符串
     * @param key              密钥
     * @return 解密后的原始字符串
     */
    public static String decrypt(String encryptedContent, String key) {
        try {
            if (StringUtils.isEmpty(encryptedContent)) {
                return encryptedContent;
            }

            SecretKeySpec secretKey = getSecretKey(key);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedContent));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("AES解密失败: {}", e.getMessage());
            throw new RuntimeException("数据解密失败", e);
        }
    }

    /**
     * 生成密钥
     *
     * @param key 原始密钥字符串
     * @return SecretKeySpec对象
     */
    private static SecretKeySpec getSecretKey(String key) {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            secureRandom.setSeed(key.getBytes(StandardCharsets.UTF_8));
            keyGenerator.init(KEY_SIZE, secureRandom);

            SecretKey secretKey = keyGenerator.generateKey();
            return new SecretKeySpec(secretKey.getEncoded(), ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            log.error("生成密钥失败: {}", e.getMessage());
            throw new RuntimeException("生成密钥失败", e);
        }
    }

    /**
     * 生成随机密钥
     *
     * @return Base64编码的密钥字符串
     */
    public static String generateKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(KEY_SIZE);
            SecretKey secretKey = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            log.error("生成随机密钥失败: {}", e.getMessage());
            throw new RuntimeException("生成随机密钥失败", e);
        }
    }
}
