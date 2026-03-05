package com.zjrcu.iras.bi.platform.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 系统内置函数转换器
 * 负责将系统内置函数转换为目标数据库的SQL方言
 * 
 * @author IRAS
 */
@Component
public class BuiltinFunctionConverter {
    
    private static final Logger log = LoggerFactory.getLogger(BuiltinFunctionConverter.class);
    
    // 系统内置函数列表
    private static final Set<String> BUILTIN_FUNCTIONS = Set.of(
        // 逻辑函数
        "CASE", "IF", "COALESCE", "NULLIF",
        // 数值函数
        "ROUND", "FLOOR", "CEIL", "ABS", "MOD", "POWER",
        // 文本函数
        "CONCAT", "SUBSTRING", "LENGTH", "UPPER", "LOWER", "TRIM",
        // 日期函数
        "YEAR", "MONTH", "DAY", "DATEDIFF"
    );
    
    /**
     * 判断是否为系统内置函数
     * 
     * @param functionName 函数名称
     * @return true表示是系统内置函数
     */
    public boolean isBuiltinFunction(String functionName) {
        if (functionName == null || functionName.trim().isEmpty()) {
            return false;
        }
        return BUILTIN_FUNCTIONS.contains(functionName.toUpperCase());
    }
    
    /**
     * 转换系统内置函数为目标数据库SQL
     * 
     * @param expression 包含函数的表达式
     * @param functionName 函数名称
     * @param dbType 数据库类型(MySQL, PostgreSQL, ClickHouse等)
     * @return 转换后的表达式
     */
    public String convertFunction(String expression, String functionName, String dbType) {
        if (expression == null || functionName == null || dbType == null) {
            return expression;
        }
        
        String upperFunc = functionName.toUpperCase();
        
        switch (upperFunc) {
            case "IF":
                return convertIfFunction(expression, dbType);
            case "CONCAT":
                return convertConcatFunction(expression, dbType);
            case "SUBSTRING":
                return convertSubstringFunction(expression, dbType);
            case "DATEDIFF":
                return convertDateDiffFunction(expression, dbType);
            // 其他函数大多数数据库语法一致,不需要转换
            default:
                return expression;
        }
    }
    
    /**
     * 转换IF函数
     * MySQL: IF(condition, true_val, false_val)
     * PostgreSQL: CASE WHEN condition THEN true_val ELSE false_val END
     * ClickHouse: if(condition, true_val, false_val) - 小写
     */
    private String convertIfFunction(String expression, String dbType) {
        if ("PostgreSQL".equalsIgnoreCase(dbType)) {
            // 将IF(cond, val1, val2)转换为CASE WHEN cond THEN val1 ELSE val2 END
            Pattern pattern = Pattern.compile("IF\\s*\\(([^,]+),([^,]+),([^)]+)\\)", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(expression);
            
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                String condition = matcher.group(1).trim();
                String trueVal = matcher.group(2).trim();
                String falseVal = matcher.group(3).trim();
                String replacement = String.format("CASE WHEN %s THEN %s ELSE %s END", condition, trueVal, falseVal);
                matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            }
            matcher.appendTail(sb);
            return sb.toString();
        } else if ("ClickHouse".equalsIgnoreCase(dbType)) {
            // ClickHouse使用小写的if函数
            return expression.replaceAll("(?i)IF\\s*\\(", "if(");
        }
        return expression;
    }
    
    /**
     * 转换CONCAT函数
     * MySQL: CONCAT(str1, str2, ...)
     * PostgreSQL: str1 || str2 || ...
     * ClickHouse: concat(str1, str2, ...) - 小写
     */
    private String convertConcatFunction(String expression, String dbType) {
        if ("PostgreSQL".equalsIgnoreCase(dbType)) {
            // 将CONCAT(a, b, c)转换为a || b || c
            Pattern pattern = Pattern.compile("CONCAT\\s*\\(([^)]+)\\)", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(expression);
            
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                String args = matcher.group(1);
                String replacement = args.replaceAll("\\s*,\\s*", " || ");
                matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            }
            matcher.appendTail(sb);
            return sb.toString();
        } else if ("ClickHouse".equalsIgnoreCase(dbType)) {
            // ClickHouse使用小写的concat函数
            return expression.replaceAll("(?i)CONCAT\\s*\\(", "concat(");
        }
        return expression;
    }
    
    /**
     * 转换SUBSTRING函数
     * MySQL: SUBSTRING(str, pos, len)
     * PostgreSQL: SUBSTRING(str FROM pos FOR len)
     * ClickHouse: substring(str, pos, len) - 小写
     */
    private String convertSubstringFunction(String expression, String dbType) {
        if ("PostgreSQL".equalsIgnoreCase(dbType)) {
            Pattern pattern = Pattern.compile("SUBSTRING\\s*\\(([^,]+),([^,]+),([^)]+)\\)", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(expression);
            
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                String str = matcher.group(1).trim();
                String pos = matcher.group(2).trim();
                String len = matcher.group(3).trim();
                String replacement = String.format("SUBSTRING(%s FROM %s FOR %s)", str, pos, len);
                matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            }
            matcher.appendTail(sb);
            return sb.toString();
        } else if ("ClickHouse".equalsIgnoreCase(dbType)) {
            // ClickHouse使用小写的substring函数
            return expression.replaceAll("(?i)SUBSTRING\\s*\\(", "substring(");
        }
        return expression;
    }
    
    /**
     * 转换DATEDIFF函数
     * MySQL: DATEDIFF(date1, date2) - 返回天数差
     * PostgreSQL: EXTRACT(DAY FROM (date1 - date2))
     * ClickHouse: dateDiff('day', date2, date1) - 注意参数顺序相反
     */
    private String convertDateDiffFunction(String expression, String dbType) {
        if ("PostgreSQL".equalsIgnoreCase(dbType)) {
            Pattern pattern = Pattern.compile("DATEDIFF\\s*\\(([^,]+),([^)]+)\\)", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(expression);
            
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                String date1 = matcher.group(1).trim();
                String date2 = matcher.group(2).trim();
                String replacement = String.format("EXTRACT(DAY FROM (%s - %s))", date1, date2);
                matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            }
            matcher.appendTail(sb);
            return sb.toString();
        } else if ("ClickHouse".equalsIgnoreCase(dbType)) {
            Pattern pattern = Pattern.compile("DATEDIFF\\s*\\(([^,]+),([^)]+)\\)", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(expression);
            
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                String date1 = matcher.group(1).trim();
                String date2 = matcher.group(2).trim();
                // ClickHouse的dateDiff参数顺序是(unit, start, end),与MySQL相反
                String replacement = String.format("dateDiff('day', %s, %s)", date2, date1);
                matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            }
            matcher.appendTail(sb);
            return sb.toString();
        }
        return expression;
    }
}
