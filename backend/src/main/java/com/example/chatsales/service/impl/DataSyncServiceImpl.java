package com.example.chatsales.service.impl;

import com.example.chatsales.mysql.entity.Sals;
import com.example.chatsales.mysql.repository.SalsRepository;
import com.example.chatsales.service.DataSyncService;
import com.example.chatsales.service.SqlServerQueryService;
import com.example.chatsales.sqlserver.entity.SourceRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Lazy
public class DataSyncServiceImpl implements DataSyncService {

    private static final Logger logger = LoggerFactory.getLogger(DataSyncServiceImpl.class);
    private static final int BATCH_SIZE = 5000;
    private static final int THREAD_POOL_SIZE = 5;

    @Autowired
    @Qualifier("sqlserverJdbcTemplate")
    private JdbcTemplate sqlServerJdbcTemplate;

    @Autowired
    @Qualifier("mysqlJdbcTemplate")
    private JdbcTemplate mysqlJdbcTemplate;

    @Autowired
    @Qualifier("mysqlTransactionManager")
    private PlatformTransactionManager mysqlTransactionManager;

    @Autowired
    private SalsRepository salsRepository;

    @Autowired
    private SqlServerQueryService sqlServerQueryService;
    @Override
    public void syncAllData() {
        logger.info("开始数据同步过程");
        long totalStart = System.currentTimeMillis();

        try {
            // 1. 先用MySQL事务做表操作
            logger.info("Step 1: 执行MySQL表操作");
            long mysqlTableStart = System.currentTimeMillis();
            doMysqlTableOps();
            long mysqlTableEnd = System.currentTimeMillis();
            logger.info("MySQL表操作完成，耗时：{} ms", (mysqlTableEnd - mysqlTableStart));
            List<SourceRecord> pageRecords = sqlServerQueryService.findCustomStockSerial();

            long insertStart = System.currentTimeMillis();
            doMysqlInsert(pageRecords);
            long insertEnd = System.currentTimeMillis();

            long totalEnd = System.currentTimeMillis();
            logger.info("数据同步过程完成，总耗时：{} ms，共同步 {} 条记录",
                    (totalEnd - totalStart), pageRecords.size());
        } catch (Exception e) {
            logger.error("数据同步过程发生错误", e);
            throw new RuntimeException("数据同步失败: " + e.getMessage(), e);
        }
    }
    /**
     * 保存一批数据，每批独立事务
     */
    @Transactional(transactionManager = "mysqlTransactionManager")
    private void saveBatch(List<SourceRecord> batch) {
        try {
            List<Sals> salsList = new ArrayList<>(batch.size());
            for (SourceRecord record : batch) {
                Sals sals = new Sals();
                sals.setSkuCode(record.getSkuCode());
                sals.setSkuName(record.getSkuName());
                sals.setEName(record.getEName());
                //sals.setInvAttr(record.getInvAttr());
                sals.setStorageType(record.getStorageType());
                sals.setQty(record.getQty());
                sals.setMainUnit("KG");
                sals.setUdf31(record.getUdf31());
                sals.setUdf30(record.getUdf30());
                sals.setFreshFrozenFlag(record.getFreshFrozenFlag());
                sals.setUdf27(record.getUdf27());
                sals.setShelfLifeDays(record.getStorageType() != null && record.getStorageType().contains("冷冻") ? 730 : 120);
                sals.setLocCode(record.getLocCode());
                sals.setLocName(record.getLocName());
                salsList.add(sals);
            }
            logger.debug("批量保存 {} 条记录", salsList.size());
            salsRepository.saveAll(salsList);
        } catch (Exception e) {
            logger.error("保存批次数据时发生错误", e);
            throw e;
        }
    }
    // 去掉@Transactional注解，实现每批独立事务
    public void doMysqlInsert(List<SourceRecord> records) {
        try {
            int batchSize = 5000;
            int totalBatches = (records.size() + batchSize - 1) / batchSize;
            logger.debug("开始分批保存数据到MySQL，总记录数：{}，批次大小：{}，总批次：{}",
                    records.size(), batchSize, totalBatches);

            for (int i = 0; i < records.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, records.size());
                int batchNumber = (i / batchSize) + 1;
                logger.debug("处理第 {}/{} 批数据，范围：{} - {}",
                        batchNumber, totalBatches, i, endIndex-1);
                saveBatch(records.subList(i, endIndex));
                logger.debug("第 {} 批数据保存完成", batchNumber);
            }
            logger.debug("所有数据批次保存完成");
        } catch (Exception e) {
            logger.error("MySQL插入数据时发生错误", e);
            throw e;
        }
    }

    @Transactional(transactionManager = "mysqlTransactionManager")
    public void doMysqlTableOps() {
        try {
            logger.debug("执行表操作：删除备份表");
            mysqlJdbcTemplate.execute("DROP TABLE IF EXISTS inventory_pro_bak");
            logger.debug("执行表操作：重命名当前表为备份表");
            mysqlJdbcTemplate.execute("RENAME TABLE inventory_pro TO inventory_pro_bak");
            logger.debug("执行表操作：创建新表");
            mysqlJdbcTemplate.execute("CREATE TABLE inventory_pro LIKE inventory_pro_bak");
            logger.debug("表操作完成");
        } catch (Exception e) {
            logger.error("执行MySQL表操作时发生错误", e);
            throw e;
        }
    }

    @Override
    public Map<String, Object> syncData(String sourceTable, String targetTable, String sqlServerQuery, String mysqlInsert) {
        logger.info("开始数据同步: {} -> {}", sourceTable, targetTable);
        
        long startTime = System.currentTimeMillis();
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        AtomicLong totalTime = new AtomicLong(0);
        
        try {
            // 查询SQL Server数据 - 此处才会首次连接SQL Server数据库
            logger.info("查询SQL Server数据: {}", sqlServerQuery);
            long queryStartTime = System.currentTimeMillis();
            List<Map<String, Object>> allData = sqlServerJdbcTemplate.queryForList(sqlServerQuery);
            long queryEndTime = System.currentTimeMillis();
            logger.info("SQL Server查询完成，获取记录数: {}, 耗时: {}ms", allData.size(), (queryEndTime - queryStartTime));
            
            // 如果没有数据，直接返回
            if (allData.isEmpty()) {
                logger.info("没有数据需要同步");
                return Map.of(
                    "success", true, 
                    "message", "没有数据需要同步",
                    "total", 0,
                    "time_ms", System.currentTimeMillis() - startTime
                );
            }
            
            // 分批处理数据
            final int totalSize = allData.size();
            final int batchCount = (int) Math.ceil((double) totalSize / BATCH_SIZE);
            
            logger.info("开始分批同步数据，总批次: {}, 每批大小: {}", batchCount, BATCH_SIZE);
            
            // 创建线程池和计数器
            ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
            CountDownLatch latch = new CountDownLatch(batchCount);
            
            // 分批处理
            for (int i = 0; i < batchCount; i++) {
                final int batchIndex = i;
                final int fromIndex = i * BATCH_SIZE;
                final int toIndex = Math.min(fromIndex + BATCH_SIZE, totalSize);
                final List<Map<String, Object>> batchData = allData.subList(fromIndex, toIndex);
                
                // 提交到线程池执行
                executor.submit(() -> {
                    long batchStartTime = System.currentTimeMillis();
                    try {
                        int processed = processBatch(batchData, targetTable, mysqlInsert, batchIndex);
                        successCount.addAndGet(processed);
                        logger.info("批次 #{} 完成: {} 条记录, 耗时: {}ms", 
                            batchIndex + 1, processed, (System.currentTimeMillis() - batchStartTime));
                    } catch (Exception e) {
                        logger.error("批次 #{} 处理失败: {}", batchIndex + 1, e.getMessage(), e);
                        failCount.addAndGet(batchData.size());
                    } finally {
                        totalTime.addAndGet(System.currentTimeMillis() - batchStartTime);
                        latch.countDown();
                    }
                });
            }
            
            // 等待所有批次完成
            latch.await();
            executor.shutdown();
            
            // 记录总耗时
            long totalTimeMs = System.currentTimeMillis() - startTime;
            logger.info("数据同步完成. 总记录: {}, 成功: {}, 失败: {}, 总耗时: {}ms", 
                totalSize, successCount.get(), failCount.get(), totalTimeMs);
            
            return Map.of(
                "success", true,
                "total", totalSize,
                "success_count", successCount.get(),
                "fail_count", failCount.get(),
                "time_ms", totalTimeMs,
                "message", String.format("数据同步完成. 总记录: %d, 成功: %d, 失败: %d", 
                    totalSize, successCount.get(), failCount.get())
            );
            
        } catch (Exception e) {
            logger.error("数据同步过程中发生错误: {}", e.getMessage(), e);
            return Map.of(
                "success", false,
                "message", "同步失败: " + e.getMessage(),
                "time_ms", System.currentTimeMillis() - startTime
            );
        }
    }
    
    private int processBatch(List<Map<String, Object>> batchData, String targetTable, String mysqlInsert, int batchIndex) {
        logger.debug("处理批次 #{}, 数据量: {}", batchIndex + 1, batchData.size());
        
        // 创建事务
        TransactionDefinition txDef = new DefaultTransactionDefinition();
        TransactionStatus txStatus = mysqlTransactionManager.getTransaction(txDef);
        
        try {
            // 对批次数据进行处理和插入
            List<Object[]> batchParams = new ArrayList<>();
            
            for (Map<String, Object> row : batchData) {
                // 这里根据mysqlInsert语句的参数顺序，提取相应的字段值
                // 假设mysqlInsert的格式是 "INSERT INTO table (col1, col2, ...) VALUES (?, ?, ...)"
                // 我们需要按顺序提取字段值
                
                // 示例: 简单处理，假设SQL参数与源表字段名相同
                // 实际应用中需要根据具体的SQL语句和表结构来调整
                Object[] params = extractParams(row, mysqlInsert);
                batchParams.add(params);
            }
            
            // 批量执行插入
            int[] updateCounts = mysqlJdbcTemplate.batchUpdate(mysqlInsert, batchParams);
            
            // 提交事务
            mysqlTransactionManager.commit(txStatus);
            
            // 计算成功插入的记录数
            int successCount = 0;
            for (int count : updateCounts) {
                if (count > 0) {
                    successCount++;
                }
            }
            
            return successCount;
        } catch (Exception e) {
            // 回滚事务
            mysqlTransactionManager.rollback(txStatus);
            logger.error("批次 #{} 处理失败，已回滚: {}", batchIndex + 1, e.getMessage(), e);
            throw e;
        }
    }
    
    // 辅助方法，从源数据提取SQL参数
    private Object[] extractParams(Map<String, Object> row, String sqlInsert) {
        // 分析SQL插入语句提取参数名称
        // 这里是简化的示例，实际应用中需要根据具体的SQL语句和表结构来调整
        // 例如，使用正则表达式解析SQL语句，提取字段名
        
        // 假设SQL格式为 INSERT INTO table (col1, col2, col3) VALUES (?, ?, ?)
        // 我们需要提取括号中的字段名
        
        // 简化处理: 直接返回Map中的所有值作为参数
        // 实际应用中需要根据SQL语句的参数顺序提取相应的字段值
        return row.values().toArray();
    }
} 