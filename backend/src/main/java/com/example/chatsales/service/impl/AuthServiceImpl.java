package com.example.chatsales.service.impl;

import com.example.chatsales.dto.AuthRequest;
import com.example.chatsales.dto.AuthResponse;
import com.example.chatsales.entity.User;
import com.example.chatsales.repository.UserRepository;
import com.example.chatsales.security.JwtUtils;
import com.example.chatsales.security.PasswordUtils;
import com.example.chatsales.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);
    
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_TIME_MINUTES = 30;
    private static final int PASSWORD_EXPIRY_DAYS = 90;
    private static final int PASSWORD_WARNING_DAYS = 7;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordUtils passwordUtils;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    @Override
    public AuthResponse login(AuthRequest request) {
        logger.info("用户 {} 尝试登录", request.getUsername());
        
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());
        
        // 用户不存在
        if (userOpt.isEmpty()) {
            logger.warn("登录失败：用户 {} 不存在", request.getUsername());
            return AuthResponse.fail("用户名或密码错误");
        }
        
        User user = userOpt.get();
        
        // 检查账户是否被锁定
        if (user.getLockedUntil() != null && LocalDateTime.now().isBefore(user.getLockedUntil())) {
            LocalDateTime unlockTime = user.getLockedUntil();
            long minutesRemaining = LocalDateTime.now().until(unlockTime, ChronoUnit.MINUTES) + 1;
            
            logger.warn("登录失败：用户 {} 账户被锁定", request.getUsername());
            return AuthResponse.fail("账户已锁定，请" + minutesRemaining + "分钟后再试");
        }
        
        // 验证密码
        boolean passwordValid = passwordUtils.verifyPassword(
                request.getPassword(), 
                user.getSalt(), 
                user.getPasswordHash());
        
        if (!passwordValid) {
            // 增加失败次数
            user.setFailedAttempts(user.getFailedAttempts() + 1);
            
            // 达到最大失败次数，锁定账户
            if (user.getFailedAttempts() >= MAX_FAILED_ATTEMPTS) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_TIME_MINUTES));
                logger.warn("用户 {} 登录失败次数过多，账户已锁定", request.getUsername());
            }
            
            userRepository.save(user);
            return AuthResponse.fail("用户名或密码错误");
        }
        
        // 登录成功，重置失败计数
        user.setFailedAttempts(0);
        user.setLockedUntil(null);
        user.setLastLoginTime(LocalDateTime.now());
        userRepository.save(user);
        
        // 生成JWT令牌
        String token = jwtUtils.generateToken(user.getUsername());
        String refreshToken = jwtUtils.generateRefreshToken(user.getUsername());
        
        // 创建响应
        AuthResponse response = AuthResponse.success(token, user.getAccountName());
        
        // 检查密码是否即将过期
        if (user.getPasswordExpiryTime() != null) {
            LocalDateTime now = LocalDateTime.now();
            if (now.isAfter(user.getPasswordExpiryTime())) {
                // 密码已过期
                response.setPasswordExpiring(true);
                response.setDaysUntilExpiry(0);
                response.setMessage("您的密码已过期，请立即修改");
            } else if (now.plusDays(PASSWORD_WARNING_DAYS).isAfter(user.getPasswordExpiryTime())) {
                // 密码即将过期
                long daysRemaining = now.until(user.getPasswordExpiryTime(), ChronoUnit.DAYS) + 1;
                response.setPasswordExpiring(true);
                response.setDaysUntilExpiry((int) daysRemaining);
                response.setMessage("您的密码将在" + daysRemaining + "天后过期，请及时修改");
            }
        }
        
        logger.info("用户 {} 登录成功", request.getUsername());
        return response;
    }
    
    @Override
    public AuthResponse register(AuthRequest request) {
        logger.info("尝试注册新用户: {}", request.getUsername());
        
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            logger.warn("注册失败：用户名 {} 已存在", request.getUsername());
            return AuthResponse.fail("用户名已存在");
        }
        
        // 创建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setAccountName(request.getAccountName());
        // 生成盐值和密码哈希
        String salt = passwordUtils.generateSalt();
        String passwordHash = passwordUtils.hashPassword(request.getPassword(), salt);
        
        user.setSalt(salt);
        user.setPasswordHash(passwordHash);
        user.setCreatedAt(LocalDateTime.now());
        
        // 设置密码过期时间
        user.setPasswordExpiryTime(LocalDateTime.now().plusDays(PASSWORD_EXPIRY_DAYS));
        
        // 保存用户
        userRepository.save(user);
        
        // 生成JWT令牌
        String token = jwtUtils.generateToken(user.getUsername());
        
        logger.info("用户 {} 注册成功", request.getUsername());
        return AuthResponse.success(token, user.getUsername());
    }
    
    @Override
    public boolean validateToken(String token) {
        return jwtUtils.validateToken(token);
    }
    
    @Override
    public User getUserFromToken(String token) {
        String username = jwtUtils.getUsernameFromToken(token);
        return userRepository.findByUsername(username).orElse(null);
    }
    
    @Override
    public String refreshToken(String refreshToken) {
        // 验证刷新令牌有效性
        if (!jwtUtils.validateToken(refreshToken)) {
            logger.warn("刷新令牌无效");
            return null;
        }
        
        // 从刷新令牌获取用户名
        String username = jwtUtils.getUsernameFromToken(refreshToken);
        
        // 生成新的访问令牌
        return jwtUtils.generateToken(username);
    }
} 