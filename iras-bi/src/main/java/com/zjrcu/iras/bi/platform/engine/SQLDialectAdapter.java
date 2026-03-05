package com.zjrcu.iras.bi.platform.engine;

import com.zjrcu.iras.bi.platform.domain.DataSource;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * SQL方言适配器
 * 
 * 负责根据数据库类型生成对应的SQL语法,支持多种数据库方言。
 * 实现需求: 7.1, 7.2, 7.3, 7.4, 7.5
 * 
 * @author IRAS BI Platform
 */
@Component
public class SQLDialectAdapter {

    /**
     * 支持的数据库类型
     */
    public enum DatabaseType {
        MYSQL("mysql"),
        POSTGRESQL("postgresql"),
        CLICKHOUSE("clickhouse"),
        DORIS("doris"),
        UNKNOWN("unknown");

        private final String code;

        DatabaseType(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        public static DatabaseType fromCode(String code) {
            if (code == null) {
                return UNKNOWN;
            }
            String lowerCode = code.toLowerCase().trim();
            for (DatabaseType type : values()) {
                if (type.code.equals(lowerCode)) {
                    return type;
                }
            }
            return UNKNOWN;
        }
    }

    /**
     * 从数据源检测数据库类型
     * 需求 7.1
     * 
     * @param dataSource 数据源对象
     * @return 数据库类型
     */
    public DatabaseType detectDatabaseType(DataSource dataSource) {
        if (dataSource == null || dataSource.getType() == null) {
            return DatabaseType.UNKNOWN;
        }
        return DatabaseType.fromCode(dataSource.getType());
    }

    /**
     * 生成CASE WHEN表达式
     * 
     * @param dbType 数据库类型
     * @param condition 条件
     * @param thenValue THEN值
     * @param elseValue ELSE值
     * @return CASE表达式
     */
    public String generateCaseWhen(DatabaseType dbType, String condition, String thenValue, String elseValue) {
        // 所有支持的数据库都使用标准CASE语法
        return String.format("CASE WHEN %s THEN %s ELSE %s END", condition, thenValue, elseValue);
    }

    /**
     * 生成NULLIF表达式
     * 需求 7.2, 7.3, 7.4, 7.5
     * 
     * @param dbType 数据库类型
     * @param expression 表达式
     * @param nullValue 空值
     * @return NULLIF表达式
     */
    public String generateNullIf(DatabaseType dbType, String expression, String nullValue) {
        switch (dbType) {
            case MYSQL:
            case POSTGRESQL:
            case CLICKHOUSE:
            case DORIS:
                // 所有支持的数据库都支持标准NULLIF函数
                return String.format("NULLIF(%s, %s)", expression, nullValue);
            default:
                throw new UnsupportedOperationException(
                    "数据库类型 " + dbType + " 不支持NULLIF函数"
                );
        }
    }

    /**
     * 生成聚合函数表达式
     * 需求 7.2, 7.3, 7.4, 7.5
     * 
     * @param dbType 数据库类型
     * @param aggregation 聚合函数名称(SUM, AVG, COUNT, MAX, MIN)
     * @param field 字段名
     * @return 聚合表达式
     */
    public String generateAggregation(DatabaseType dbType, String aggregation, String field) {
        String upperAgg = aggregation.toUpperCase();
        
        switch (dbType) {
            case MYSQL:
                return generateMySQLAggregation(upperAgg, field);
            case POSTGRESQL:
                return generatePostgreSQLAggregation(upperAgg, field);
            case CLICKHOUSE:
                return generateClickHouseAggregation(upperAgg, field);
            case DORIS:
                return generateDorisAggregation(upperAgg, field);
            default:
                throw new UnsupportedOperationException(
                    "数据库类型 " + dbType + " 不支持聚合函数"
                );
        }
    }

    /**
     * 生成MySQL聚合函数
     * 需求 7.2
     */
    private String generateMySQLAggregation(String aggregation, String field) {
        // MySQL支持所有标准聚合函数
        return String.format("%s(%s)", aggregation, quoteIdentifier(DatabaseType.MYSQL, field));
    }

