package com.example.chatsales.service;

import com.example.chatsales.sqlserver.entity.SourceRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Lazy  // 添加懒加载注解
public class SqlServerQueryService {
    private static final Logger logger = LoggerFactory.getLogger(SqlServerQueryService.class);
    
    @Autowired
    @Qualifier("sqlserverJdbcTemplate")
    private JdbcTemplate sqlserverJdbcTemplate;
    
    // SQL Server分页查询SQL
    private static final String PAGE_QUERY_SQL = """
        SELECT 
            t.SKU_CODE AS sku_code,
            B.Sku_Name AS sku_name,
            B.E_NAME AS e_name,
            B.INV_ATTR AS inv_attr,
            G.CODE_NAME AS storage_type,
            t.qty AS qty,
            'KG' AS main_unit,
            t.UDF31 AS udf31,
            t.UDF30 AS udf30,
            CASE WHEN t.content = '鲜转冻' THEN '鲜转冻' ELSE '' END AS fresh_frozen_flag,
            t.UDF27 AS udf27,
            CASE WHEN G.CODE_NAME LIKE '%冷冻%' THEN 730 ELSE 120 END AS shelf_life_days,
            t.LOC_CODE AS loc_code,
            D.Loc_Name AS loc_name
        FROM 
            WMS_STOCKSERIAL t
        LEFT JOIN 
            base_sku_item B ON t.Sku_Code = B.Sku_Code
        LEFT JOIN 
            base_location_item D ON t.Loc_Code = D.Loc_Code
        LEFT JOIN 
            SYS_CODE G ON B.STORAGETYPE = G.CODE AND G.CODE_ID = 'STORAGE_TYPE'
        WHERE 
            D.Loc_Name LIKE '%ZZZ%' AND B.INV_ATTR = '11'
        ORDER BY
            t.SKU_CODE
        OFFSET ? ROWS
        FETCH NEXT ? ROWS ONLY
    """;
    
    /**
     * 查询所有数据，不限制数量
     */
    public List<SourceRecord> findCustomStockSerial() {
        logger.info("使用普通查询方式查询SQL Server数据");
        String sql = """
            SELECT 
                t.SKU_CODE AS sku_code,
                B.Sku_Name AS sku_name,
                B.E_NAME AS e_name,
                B.INV_ATTR AS inv_attr,
                G.CODE_NAME AS storage_type,
                t.qty AS qty,
                'KG' AS main_unit,
                t.UDF31 AS udf31,
                t.UDF30 AS udf30,
                CASE WHEN t.content = '鲜转冻' THEN '鲜转冻' ELSE '' END AS fresh_frozen_flag,
                t.UDF27 AS udf27,
                CASE WHEN G.CODE_NAME LIKE '%冷冻%' THEN 730 ELSE 120 END AS shelf_life_days,
                t.LOC_CODE AS loc_code,
                D.Loc_Name AS loc_name
            FROM 
                WMS_STOCKSERIAL t
            LEFT JOIN 
                base_sku_item B ON t.Sku_Code = B.Sku_Code
            LEFT JOIN 
                base_location_item D ON t.Loc_Code = D.Loc_Code
            LEFT JOIN 
                SYS_CODE G ON B.STORAGETYPE = G.CODE AND G.CODE_ID = 'STORAGE_TYPE'
            WHERE 
                D.Loc_Name LIKE '%ZZZ%' AND B.INV_ATTR = '11'
        """;
        
        logger.debug("执行SQL: {}", sql);
        
        try {
            // 创建一个支持下划线转驼峰的RowMapper
            BeanPropertyRowMapper<SourceRecord> rowMapper = BeanPropertyRowMapper.newInstance(SourceRecord.class);
            // 启用下划线转驼峰命名
            rowMapper.setPrimitivesDefaultedForNullValue(true);
            
            List<SourceRecord> results = sqlserverJdbcTemplate.query(sql, rowMapper);
            logger.info("SQL Server查询完成，返回 {} 条记录", results.size());
            return results;
        } catch (Exception e) {
            logger.error("SQL Server查询失败", e);
            throw e;
        }
    }
    
