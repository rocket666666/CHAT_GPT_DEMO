package com.example.chatsales.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = {"com.example.chatsales.repository", "com.example.chatsales.mysql.repository"}
)
public class MysqlDataSourceConfig {
    // 使用Spring Boot的自动配置，不需要手动配置数据源
} 