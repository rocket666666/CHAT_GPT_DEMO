package com.example.chatsales.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/database")
@Lazy
public class DatabaseController {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseController.class);
    
    // 创建一个线程池用于执行查询任务，避免阻塞主线程
    private final ExecutorService queryExecutor = Executors.newFixedThreadPool(5);
    
    // 用于JSON解析
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 执行SELECT查询语句
     * @param request 包含SQL查询的请求体
     * @return 查询结果
     */
    @PostMapping(value = "/query", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<?> executeQuery(@RequestBody String request) {
        logger.info("收到查询请求: {}", request);
        
        String sql = request; // 默认使用整个请求作为SQL
        
        // 尝试从请求中提取SQL语句
        try {
            // 预处理：移除可能的Markdown代码块标记
            String preprocessedRequest = request;
            // 移除Markdown代码块的前缀（如：```json、```sql等）
            preprocessedRequest = preprocessedRequest.replaceAll("^\\s*```.*?\\s*\\n", "");
            // 移除Markdown代码块的后缀
            preprocessedRequest = preprocessedRequest.replaceAll("\\n\\s*```\\s*$", "");
            
            logger.debug("预处理后的请求: {}", preprocessedRequest);
            
            // 策略1: 尝试将整个请求解析为JSON对象
            try {
                Map<String, Object> jsonRequest = objectMapper.readValue(preprocessedRequest, Map.class);
                if (jsonRequest.containsKey("sql")) {
                    sql = String.valueOf(jsonRequest.get("sql"));
                    logger.info("成功从JSON对象中提取SQL: {}", sql);
                }
            } catch (Exception e) {
                logger.debug("JSON解析失败，尝试其他策略: {}", e.getMessage());
                
                // 策略2: 使用改进的正则表达式，可处理前缀和各种格式
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(".*?\"sql\"\\s*:\\s*\"(.*?)\"(?=\\s*,|\\s*})", java.util.regex.Pattern.DOTALL);
                java.util.regex.Matcher matcher = pattern.matcher(preprocessedRequest);
                if (matcher.find()) {
                    sql = matcher.group(1);
                    // 处理可能的转义字符
                    sql = sql.replace("\\\"", "\"").replace("\\\\", "\\");
                    sql = processSQL(sql);
                    logger.info("成功使用正则提取SQL语句: {}", sql);
                } else {
                    // 策略3: 尝试使用更灵活的正则表达式处理不同类型的引号
                    java.util.regex.Pattern flexPattern = java.util.regex.Pattern.compile(".*?[\"']sql[\"']\\s*:\\s*[\"'](.*?)[\"'](?=\\s*,|\\s*})", java.util.regex.Pattern.DOTALL);
                    java.util.regex.Matcher flexMatcher = flexPattern.matcher(preprocessedRequest);
                    if (flexMatcher.find()) {
                        sql = flexMatcher.group(1);
                        // 处理可能的转义字符
                        sql = sql.replace("\\\"", "\"").replace("\\\\", "\\");
                        sql = processSQL(sql);
                        logger.info("使用灵活正则成功提取SQL语句: {}", sql);
                    } else {
                        logger.warn("未找到SQL模式，使用原始文本");
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("提取SQL失败，使用原始文本: {}", e.getMessage());
        }
        
        if (sql == null || sql.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "SQL语句不能为空"));
        }

        // 安全检查：仅允许SELECT语句
        if (!sql.trim().toLowerCase().startsWith("select")) {
            return ResponseEntity.badRequest().body(Map.of("error", "仅支持SELECT查询"));
        }
        
        final String finalSql = sql; // 用于lambda表达式
        
        try {
            logger.info("执行SQL查询: {}", finalSql);
            
            // 使用CompletableFuture异步执行查询，设置超时
            CompletableFuture<List<Map<String, Object>>> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return jdbcTemplate.queryForList(finalSql);
                } catch (Exception e) {
                    logger.error("执行查询失败: {}", e.getMessage());
                    throw new RuntimeException("查询执行失败: " + e.getMessage(), e);
                }
            }, queryExecutor);
            
            // 等待查询结果，设置额外的超时保险
            List<Map<String, Object>> results = future.get(35, TimeUnit.SECONDS); // 35秒超时，比JdbcTemplate的30秒多一点
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("results", results);
            response.put("count", results.size());
            
            return ResponseEntity.ok(response);
        } catch (TimeoutException e) {
            logger.error("查询执行超时", e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "查询执行超时，请检查SQL语句或尝试简化查询",
                "success", false
            ));
        } catch (Exception e) {
            logger.error("执行SQL查询失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "查询执行失败: " + e.getMessage(),
                "success", false
            ));
        }
    }

    /**
     * 执行更新操作（INSERT, UPDATE, DELETE）
     * @param request 包含SQL语句的请求体
     * @return 影响的行数
     */
    @PostMapping("/execute")
    public ResponseEntity<?> executeUpdate(@RequestBody Map<String, Object> request) {
        String sql = (String) request.get("sql");
        
        if (sql == null || sql.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "SQL语句不能为空"));
        }

        // 安全检查：不允许某些危险操作
        String sqlLower = sql.toLowerCase().trim();
        if (sqlLower.startsWith("drop") || sqlLower.startsWith("truncate") || 
            sqlLower.startsWith("alter") || sqlLower.contains("information_schema")) {
            return ResponseEntity.badRequest().body(Map.of("error", "不允许执行危险的SQL操作"));
        }

        final String finalSql = sql; // 用于lambda表达式
        
        try {
            logger.info("执行SQL更新: {}", finalSql);
            
            // 使用CompletableFuture异步执行更新，设置超时
            CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return jdbcTemplate.update(finalSql);
                } catch (Exception e) {
                    logger.error("执行更新失败: {}", e.getMessage());
                    throw new RuntimeException("更新执行失败: " + e.getMessage(), e);
                }
            }, queryExecutor);
            
            // 等待更新结果，设置额外的超时保险
            int rowsAffected = future.get(35, TimeUnit.SECONDS); // 35秒超时，比JdbcTemplate的30秒多一点
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("rowsAffected", rowsAffected);
            
            return ResponseEntity.ok(response);
        } catch (TimeoutException e) {
            logger.error("更新执行超时", e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "更新执行超时，请检查SQL语句或表数据量",
                "success", false
            ));
        } catch (Exception e) {
            logger.error("执行SQL更新失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "更新执行失败: " + e.getMessage(),
                "success", false
            ));
        }
    }

    /**
     * 执行带参数的预编译SQL语句（更安全的方法）
     * @param request 包含SQL和参数的请求体
     * @return 查询结果或更新行数
     */
    @PostMapping("/execute-with-params")
    public ResponseEntity<?> executeWithParams(@RequestBody Map<String, Object> request) {
        String sql = (String) request.get("sql");
        List<Object> params = (List<Object>) request.get("params");
        
        if (sql == null || sql.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "SQL语句不能为空"));
        }
        
        if (params == null) {
            params = List.of();
        }

        final String finalSql = sql; // 用于lambda表达式
        final List<Object> finalParams = params; // 用于lambda表达式
        
        try {
            logger.info("执行参数化SQL: {}, 参数: {}", finalSql, finalParams);
            
            String sqlLower = sql.toLowerCase().trim();
            
            if (sqlLower.startsWith("select")) {
                // 执行SELECT查询
                CompletableFuture<List<Map<String, Object>>> future = CompletableFuture.supplyAsync(() -> {
                    try {
                        return jdbcTemplate.queryForList(finalSql, finalParams.toArray());
                    } catch (Exception e) {
                        logger.error("执行参数化查询失败: {}", e.getMessage());
                        throw new RuntimeException("参数化查询执行失败: " + e.getMessage(), e);
                    }
                }, queryExecutor);
                
                // 等待查询结果，设置额外的超时保险
                List<Map<String, Object>> results = future.get(35, TimeUnit.SECONDS);
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("results", results);
                response.put("count", results.size());
                
                return ResponseEntity.ok(response);
            } else {
                // 执行更新操作
                CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
                    try {
                        return jdbcTemplate.update(finalSql, finalParams.toArray());
                    } catch (Exception e) {
                        logger.error("执行参数化更新失败: {}", e.getMessage());
                        throw new RuntimeException("参数化更新执行失败: " + e.getMessage(), e);
                    }
                }, queryExecutor);
                
                // 等待更新结果，设置额外的超时保险
                int rowsAffected = future.get(35, TimeUnit.SECONDS);
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("rowsAffected", rowsAffected);
                
                return ResponseEntity.ok(response);
            }
        } catch (TimeoutException e) {
            logger.error("参数化SQL执行超时", e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "SQL执行超时，请检查SQL语句或简化查询",
                "success", false
            ));
        } catch (Exception e) {
            logger.error("执行参数化SQL失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "SQL执行失败: " + e.getMessage(),
                "success", false
            ));
        }
    }

    /**
     * 处理SQL语句格式，移除不必要的换行符和规范化空格
     */
    private String processSQL(String sql) {
        if (sql == null) return "";
        
        // 处理转义字符
        sql = sql.replace("\\\"", "\"").replace("\\\\", "\\");
        
        // 先特别处理文本中的\n、\t等序列
        sql = sql.replace("\\n", " ").replace("\\t", " ").replace("\\r", " ");
        
        // 规范化SQL格式，替换多个空格为单个空格
        sql = sql.replaceAll("\\s+", " ");
        
        // 在处理关键字前，先保护引号中的内容（列别名等）
        Map<String, String> protectedTexts = new HashMap<>();
        java.util.regex.Pattern quotePattern = java.util.regex.Pattern.compile("'(.*?)'");
        java.util.regex.Matcher quoteMatcher = quotePattern.matcher(sql);
        
        StringBuffer sb = new StringBuffer();
        int index = 0;
        while (quoteMatcher.find()) {
            String placeholder = "##QUOTED_TEXT_" + index + "##";
            String quotedText = quoteMatcher.group(0);
            protectedTexts.put(placeholder, quotedText);
            quoteMatcher.appendReplacement(sb, placeholder);
            index++;
        }
        quoteMatcher.appendTail(sb);
        sql = sb.toString();
        
        // 确保列名中的关键字不被错误替换（处理类似i.color和ORDER BY的情况）
        // 先处理确定的完整SQL关键字
        String[] completeKeywords = {
            "SELECT", "FROM", "WHERE", "GROUP BY", "HAVING", "ORDER BY", "LIMIT",
            "JOIN", "LEFT JOIN", "RIGHT JOIN", "INNER JOIN", "UNION", "DISTINCT"
        };
        
        for (String keyword : completeKeywords) {
            // 只处理完整的关键字，确保它们前后有空格
            String pattern = "(?i)\\b" + keyword.replace(" ", "\\s+") + "\\b";
            sql = sql.replaceAll(pattern, " " + keyword + " ");
        }
        
        // 规范化空格（再次处理，避免多余空格）
        sql = sql.replaceAll("\\s+", " ");
        
        // 恢复被保护的文本
        for (Map.Entry<String, String> entry : protectedTexts.entrySet()) {
            sql = sql.replace(entry.getKey(), entry.getValue());
        }
        
        // 去除字符串两端空格
        return sql.trim();
    }
} 