package com.zjrcu.iras.bi.platform.security;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * SQL注入防护验证器
 * 
 * 负责验证所有用户提供的输入,防止SQL注入攻击。
 * 实现需求: 6.1, 6.2, 6.3, 6.4, 6.7
 * 
 * @author IRAS BI Platform
 */
@Component
public class SQLValidator {

    // 字段名验证正则: 必须以字母或下划线开头,后续可包含字母、数字、下划线
    private static final Pattern FIELD_NAME_PATTERN = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");
    
    // 聚合函数白名单
    private static final Set<String> ALLOWED_AGGREGATIONS = new HashSet<>(Arrays.asList(
        "SUM", "AVG", "COUNT", "MAX", "MIN"
    ));
    
    // SQL关键字黑名单 (需求 6.2)
    private static final Set<String> SQL_KEYWORDS_BLACKLIST = new HashSet<>(Arrays.asList(
        "SELECT", "INSERT", "UPDATE", "DELETE", "DROP", "CREATE", "ALTER", "TRUNCATE",
        "EXEC", "EXECUTE", "UNION", "DECLARE", "CAST", "CONVERT", "SCRIPT",
        "JAVASCRIPT", "IMPORT", "GRANT", "REVOKE", "COMMIT", "ROLLBACK"
    ));
    
    // SQL注释模式 (需求 6.3)
    private static final Pattern SQL_COMMENT_PATTERN = Pattern.compile("(--|/\\*|\\*/)");
    
    // SQL终止符模式 (需求 6.4)
    private static final Pattern SQL_TERMINATOR_PATTERN = Pattern.compile(";");
    
    // 允许的比较运算符
    private static final Set<String> ALLOWED_COMPARISON_OPERATORS = new HashSet<>(Arrays.asList(
        "=", "!=", "<>", ">", "<", ">=", "<=", "LIKE", "IN", "NOT IN", "IS NULL", "IS NOT NULL"
    ));
    
    // 允许的逻辑运算符
    private static final Set<String> ALLOWED_LOGICAL_OPERATORS = new HashSet<>(Arrays.asList(
        "AND", "OR", "NOT"
    ));
    
    // 允许的数学运算符
    private static final Pattern MATH_OPERATOR_PATTERN = Pattern.compile("[+\\-*/()]");
    
    // 最大输入长度限制
    private static final int MAX_FIELD_NAME_LENGTH = 64;
    private static final int MAX_CONDITION_LENGTH = 500;
    private static final int MAX_EXPRESSION_LENGTH = 500;

    /**
     * 验证字段名
     * 需求 6.1, 6.7
     * 
     * @param fieldName 字段名
     * @throws SecurityException 如果字段名无效
     */
    public void validateFieldName(String fieldName) {
        if (fieldName == null || fieldName.trim().isEmpty()) {
            throw new SecurityException("字段名不能为空");
        }
        
        if (fieldName.length() > MAX_FIELD_NAME_LENGTH) {
            throw new SecurityException("字段名长度不能超过 " + MAX_FIELD_NAME_LENGTH + " 个字符");
        }
        
        if (!FIELD_NAME_PATTERN.matcher(fieldName).matches()) {
            throw new SecurityException("字段名格式无效: " + fieldName + 
                ". 必须以字母或下划线开头,只能包含字母、数字、下划线");
        }
        
        // 检查是否包含SQL关键字
        String upperFieldName = fieldName.toUpperCase();
        if (SQL_KEYWORDS_BLACKLIST.contains(upperFieldName)) {
            throw new SecurityException("字段名不能使用SQL关键字: " + fieldName);
        }
    }

    /**
     * 验证聚合函数
     * 需求 6.1
     * 
     * @param aggregation 聚合函数名称
     * @throws SecurityException 如果聚合函数无效
     */
    public void validateAggregation(String aggregation) {
        if (aggregation == null || aggregation.trim().isEmpty()) {
            throw new SecurityException("聚合函数不能为空");
        }
        
        String upperAggregation = aggregation.toUpperCase().trim();
        if (!ALLOWED_AGGREGATIONS.contains(upperAggregation)) {
            throw new SecurityException("不支持的聚合函数: " + aggregation + 
                ". 允许的函数: " + ALLOWED_AGGREGATIONS);
        }
    }

    /**
     * 验证条件表达式
     * 需求 6.2, 6.3, 6.4, 6.7
     * 
     * @param condition 条件表达式
     * @throws SecurityException 如果条件表达式包含危险模式
     */
    public void validateCondition(String condition) {
        validateCondition(condition, false);
    }
    
    /**
     * 验证条件表达式
     * 需求 6.2, 6.3, 6.4, 6.7
     * 
     * @param condition 条件表达式
     * @param allowEmpty 是否允许为空（用于条件比率的分母条件）
     * @throws SecurityException 如果条件表达式包含危险模式
     */
    public void validateCondition(String condition, boolean allowEmpty) {
        if (condition == null || condition.trim().isEmpty()) {
            if (allowEmpty) {
                return; // 允许为空时直接返回
            }
            throw new SecurityException("条件表达式不能为空");
        }
        
        if (condition.length() > MAX_CONDITION_LENGTH) {
            throw new SecurityException("条件表达式长度不能超过 " + MAX_CONDITION_LENGTH + " 个字符");
        }
        
        // 检查SQL注释 (需求 6.3)
        if (SQL_COMMENT_PATTERN.matcher(condition).find()) {
            throw new SecurityException("条件表达式不能包含SQL注释符号 (--、/*、*/)");
        }
        
        // 检查SQL终止符 (需求 6.4)
        if (SQL_TERMINATOR_PATTERN.matcher(condition).find()) {
            throw new SecurityException("条件表达式不能包含分号");
        }
        
        // 检查SQL关键字黑名单 (需求 6.2)
        String upperCondition = condition.toUpperCase();
        for (String keyword : SQL_KEYWORDS_BLACKLIST) {
            // 使用单词边界检查,避免误判(例如 "description" 包含 "script")
            if (upperCondition.matches(".*\\b" + keyword + "\\b.*")) {
                throw new SecurityException("条件表达式不能包含SQL关键字: " + keyword);
            }
        }
        
        // 验证条件表达式的基本结构
        validateConditionStructure(condition);
    }

