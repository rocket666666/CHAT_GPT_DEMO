package com.example.chatsales.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    // 默认的密钥，应该在配置文件中设置并使用@Value注入
    @Value("${jwt.secret:thisIsAVerySecureJwtKeyForDevelopmentNeverUseInProduction}")
    private String secret;

    @Value("${jwt.expiration:7200000}") // 默认2小时(毫秒)
    private long expiration;

    @Value("${jwt.refresh-expiration:604800000}") // 默认7天(毫秒)
    private long refreshExpiration;

    /**
     * 从令牌中获取用户名
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * 从令牌中获取过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * 从令牌中获取声明
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 解析令牌获取所有声明
     */
    private Claims getAllClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            // 令牌已过期，但我们仍然返回其主体以便于进行刷新令牌操作
            logger.debug("JWT令牌已过期: {}", e.getMessage());
            return e.getClaims();
        } catch (Exception e) {
            logger.error("JWT令牌解析失败: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 检查令牌是否已过期
     */
    public Boolean isTokenExpired(String token) {
        try {
            final Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            logger.error("检查令牌过期时出错: {}", e.getMessage());
            return true;
        }
    }

    /**
     * 生成令牌
     */
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return doGenerateToken(claims, username, expiration);
    }

    /**
     * 生成刷新令牌
     */
    public String generateRefreshToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("refresh", true);
        return doGenerateToken(claims, username, refreshExpiration);
    }

    /**
     * 创建令牌的核心方法
     */
    private String doGenerateToken(Map<String, Object> claims, String subject, long expirationTime) {
        final Date createdDate = new Date();
        final Date expirationDate = new Date(createdDate.getTime() + expirationTime);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(createdDate)
                .setExpiration(expirationDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 获取签名密钥
     */
    private Key getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 验证令牌
     */
    public Boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            logger.debug("JWT令牌已过期: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("JWT令牌验证失败: {}", e.getMessage());
            return false;
        }
    }
} 