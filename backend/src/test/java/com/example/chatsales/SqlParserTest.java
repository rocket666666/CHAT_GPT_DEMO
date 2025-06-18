package com.example.chatsales;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 用于测试SQL解析功能的独立类
 * 您可以在IDE中直接运行这个类进行测试
 */
public class SqlParserTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        // 测试用例1: 标准JSON
        String request1 = "{\"sql\": \"SELECT * FROM table\"}";
        
        // 测试用例2: 带有Markdown代码块的JSON
        String request2 = "```json\n{\"sql\": \"SELECT sku_code as \\\"存货编码\\\", sku_name as \\\"存货名称\\\" FROM table\"}\n```";
        
        // 测试用例3: 带有嵌套引号的复杂SQL
        String request3 = "{\"sql\": \"SELECT sku_code as \\\"存货编码\\\", sku_name as \\\"存货名称\\\" FROM table WHERE name LIKE '%test%'\"}";
        
        // 测试用例4: 恶意输入测试
        String request4 = "```json\n{\n  \"sql\": \"DROP TABLE users;\"\n}\n```";

        System.out.println("========== 测试用例1: 标准JSON ==========");
        testSqlExtraction(request1);
        
        System.out.println("\n========== 测试用例2: 带有Markdown代码块的JSON ==========");
        testSqlExtraction(request2);
        
        System.out.println("\n========== 测试用例3: 带有嵌套引号的复杂SQL ==========");
        testSqlExtraction(request3);
        
        System.out.println("\n========== 测试用例4: 恶意输入测试 ==========");
        testSqlExtraction(request4);
    }
    
    private static void testSqlExtraction(String request) {
        System.out.println("原始请求: " + request);
        
        String sql = null;
        
        // 预处理：移除可能的Markdown代码块标记
        String preprocessedRequest = request;
        // 移除Markdown代码块的前缀（如：```json、```sql等）
        preprocessedRequest = preprocessedRequest.replaceAll("^\\s*```.*?\\s*\\n", "");
        // 移除Markdown代码块的后缀
        preprocessedRequest = preprocessedRequest.replaceAll("\\n\\s*```\\s*$", "");
        
        System.out.println("预处理后的请求: " + preprocessedRequest);
        
        // 策略1: 尝试将整个请求解析为JSON对象
        try {
            Map<String, Object> jsonRequest = objectMapper.readValue(preprocessedRequest, Map.class);
            if (jsonRequest.containsKey("sql")) {
                sql = String.valueOf(jsonRequest.get("sql"));
                System.out.println("成功从JSON对象中提取SQL: " + sql);
            }
        } catch (Exception e) {
            System.out.println("JSON解析失败，尝试其他策略: " + e.getMessage());
            
            // 策略2: 使用改进的正则表达式，可处理前缀和各种格式
            Pattern pattern = Pattern.compile(".*?\"sql\"\\s*:\\s*\"(.*?)\"(?=\\s*,|\\s*})", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(preprocessedRequest);
            if (matcher.find()) {
                sql = matcher.group(1);
                // 处理可能的转义字符
                sql = sql.replace("\\\"", "\"").replace("\\\\", "\\");
                System.out.println("成功使用正则提取SQL语句: " + sql);
            } else {
                // 策略3: 尝试使用更灵活的正则表达式处理不同类型的引号
                Pattern flexPattern = Pattern.compile(".*?[\"']sql[\"']\\s*:\\s*[\"'](.*?)[\"'](?=\\s*,|\\s*})", Pattern.DOTALL);
                Matcher flexMatcher = flexPattern.matcher(preprocessedRequest);
                if (flexMatcher.find()) {
                    sql = flexMatcher.group(1);
                    // 处理可能的转义字符
                    sql = sql.replace("\\\"", "\"").replace("\\\\", "\\");
                    System.out.println("使用灵活正则成功提取SQL语句: " + sql);
                } else {
                    System.out.println("未找到SQL模式，使用原始文本");
                }
            }
        }
        
        // 安全检查：仅允许SELECT语句
        if (sql != null && !sql.trim().toLowerCase().startsWith("select")) {
            System.out.println("警告：不安全的SQL语句类型!");
        }
    }
} 