    /**
     * 生成PostgreSQL聚合函数
     * 需求 7.3
     */
    private String generatePostgreSQLAggregation(String aggregation, String field) {
        // PostgreSQL支持所有标准聚合函数
        return String.format("%s(%s)", aggregation, quoteIdentifier(DatabaseType.POSTGRESQL, field));
    }

    /**
     * 生成ClickHouse聚合函数
     * 需求 7.4
     */
    private String generateClickHouseAggregation(String aggregation, String field) {
        // ClickHouse支持所有标准聚合函数,但有一些特殊的变体
        // 这里使用标准函数,如需优化可以使用ClickHouse特定函数
        return String.format("%s(%s)", aggregation, quoteIdentifier(DatabaseType.CLICKHOUSE, field));
    }

    /**
     * 生成Doris聚合函数
     * 需求 7.5
     */
    private String generateDorisAggregation(String aggregation, String field) {
        // Doris(基于Apache Doris)支持所有标准聚合函数
        return String.format("%s(%s)", aggregation, quoteIdentifier(DatabaseType.DORIS, field));
    }

    /**
     * 引用标识符(字段名、表名)
     * 
     * @param dbType 数据库类型
     * @param identifier 标识符
     * @return 引用后的标识符
     */
    public String quoteIdentifier(DatabaseType dbType, String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            return identifier;
        }
        
        switch (dbType) {
            case MYSQL:
            case DORIS:
                // MySQL和Doris使用反引号
                return "`" + identifier + "`";
            case POSTGRESQL:
                // PostgreSQL使用双引号
                return "\"" + identifier + "\"";
            case CLICKHOUSE:
                // ClickHouse使用反引号或不引用
                return "`" + identifier + "`";
            default:
                // 默认不引用
                return identifier;
        }
    }

    /**
     * 生成字符串字面量
     * 
     * @param dbType 数据库类型
     * @param value 字符串值
     * @return SQL字符串字面量
     */
    public String generateStringLiteral(DatabaseType dbType, String value) {
        if (value == null) {
            return "NULL";
        }
        
        // 所有数据库都使用单引号,并转义内部单引号
        String escaped = value.replace("'", "''");
        return "'" + escaped + "'";
    }

    /**
     * 生成除法表达式(带NULLIF保护)
     * 
     * @param dbType 数据库类型
     * @param numerator 分子表达式
     * @param denominator 分母表达式
     * @return 除法表达式
     */
    public String generateDivision(DatabaseType dbType, String numerator, String denominator) {
        String nullIfDenominator = generateNullIf(dbType, denominator, "0");
        return String.format("(%s) / %s", numerator, nullIfDenominator);
    }

    /**
     * 生成条件比率SQL
     * 
     * @param dbType 数据库类型
     * @param field 字段名
     * @param numeratorCondition 分子条件
     * @param denominatorCondition 分母条件（可为空，表示所有记录）
     * @return 条件比率SQL表达式
     */
    public String generateConditionalRatio(
            DatabaseType dbType,
            String field,
            String numeratorCondition,
            String denominatorCondition) {
        
        String quotedField = quoteIdentifier(dbType, field);
        
        // 规范化条件中的引号（将智能引号转换为标准单引号）
        String normalizedNumeratorCondition = normalizeQuotes(numeratorCondition);
        String normalizedDenominatorCondition = denominatorCondition != null ? normalizeQuotes(denominatorCondition) : null;
        
        // 分子: SUM(CASE WHEN condition THEN field ELSE 0 END)
        String numerator = String.format(
            "SUM(%s)",
            generateCaseWhen(dbType, normalizedNumeratorCondition, quotedField, "0")
        );
        
        // 分母: 如果条件为空，则使用 SUM(field)；否则使用 SUM(CASE WHEN condition THEN field ELSE 0 END)
        String denominator;
        if (normalizedDenominatorCondition == null || normalizedDenominatorCondition.trim().isEmpty()) {
            // 分母条件为空，表示所有记录
            denominator = String.format("SUM(%s)", quotedField);
        } else {
            // 分母条件不为空，使用CASE WHEN
            denominator = String.format(
                "SUM(%s)",
                generateCaseWhen(dbType, normalizedDenominatorCondition, quotedField, "0")
            );
        }
        
        // 除法(带NULLIF保护)
        return generateDivision(dbType, numerator, denominator);
    }