    /**
     * 验证条件表达式的结构
     * 
     * @param condition 条件表达式
     * @throws SecurityException 如果结构无效
     */
    private void validateConditionStructure(String condition) {
        // 移除字符串字面量以简化验证
        String sanitized = condition.replaceAll("'[^']*'", "''");
        
        // 检查括号匹配
        int parenthesesCount = 0;
        for (char c : sanitized.toCharArray()) {
            if (c == '(') parenthesesCount++;
            if (c == ')') parenthesesCount--;
            if (parenthesesCount < 0) {
                throw new SecurityException("条件表达式括号不匹配");
            }
        }
        if (parenthesesCount != 0) {
            throw new SecurityException("条件表达式括号不匹配");
        }
        
        // 检查是否包含字段名(至少一个有效标识符)
        if (!sanitized.matches(".*[a-zA-Z_][a-zA-Z0-9_]*.*")) {
            throw new SecurityException("条件表达式必须包含至少一个字段名");
        }
    }

    /**
     * 验证数学表达式
     * 需求 6.7
     * 
     * @param expression 数学表达式
     * @throws SecurityException 如果表达式包含危险模式
     */
    public void validateExpression(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            throw new SecurityException("数学表达式不能为空");
        }
        
        if (expression.length() > MAX_EXPRESSION_LENGTH) {
            throw new SecurityException("数学表达式长度不能超过 " + MAX_EXPRESSION_LENGTH + " 个字符");
        }
        
        // 检查SQL注释
        if (SQL_COMMENT_PATTERN.matcher(expression).find()) {
            throw new SecurityException("数学表达式不能包含SQL注释符号");
        }
        
        // 检查SQL终止符
        if (SQL_TERMINATOR_PATTERN.matcher(expression).find()) {
            throw new SecurityException("数学表达式不能包含分号");
        }
        
        // 检查SQL关键字黑名单
        String upperExpression = expression.toUpperCase();
        for (String keyword : SQL_KEYWORDS_BLACKLIST) {
            if (upperExpression.matches(".*\\b" + keyword + "\\b.*")) {
                throw new SecurityException("数学表达式不能包含SQL关键字: " + keyword);
            }
        }
        
        // 验证表达式只包含允许的字符
        validateExpressionCharacters(expression);
    }

    /**
     * 验证表达式字符
     * 
     * @param expression 表达式
     * @throws SecurityException 如果包含非法字符
     */
    private void validateExpressionCharacters(String expression) {
        // 移除字符串字面量后再验证(避免字符串内容影响验证)
        String sanitized = expression.replaceAll("'[^']*'", "''");
        
        // 允许的字符: 字母、数字、下划线(字段名)、数学运算符、空格、点(小数)、方括号(指标引用)、逗号(IN子句)
        // 注意: 字符串字面量已被移除,所以不需要在这里验证单引号内的内容
        Pattern allowedPattern = Pattern.compile("^[a-zA-Z0-9_+\\-*/()\\s.,\\[\\]']+$");
        if (!allowedPattern.matcher(sanitized).matches()) {
            throw new SecurityException("数学表达式包含非法字符");
        }
        
        // 检查括号匹配
        int parenthesesCount = 0;
        int bracketCount = 0;
        for (char c : sanitized.toCharArray()) {
            if (c == '(') parenthesesCount++;
            if (c == ')') parenthesesCount--;
            if (c == '[') bracketCount++;
            if (c == ']') bracketCount--;
            if (parenthesesCount < 0 || bracketCount < 0) {
                throw new SecurityException("表达式括号不匹配");
            }
        }
        if (parenthesesCount != 0 || bracketCount != 0) {
            throw new SecurityException("表达式括号不匹配");
        }
    }

    /**
     * 验证指标引用列表
     * 需求 6.7
     * 
     * @param metricNames 指标名称列表
     * @param availableMetrics 可用的指标名称集合
     * @throws SecurityException 如果引用了不存在的指标
     */
    public void validateMetricReferences(Set<String> metricNames, Set<String> availableMetrics) {
        if (metricNames == null || metricNames.isEmpty()) {
            return;
        }
        
        for (String metricName : metricNames) {
            if (!availableMetrics.contains(metricName)) {
                throw new SecurityException("引用了不存在的指标: " + metricName + 
                    ". 可用指标: " + availableMetrics);
            }
        }
    }

    /**
     * 清理字符串字面量(用于SQL生成)
     * 
     * @param value 字符串值
     * @return 清理后的值
     */
    public String sanitizeStringLiteral(String value) {
        if (value == null) {
            return null;
        }
        // 转义单引号
        return value.replace("'", "''");
    }
}
