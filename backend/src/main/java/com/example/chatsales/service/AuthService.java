package com.example.chatsales.service;

import com.example.chatsales.dto.AuthRequest;
import com.example.chatsales.dto.AuthResponse;
import com.example.chatsales.entity.User;

public interface AuthService {
    
    /**
     * 用户登录方法
     * @param request 包含用户名和密码的登录请求
     * @return 登录响应，包含成功/失败状态和令牌(如果成功)
     */
    AuthResponse login(AuthRequest request);
    
    /**
     * 注册新用户
     * @param request 包含用户信息的请求
     * @return 注册结果
     */
    AuthResponse register(AuthRequest request);
    
    /**
     * 验证JWT令牌
     * @param token JWT令牌字符串
     * @return 验证结果
     */
    boolean validateToken(String token);
    
    /**
     * 从令牌中获取用户信息
     * @param token JWT令牌
     * @return 用户信息
     */
    User getUserFromToken(String token);
    
    /**
     * 刷新JWT令牌
     * @param refreshToken 刷新令牌
     * @return 新的访问令牌
     */
    String refreshToken(String refreshToken);
} 