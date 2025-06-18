package com.example.chatsales.util;

import com.example.chatsales.dto.AuthRequest;
import com.example.chatsales.dto.AuthResponse;
import com.example.chatsales.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户数据导入工具类
 * 可以通过命令行参数指定需要导入用户数据
 * 使用方法: java -jar app.jar --spring.profiles.active=import
 */
@Component
@Profile("import")
public class UserImportUtil implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(UserImportUtil.class);
    
    private final AuthService authService;
    
    public UserImportUtil(AuthService authService) {
        this.authService = authService;
    }
    
    @Override
    public void run(String... args) throws Exception {
        logger.info("开始导入用户数据...");
        
        importUsersFromCsv();
        
        logger.info("用户数据导入完成，应用将自动退出");
        // 导入完成后退出应用
        System.exit(0);
    }
    
    /**
     * 从CSV文件导入用户数据
     */
    private void importUsersFromCsv() {
        Path csvPath = Paths.get("documents/userInfo.csv");
        List<String> results = new ArrayList<>();
        int successCount = 0;
        int failedCount = 0;
        
        try (BufferedReader br = new BufferedReader(new FileReader(csvPath.toFile()))) {
            // 跳过CSV头行
            String line = br.readLine();
            logger.info("读取CSV文件: {}", csvPath.toAbsolutePath());
            
            // 读取每一行数据
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 3) {
                    String username = data[0].trim();
                    String description = data[1].trim();
                    String password = data[2].trim();
                    
                    // 创建用户注册请求
                    AuthRequest request = new AuthRequest();
                    request.setUsername(username);
                    request.setPassword(password);
                    
                    try {
                        // 调用注册接口
                        AuthResponse response = authService.register(request);
                        
                        if (response.isSuccess()) {
                            results.add(String.format("成功: 用户 %s (%s) 创建成功", username, description));
                            successCount++;
                        } else {
                            results.add(String.format("失败: 用户 %s (%s) 创建失败: %s", username, description, response.getMessage()));
                            failedCount++;
                        }
                    } catch (Exception e) {
                        results.add(String.format("错误: 用户 %s (%s) 创建异常: %s", username, description, e.getMessage()));
                        failedCount++;
                    }
                }
            }
        } catch (IOException e) {
            logger.error("读取CSV文件失败", e);
            return;
        }
        
        // 输出结果
        logger.info("用户导入完成: {} 成功, {} 失败", successCount, failedCount);
        results.forEach(logger::info);
    }
    
    /**
     * 独立运行的入口方法
     */
    public static void main(String[] args) {
        String[] importArgs = {"--spring.profiles.active=import"};
        SpringApplication.run(UserImportUtil.class, importArgs);
    }
} 