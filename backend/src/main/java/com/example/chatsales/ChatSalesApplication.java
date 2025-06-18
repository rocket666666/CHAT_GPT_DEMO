package com.example.chatsales;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync  // 启用异步执行
@EnableScheduling  // 启用定时任务支持
public class ChatSalesApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChatSalesApplication.class, args);
    }
} 