    /**
     * 获取单页数据
     * @param pageSize 页大小
     * @param pageNum 页码（从1开始）
     * @return 当前页的数据
     */
    public List<SourceRecord> findPagedData(int pageSize, int pageNum) {
        int offset = (pageNum - 1) * pageSize;
        logger.info("查询SQL Server数据第 {} 页，页大小: {}, 偏移量: {}", pageNum, pageSize, offset);
        
        try {
            BeanPropertyRowMapper<SourceRecord> rowMapper = BeanPropertyRowMapper.newInstance(SourceRecord.class);
            rowMapper.setPrimitivesDefaultedForNullValue(true);
            
            // 统计总时间
            long totalStartTime = System.currentTimeMillis();
            
            // 使用更底层的JDBC API获取更详细的性能指标
            List<SourceRecord> results = new ArrayList<>();
            
            sqlserverJdbcTemplate.execute((java.sql.Connection connection) -> {
                long prepareStartTime = System.currentTimeMillis();
                try (java.sql.PreparedStatement stmt = connection.prepareStatement(PAGE_QUERY_SQL)) {
                    stmt.setInt(1, offset);
                    stmt.setInt(2, pageSize);
                    
                    long executeStartTime = System.currentTimeMillis();
                    long preparationTime = executeStartTime - prepareStartTime;
                    
                    try (java.sql.ResultSet rs = stmt.executeQuery()) {
                        long firstRowTime = System.currentTimeMillis();
                        long executionTime = firstRowTime - executeStartTime;
                        
                        // 处理结果集
                        int rowCount = 0;
                        while (rs.next()) {
                            SourceRecord record = rowMapper.mapRow(rs, rowCount++);
                            if (record != null) {
                                results.add(record);
                            }
                        }
                        
                        long fetchEndTime = System.currentTimeMillis();
                        long fetchTime = fetchEndTime - firstRowTime;
                        
                        logger.info("SQL性能统计 - 页码: {}, SQL准备时间: {}ms, 执行时间: {}ms, 数据获取时间: {}ms, 总时间: {}ms, 行数: {}", 
                                pageNum, 
                                preparationTime,
                                executionTime, 
                                fetchTime, 
                                (fetchEndTime - prepareStartTime),
                                rowCount);
                    }
                }
                return null;
            });
            
            long totalEndTime = System.currentTimeMillis();
            
            logger.info("第 {} 页查询完成，获取到 {} 条记录，总用时: {} 毫秒", 
                    pageNum, results.size(), (totalEndTime - totalStartTime));
            
            return results;
        } catch (Exception e) {
            logger.error("SQL Server分页查询失败", e);
            throw e;
        }
    }
    
    /**
     * 获取总记录数
     * @return 总记录数
     */
    public int getRecordCount() {
        String countSql = """
            SELECT COUNT(1)
            FROM WMS_STOCKSERIAL t
            LEFT JOIN base_sku_item B ON t.Sku_Code = B.Sku_Code
            LEFT JOIN base_location_item D ON t.Loc_Code = D.Loc_Code
            WHERE D.Loc_Name LIKE '%ZZZ%' AND B.INV_ATTR = '11'
        """;
        
        try {
            Integer count = sqlserverJdbcTemplate.queryForObject(countSql, Integer.class);
            logger.info("SQL Server总记录数: {}", count);
            return count != null ? count : 0;
        } catch (Exception e) {
            logger.error("获取SQL Server总记录数失败", e);
            throw e;
        }
    }
    
    /**
     * 分页查询数据，适用于数据量特别大的情况
     * @param pageSize 每页大小
     * @return 所有数据
     */
    public List<SourceRecord> findAllCustomStockSerialByPaging(int pageSize) {
        logger.info("使用分页查询方式查询SQL Server数据，页大小: {}", pageSize);
        List<SourceRecord> allRecords = new ArrayList<>();
        
        logger.debug("分页SQL: {}", PAGE_QUERY_SQL);
        
        try {
            BeanPropertyRowMapper<SourceRecord> rowMapper = BeanPropertyRowMapper.newInstance(SourceRecord.class);
            rowMapper.setPrimitivesDefaultedForNullValue(true);
            
            int offset = 0;
            List<SourceRecord> pageRecords;
            int pageNumber = 1;
            
            do {
                logger.debug("查询第 {} 页数据，偏移量: {}, 页大小: {}", pageNumber, offset, pageSize);
                long startTime = System.currentTimeMillis();
                
                pageRecords = sqlserverJdbcTemplate.query(
                    PAGE_QUERY_SQL, 
                    rowMapper,
                    offset,
                    pageSize
                );
                
                long endTime = System.currentTimeMillis();
                
                if (!pageRecords.isEmpty()) {
                    allRecords.addAll(pageRecords);
                    offset += pageSize;
                    logger.debug("第 {} 页查询完成，获取到 {} 条记录，用时: {} 毫秒", 
                            pageNumber, pageRecords.size(), (endTime - startTime));
                    pageNumber++;
                } else {
                    logger.debug("第 {} 页没有数据，分页查询结束", pageNumber);
                }
            } while (!pageRecords.isEmpty() && pageRecords.size() == pageSize);
            
            logger.info("SQL Server分页查询完成，共 {} 页，总记录数: {}", pageNumber - 1, allRecords.size());
            return allRecords;
        } catch (Exception e) {
            logger.error("SQL Server分页查询失败", e);
            throw e;
        }
    }
} 