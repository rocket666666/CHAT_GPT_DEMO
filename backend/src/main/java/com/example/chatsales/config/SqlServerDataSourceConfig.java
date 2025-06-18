package com.example.chatsales.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
// 移除 @EnableJpaRepositories 注解，因为我们不再使用JPA访问SQL Server
public class SqlServerDataSourceConfig {
    
    @Bean(name = "sqlserverDataSource")
    @ConfigurationProperties("custom-datasource.sqlserver")
    @Lazy // 添加懒加载注解，只有在实际使用时才初始化
    public DataSource sqlserverDataSource() {
        HikariDataSource dataSource = (HikariDataSource) DataSourceBuilder.create().build();
        
        // 设置连接初始化模式为按需初始化
        dataSource.setInitializationFailTimeout(-1); // 连接失败时不抛出异常
        dataSource.setMinimumIdle(0); // 设置最小空闲连接为0，避免在启动时就创建连接
        
        return dataSource;
    }

    @Bean(name = "sqlserverEntityManagerFactory")
    @Lazy // 懒加载实体管理器工厂
    public LocalContainerEntityManagerFactoryBean sqlserverEntityManagerFactory(EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(sqlserverDataSource())
                .packages("com.example.chatsales.sqlserver.entity") // 保留这行以支持事务管理
                .persistenceUnit("sqlserver")
                .build();
    }

    @Bean(name = "sqlserverTransactionManager")
    @Lazy // 懒加载事务管理器
    public PlatformTransactionManager sqlserverTransactionManager(
            @Qualifier("sqlserverEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
} 