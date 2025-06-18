package com.example.chatsales.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

@Component
public class PasswordUtils {
    private static final Logger logger = LoggerFactory.getLogger(PasswordUtils.class);
    
    // PBKDF2 算法参数 (由于没有直接的Argon2id库，我们使用PBKDF2代替)
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    
    private final SecureRandom random = new SecureRandom();

    /**
     * 生成随机盐值
     * @return 盐值的Base64编码字符串
     */
    public String generateSalt() {
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * 使用PBKDF2算法哈希密码
     * @param password 明文密码
     * @param salt 盐值(Base64编码)
     * @return 哈希后的密码(Base64编码)
     */
    public String hashPassword(String password, String salt) {
        try {
            byte[] saltBytes = Base64.getDecoder().decode(salt);
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), saltBytes, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] hash = factory.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            logger.error("密码哈希生成失败", e);
            throw new RuntimeException("密码处理错误", e);
        }
    }

    /**
     * 验证密码是否匹配
     * @param password 要验证的明文密码
     * @param salt 盐值(Base64编码)
     * @param storedHash 存储的密码哈希(Base64编码)
     * @return 密码是否匹配
     */
    public boolean verifyPassword(String password, String salt, String storedHash) {
        String computedHash = hashPassword(password, salt);
        return computedHash.equals(storedHash);
    }
} 