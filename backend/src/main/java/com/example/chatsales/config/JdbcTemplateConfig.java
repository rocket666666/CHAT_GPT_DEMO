package com.example.chatsales.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * JdbcTemplate配置类
 * 为MySQL和SQL Server创建相应的JdbcTemplate
 */
@Configuration
public class JdbcTemplateConfig {

    /**
     * 创建主要的MySQL JdbcTemplate
     * @param dataSource MySQL数据源
     * @return JdbcTemplate对象
     */
    @Bean(name = "mysqlJdbcTemplate")
    @Primary
    public JdbcTemplate mysqlJdbcTemplate(@Qualifier("mysqlDataSource") DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        // 配置JdbcTemplate，不限制查询结果行数
        jdbcTemplate.setMaxRows(0);
        // 设置足够长的查询超时时间（30秒）
        jdbcTemplate.setQueryTimeout(30);
        // 启用提取大结果集的功能
        jdbcTemplate.setFetchSize(1000);
        return jdbcTemplate;
    }

    // /**
    //  * 创建SQL Server JdbcTemplate，设置为懒加载
    //  * @param dataSource SQL Server数据源
    //  * @return JdbcTemplate对象
    //  */
    // @Bean(name = "sqlserverJdbcTemplate")
    // @Lazy // 标记为懒加载，仅在需要时初始化
    // public JdbcTemplate sqlserverJdbcTemplate(@Qualifier("sqlserverDataSource") DataSource dataSource) {
    //     JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    //     // 配置JdbcTemplate，不限制查询结果行数
    //     jdbcTemplate.setMaxRows(0);
    //     // 设置足够长的查询超时时间（30秒）
    //     jdbcTemplate.setQueryTimeout(30);
    //     // 启用提取大结果集的功能
    //     jdbcTemplate.setFetchSize(1000);
    //     return jdbcTemplate;
    // }


} 