    /**
     * 生成条件求和SQL
     * 
     * @param dbType 数据库类型
     * @param field 字段名
     * @param condition 条件
     * @return 条件求和SQL表达式
     */
    public String generateConditionalSum(DatabaseType dbType, String field, String condition) {
        String quotedField = quoteIdentifier(dbType, field);
        // 规范化条件中的引号
        String normalizedCondition = normalizeQuotes(condition);
        String caseExpr = generateCaseWhen(dbType, normalizedCondition, quotedField, "0");
        return String.format("SUM(%s)", caseExpr);
    }

    /**
     * 规范化字符串中的引号
     * 将智能引号（curly quotes）转换为标准单引号（straight quotes）
     * 
     * @param text 输入文本
     * @return 规范化后的文本
     */
    private String normalizeQuotes(String text) {
        if (text == null) {
            return null;
        }
        
        // 方法1: 替换已知的智能引号变体
        String result = text
            .replace("\u2018", "'")  // 左单引号 '
            .replace("\u2019", "'")  // 右单引号 '
            .replace("\u201C", "\"") // 左双引号 "
            .replace("\u201D", "\"") // 右双引号 "
            .replace("\u300C", "'")  // 中文左引号 「
            .replace("\u300D", "'")  // 中文右引号 」
            .replace("\u300E", "'")  // 中文左双引号 『
            .replace("\u300F", "'")  // 中文右双引号 』
            .replace("\uFF07", "'")  // 全角撇号 ＇
            .replace("\u02BC", "'")  // 修饰符字母撇号 ʼ
            .replace("\u02BB", "'"); // 修饰符字母反撇号 ʻ
        
        // 方法2: 使用正则表达式替换所有Unicode引号类字符
        // 匹配Unicode类别中的引号字符(除了标准ASCII单引号和双引号)
        result = result.replaceAll("[\\u2018\\u2019\\u201A\\u201B\\u201C\\u201D\\u201E\\u201F\\u2039\\u203A\\u300C\\u300D\\u300E\\u300F\\uFF07\\u02BC\\u02BB\\u02C8\\u02CA\\u0301\\u0313\\u0314\\u0315\\u055A\\u05F3\\u05F4\\u2032\\u2033\\u2034\\u2035\\u2036\\u2037\\u2057]", "'");
        
        return result;
    }

    /**
     * 检查数据库是否支持特定功能
     * 需求 7.6
     * 
     * @param dbType 数据库类型
     * @param feature 功能名称
     * @return 是否支持
     */
    public boolean supportsFeature(DatabaseType dbType, String feature) {
        if (dbType == DatabaseType.UNKNOWN) {
            return false;
        }
        
        switch (feature.toLowerCase()) {
            case "nullif":
                // 所有支持的数据库都支持NULLIF
                return true;
            case "case_when":
                // 所有支持的数据库都支持CASE WHEN
                return true;
            case "aggregation":
                // 所有支持的数据库都支持标准聚合函数
                return true;
            case "subquery":
                // 所有支持的数据库都支持子查询
                return true;
            case "cte":
                // Common Table Expression (WITH子句)
                return dbType == DatabaseType.POSTGRESQL || 
                       dbType == DatabaseType.CLICKHOUSE || 
                       dbType == DatabaseType.DORIS ||
                       (dbType == DatabaseType.MYSQL); // MySQL 8.0+支持
            case "window_function":
                // 窗口函数
                return dbType == DatabaseType.POSTGRESQL || 
                       dbType == DatabaseType.CLICKHOUSE || 
                       dbType == DatabaseType.DORIS ||
                       (dbType == DatabaseType.MYSQL); // MySQL 8.0+支持
            default:
                return false;
        }
    }

