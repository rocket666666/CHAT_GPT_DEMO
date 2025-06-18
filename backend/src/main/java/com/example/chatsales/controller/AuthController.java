package com.example.chatsales.controller;

import com.example.chatsales.dto.AuthRequest;
import com.example.chatsales.dto.AuthResponse;
import com.example.chatsales.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // 在生产环境中应该限制为特定域名
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    private AuthService authService;
    
    /**
     * 用户登录接口
     * @param loginRequest 登录请求
     * @return 登录响应
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest loginRequest) {
        logger.info("收到登录请求: {}", loginRequest.getUsername());
        
        AuthResponse response = authService.login(loginRequest);
        
        if (response.isSuccess()) {
            logger.info("用户 {} 登录成功", loginRequest.getUsername());
            return ResponseEntity.ok(response);
        } else {
            logger.warn("用户 {} 登录失败: {}", loginRequest.getUsername(), response.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
    
    /**
     * 用户注册接口
     * @param registerRequest 注册请求
     * @return 注册响应
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody AuthRequest registerRequest) {
        logger.info("收到注册请求: {}", registerRequest.getUsername());
        
        AuthResponse response = authService.register(registerRequest);
        
        if (response.isSuccess()) {
            logger.info("用户 {} 注册成功", registerRequest.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            logger.warn("用户 {} 注册失败: {}", registerRequest.getUsername(), response.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 从CSV文件批量导入用户
     * @return 导入结果
     */
    @PostMapping("/import-users")
    public ResponseEntity<?> importUsersFromCsv() {
        logger.info("开始从CSV文件导入用户");
        
        Path csvPath = Paths.get("documents/userInfo.csv");
        List<Map<String, Object>> results = new ArrayList<>();
        int successCount = 0;
        int failedCount = 0;
        
        try (BufferedReader br = new BufferedReader(new FileReader(csvPath.toFile()))) {
            // 读取CSV头行
            String headerLine = br.readLine();
            boolean isNewFormat = headerLine.contains("username");
            
            // 基于原始CSV格式(login_name,user_description,password)
            // 读取每一行数据
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 3) {
                    String username = data[0].trim();
                    String description = data[1].trim();
                    String password = data[2].trim();
                    
                    // 对于包含引号的字段进行特殊处理
                    if (description.startsWith("\"") && !description.endsWith("\"")) {
                        // 处理多列合并的情况，如 "Lin, Michael"
                        StringBuilder fullDescription = new StringBuilder(description);
                        for (int i = 2; i < data.length - 1; i++) {
                            fullDescription.append(",").append(data[i]);
                            // 向后移动一列
                            if (i == data.length - 2) {
                                password = data[i + 1].trim();
                            }
                        }
                        
                        description = fullDescription.toString();
                        // 清理首尾引号
                        if (description.startsWith("\"")) {
                            description = description.substring(1);
                        }
                        if (description.endsWith("\"")) {
                            description = description.substring(0, description.length() - 1);
                        }
                    }
                    
                    // 创建用户注册请求
                    AuthRequest request = new AuthRequest();
                    request.setUsername(username);
                    request.setPassword(password);
                    request.setAccountName(description);
                    Map<String, Object> result = new HashMap<>();
                    result.put("username", username);
                    result.put("description", description);
                    
                    try {
                        // 调用注册接口
                        AuthResponse response = authService.register(request);
                        
                        if (response.isSuccess()) {
                            result.put("status", "success");
                            result.put("message", "用户创建成功");
                            successCount++;
                        } else {
                            result.put("status", "failed");
                            result.put("message", response.getMessage());
                            failedCount++;
                        }
                    } catch (Exception e) {
                        result.put("status", "error");
                        result.put("message", e.getMessage());
                        failedCount++;
                    }
                    
                    results.add(result);
                }
            }
        } catch (IOException e) {
            logger.error("读取CSV文件失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "导入用户失败: " + e.getMessage(),
                            "success", false
                    ));
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", String.format("导入完成: %d 成功, %d 失败", successCount, failedCount));
        response.put("results", results);
        
        logger.info("用户导入完成: {} 成功, {} 失败", successCount, failedCount);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 批量导入用户的main方法，可以直接运行
     */
    public static void main(String[] args) {
        // 注意：这个方法需要在Spring上下文中运行才能访问authService
        // 可以通过Spring Boot启动类来运行
        logger.info("请通过调用API端点/api/auth/import-users来导入用户");
    }
    
    /**
     * 刷新令牌接口
     * @param request 包含刷新令牌的请求
     * @return 新的访问令牌
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        
        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "刷新令牌不能为空"));
        }
        
        String newToken = authService.refreshToken(refreshToken);
        
        if (newToken != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("token", newToken);
            response.put("success", true);
            
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "无效的刷新令牌", "success", false));
        }
    }
    
    /**
     * 验证令牌接口
     * @param token JWT令牌
     * @return 验证结果
     */
    @GetMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestParam String token) {
        boolean valid = authService.validateToken(token);
        
        if (valid) {
            return ResponseEntity.ok(Map.of("valid", true));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("valid", false, "message", "令牌无效或已过期"));
        }
    }
    
    /**
     * 用于测试的账户创建，仅在开发环境使用
     * @return 创建结果
     */
    @PostMapping("/init-admin")
    public ResponseEntity<?> createAdminUser() {
        // 测试环境方便测试，创建一个默认管理员账户
        AuthRequest request = new AuthRequest();
        request.setUsername("admin");
        request.setPassword("admin");
        
        if (!authService.login(request).isSuccess()) {
            AuthResponse response = authService.register(request);
            if (response.isSuccess()) {
                return ResponseEntity.ok(Map.of(
                        "message", "管理员账户创建成功",
                        "username", "admin",
                        "password", "admin"
                ));
            } else {
                return ResponseEntity.internalServerError()
                        .body(Map.of("message", "管理员账户创建失败: " + response.getMessage()));
            }
        } else {
            return ResponseEntity.ok(Map.of("message", "管理员账户已存在"));
        }
    }
} 