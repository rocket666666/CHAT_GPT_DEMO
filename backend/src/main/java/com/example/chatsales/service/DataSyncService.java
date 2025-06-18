package com.example.chatsales.service;

import java.util.Map;

/**
 * 数据同步服务接口
 * 负责从SQL Server同步数据到MySQL
 */
public interface DataSyncService {

    void syncAllData();

    /**
     * 同步数据
     *
     * @param sourceTable    SQL Server源表名
     * @param targetTable    MySQL目标表名
     * @param sqlServerQuery 查询SQL Server数据的SQL语句
     * @param mysqlInsert    向MySQL插入数据的SQL语句
     * @return 同步结果，包含成功/失败记录数和执行时间等信息
     */
    Map<String, Object> syncData(String sourceTable, String targetTable, String sqlServerQuery, String mysqlInsert);
}