    /**
     * 获取数据库特定的限制信息
     * 
     * @param dbType 数据库类型
     * @return 限制信息
     */
    public Map<String, Object> getDatabaseLimits(DatabaseType dbType) {
        Map<String, Object> limits = new HashMap<>();
        
        switch (dbType) {
            case MYSQL:
                limits.put("maxIdentifierLength", 64);
                limits.put("maxQueryLength", 1048576); // 1MB
                limits.put("supportsFullOuterJoin", false);
                break;
            case POSTGRESQL:
                limits.put("maxIdentifierLength", 63);
                limits.put("maxQueryLength", Integer.MAX_VALUE);
                limits.put("supportsFullOuterJoin", true);
                break;
            case CLICKHOUSE:
                limits.put("maxIdentifierLength", 127);
                limits.put("maxQueryLength", 262144); // 256KB默认
                limits.put("supportsFullOuterJoin", true);
                break;
            case DORIS:
                limits.put("maxIdentifierLength", 64);
                limits.put("maxQueryLength", 1048576); // 1MB
                limits.put("supportsFullOuterJoin", true);
                break;
            default:
                limits.put("maxIdentifierLength", 64);
                limits.put("maxQueryLength", 10000);
                limits.put("supportsFullOuterJoin", false);
        }
        
        return limits;
    }

    /**
     * 验证SQL表达式是否符合数据库方言
     * 
     * @param dbType 数据库类型
     * @param sqlExpression SQL表达式
     * @throws IllegalArgumentException 如果表达式不符合方言要求
     */
    public void validateSQLExpression(DatabaseType dbType, String sqlExpression) {
        if (sqlExpression == null || sqlExpression.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL表达式不能为空");
        }
        
        Map<String, Object> limits = getDatabaseLimits(dbType);
        int maxLength = (int) limits.get("maxQueryLength");
        
        if (sqlExpression.length() > maxLength) {
            throw new IllegalArgumentException(
                String.format("SQL表达式长度 %d 超过数据库 %s 的最大限制 %d",
                    sqlExpression.length(), dbType, maxLength)
            );
        }
    }

    /**
     * 生成数据库特定的类型转换
     * 
     * @param dbType 数据库类型
     * @param expression 表达式
     * @param targetType 目标类型
     * @return 类型转换表达式
     */
    public String generateCast(DatabaseType dbType, String expression, String targetType) {
        switch (dbType) {
            case MYSQL:
            case POSTGRESQL:
            case DORIS:
                // 标准CAST语法
                return String.format("CAST(%s AS %s)", expression, targetType);
            case CLICKHOUSE:
                // ClickHouse也支持标准CAST,但有特殊的to*函数
                // 这里使用标准语法以保持一致性
                return String.format("CAST(%s AS %s)", expression, targetType);
            default:
                throw new UnsupportedOperationException(
                    "数据库类型 " + dbType + " 不支持类型转换"
                );
        }
    }

    /**
     * 生成LIMIT子句
     * 
     * @param dbType 数据库类型
     * @param limit 限制数量
     * @return LIMIT子句
     */
    public String generateLimit(DatabaseType dbType, int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("LIMIT值必须大于0");
        }
        
        switch (dbType) {
            case MYSQL:
            case POSTGRESQL:
            case CLICKHOUSE:
            case DORIS:
                return String.format("LIMIT %d", limit);
            default:
                throw new UnsupportedOperationException(
                    "数据库类型 " + dbType + " 不支持LIMIT子句"
                );
        }
    }

    /**
     * 获取数据库方言的友好名称
     * 
     * @param dbType 数据库类型
     * @return 友好名称
     */
    public String getDatabaseDisplayName(DatabaseType dbType) {
        switch (dbType) {
            case MYSQL:
                return "MySQL";
            case POSTGRESQL:
                return "PostgreSQL";
            case CLICKHOUSE:
                return "ClickHouse";
            case DORIS:
                return "Apache Doris";
            default:
                return "Unknown";
        }
    }
}
