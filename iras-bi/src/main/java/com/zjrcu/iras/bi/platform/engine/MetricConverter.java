package com.zjrcu.iras.bi.platform.engine;

import com.zjrcu.iras.bi.platform.domain.dto.MetricConfigDTO;
import com.zjrcu.iras.bi.platform.security.SQLValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 指标转换器
 * 
 * 负责将指标配置转换为SQL查询表达式。
 * 实现需求: 1.4, 2.4, 2.5, 3.3, 3.4, 4.3, 5.5, 5.6, 8.4
 * 
 * @author IRAS BI Platform
 */
@Component
public class MetricConverter {

    @Autowired
    private SQLValidator sqlValidator;
    
    @Autowired
    private SQLDialectAdapter dialectAdapter;
    
    @Autowired
    private ExpressionParser expressionParser;
    
    // 性能保护限制
    private static final int MAX_METRICS = 20;
    private static final int MAX_SQL_LENGTH = 10000;
    private static final int MAX_NESTING_DEPTH = 3;
    
    /**
     * 转换基础指标为SQL表达式
     * 需求 1.4
     * 
     * @param metric 基础指标配置
     * @param dbType 数据库类型
     * @return SQL表达式
     */
    public String convertBaseMetric(MetricConfigDTO.BaseMetric metric, SQLDialectAdapter.DatabaseType dbType) {
        if (metric == null) {
            throw new IllegalArgumentException("基础指标配置不能为空");
        }
        
        // 验证字段名和聚合函数
        sqlValidator.validateFieldName(metric.getField());
        sqlValidator.validateAggregation(metric.getAggregation());
        
        // 生成SQL: AGG(field) AS alias
        String aggregationSql = dialectAdapter.generateAggregation(
            dbType, 
            metric.getAggregation(), 
            metric.getField()
        );
        
        return String.format("%s AS %s", 
            aggregationSql, 
            dialectAdapter.quoteIdentifier(dbType, metric.getName())
        );
    }
    
    /**
     * 转换条件比率指标为SQL表达式
     * 需求 2.4, 2.5
     * 
     * @param metric 条件比率指标配置
     * @param dbType 数据库类型
     * @return SQL表达式
     */
    public String convertConditionalRatio(
            MetricConfigDTO.ConditionalRatioMetric metric, 
            SQLDialectAdapter.DatabaseType dbType) {
        
        if (metric == null) {
            throw new IllegalArgumentException("条件比率指标配置不能为空");
        }
        
        // 验证字段名和条件
        sqlValidator.validateFieldName(metric.getField());
        sqlValidator.validateCondition(metric.getNumeratorCondition());
        // 分母条件允许为空（表示所有记录）
        sqlValidator.validateCondition(metric.getDenominatorCondition(), true);
        
        // 生成条件比率SQL
        String ratioSql = dialectAdapter.generateConditionalRatio(
            dbType,
            metric.getField(),
            metric.getNumeratorCondition(),
            metric.getDenominatorCondition()
        );
        
        return String.format("%s AS %s", 
            ratioSql, 
            dialectAdapter.quoteIdentifier(dbType, metric.getName())
        );
    }
    
    /**
     * 转换简单比率指标为SQL表达式
     * 需求 3.3, 3.4
     * 
     * @param metric 简单比率指标配置
     * @param dbType 数据库类型
     * @param metricSqlMap 已转换的指标SQL映射
     * @return SQL表达式
     */
    public String convertSimpleRatio(
            MetricConfigDTO.SimpleRatioMetric metric,
            SQLDialectAdapter.DatabaseType dbType,
            Map<String, String> metricSqlMap) {
        
        if (metric == null) {
            throw new IllegalArgumentException("简单比率指标配置不能为空");
        }
        
        // 验证指标引用
        String numeratorMetric = metric.getNumeratorMetric();
        String denominatorMetric = metric.getDenominatorMetric();
        
        if (!metricSqlMap.containsKey(numeratorMetric)) {
            throw new IllegalArgumentException("分子指标不存在: " + numeratorMetric);
        }
        if (!metricSqlMap.containsKey(denominatorMetric)) {
            throw new IllegalArgumentException("分母指标不存在: " + denominatorMetric);
        }
        
        // 获取分子和分母的SQL表达式
        String numeratorSql = metricSqlMap.get(numeratorMetric);
        String denominatorSql = metricSqlMap.get(denominatorMetric);
        
        // 生成除法表达式(带NULLIF保护)
        String divisionSql = dialectAdapter.generateDivision(dbType, numeratorSql, denominatorSql);
        
        return String.format("%s AS %s", 
            divisionSql, 
            dialectAdapter.quoteIdentifier(dbType, metric.getName())
        );
    }
    
    /**
     * 转换条件求和指标为SQL表达式
     * 需求 4.3
     * 
     * @param metric 条件求和指标配置
     * @param dbType 数据库类型
     * @return SQL表达式
     */
    public String convertConditionalSum(
            MetricConfigDTO.ConditionalSumMetric metric,
            SQLDialectAdapter.DatabaseType dbType) {
        
        if (metric == null) {
            throw new IllegalArgumentException("条件求和指标配置不能为空");
        }
        
        // 验证字段名和条件
        sqlValidator.validateFieldName(metric.getField());
        sqlValidator.validateCondition(metric.getCondition());
        
        // 生成条件求和SQL
        String conditionalSumSql = dialectAdapter.generateConditionalSum(
            dbType,
            metric.getField(),
            metric.getCondition()
        );
        
        return String.format("%s AS %s", 
            conditionalSumSql, 
            dialectAdapter.quoteIdentifier(dbType, metric.getName())
        );
    }
    
    /**
     * 转换自定义表达式指标为SQL表达式
     * 需求 5.5, 5.6
     * 
     * @param metric 自定义表达式指标配置
     * @param dbType 数据库类型
     * @param metricSqlMap 已转换的指标SQL映射
     * @return SQL表达式
     */
    public String convertCustomExpression(
            MetricConfigDTO.CustomExpressionMetric metric,
            SQLDialectAdapter.DatabaseType dbType,
            Map<String, String> metricSqlMap) {
        
        if (metric == null) {
            throw new IllegalArgumentException("自定义表达式指标配置不能为空");
        }
        
        String expression = metric.getExpression();
        
        // 验证表达式
        sqlValidator.validateExpression(expression);
        expressionParser.validateExpression(expression);
        
        // 提取指标引用
        Set<String> referencedMetrics = expressionParser.extractMetricReferences(expression);
        
        // 验证所有引用的指标都存在
        for (String refMetric : referencedMetrics) {
            if (!metricSqlMap.containsKey(refMetric)) {
                throw new IllegalArgumentException("引用的指标不存在: " + refMetric);
            }
        }
        
        // 替换指标引用为SQL表达式
        String sqlExpression = expressionParser.replaceMetricReferences(expression, metricSqlMap);
        
        // 如果表达式包含除法,添加NULLIF保护
        if (expressionParser.containsDivision(expression)) {
            sqlExpression = addNullProtectionToDivision(sqlExpression, dbType);
        }
        
        return String.format("%s AS %s", 
            sqlExpression, 
            dialectAdapter.quoteIdentifier(dbType, metric.getName())
        );
    }
    
    /**
     * 为除法表达式添加NULLIF保护
     * 
     * @param expression SQL表达式
     * @param dbType 数据库类型
     * @return 添加保护后的表达式
     */
    private String addNullProtectionToDivision(String expression, SQLDialectAdapter.DatabaseType dbType) {
        // 简化实现: 查找除法运算符,为分母添加NULLIF
        // 注意: 这是一个基本实现,处理简单的除法情况
        // 复杂的嵌套除法可能需要更精确的解析
        
        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < expression.length()) {
            if (expression.charAt(i) == '/') {
                // 找到除法运算符,需要为分母添加NULLIF
                // 向后查找分母表达式
                int start = i + 1;
                while (start < expression.length() && Character.isWhitespace(expression.charAt(start))) {
                    start++;
                }
                
                if (start < expression.length()) {
                    // 提取分母表达式
                    String denominator = extractDenominator(expression, start);
                    if (denominator != null && !denominator.startsWith("NULLIF")) {
                        // 添加NULLIF保护
                        result.append(" / ");
                        result.append(dialectAdapter.generateNullIf(dbType, denominator, "0"));
                        i = start + denominator.length();
                        continue;
                    }
                }
            }
            result.append(expression.charAt(i));
            i++;
        }
        
        return result.toString();
    }
    
    /**
     * 提取分母表达式
     * 
     * @param expression 完整表达式
     * @param start 起始位置
     * @return 分母表达式
     */
    private String extractDenominator(String expression, int start) {
        if (start >= expression.length()) {
            return null;
        }
        
        // 如果分母以括号开始,提取整个括号内容
        if (expression.charAt(start) == '(') {
            int depth = 1;
            int end = start + 1;
            while (end < expression.length() && depth > 0) {
                if (expression.charAt(end) == '(') depth++;
                if (expression.charAt(end) == ')') depth--;
                end++;
            }
            return expression.substring(start, end);
        }
        
        // 否则提取到下一个运算符或空格
        int end = start;
        while (end < expression.length()) {
            char c = expression.charAt(end);
            if (c == '+' || c == '-' || c == '*' || c == '/' || 
                c == ')' || Character.isWhitespace(c)) {
                break;
            }
            end++;
        }
        
        return expression.substring(start, end);
    }
    
    /**
     * 转换完整的指标配置为SQL SELECT子句
     * 
     * @param config 指标配置
     * @param dbType 数据库类型
     * @return SQL SELECT子句(不包含SELECT关键字)
     */
    public String convertMetricConfig(MetricConfigDTO config, SQLDialectAdapter.DatabaseType dbType) {
        if (config == null) {
            throw new IllegalArgumentException("指标配置不能为空");
        }
        
        List<MetricConfigDTO.BaseMetric> baseMetrics = config.getBaseMetrics();
        List<MetricConfigDTO.ComputedMetric> computedMetrics = config.getComputedMetrics();
        
        // 检查指标数量限制
        int totalMetrics = (baseMetrics != null ? baseMetrics.size() : 0) + 
                          (computedMetrics != null ? computedMetrics.size() : 0);
        if (totalMetrics > MAX_METRICS) {
            throw new IllegalArgumentException(
                String.format("指标数量 %d 超过最大限制 %d", totalMetrics, MAX_METRICS)
            );
        }
        
        // 存储已转换的指标SQL(用于处理指标依赖)
        Map<String, String> metricSqlMap = new LinkedHashMap<>();
        List<String> selectClauses = new ArrayList<>();
        
        // 1. 转换基础指标
        if (baseMetrics != null) {
            for (MetricConfigDTO.BaseMetric metric : baseMetrics) {
                String sql = convertBaseMetric(metric, dbType);
                selectClauses.add(sql);
                
                // 提取不带AS的SQL表达式
                String sqlExpr = sql.substring(0, sql.lastIndexOf(" AS "));
                metricSqlMap.put(metric.getName(), sqlExpr);
            }
        }
        
        // 2. 转换计算指标(需要按依赖顺序)
        if (computedMetrics != null) {
            // 构建依赖关系图
            Map<String, Set<String>> dependencies = buildDependencyGraph(computedMetrics);
            
            // 检查嵌套深度
            validateNestingDepth(dependencies);
            
            // 拓扑排序
            List<MetricConfigDTO.ComputedMetric> sortedMetrics = topologicalSort(computedMetrics, dependencies);
            
            // 按顺序转换
            for (MetricConfigDTO.ComputedMetric metric : sortedMetrics) {
                String sql = convertComputedMetric(metric, dbType, metricSqlMap);
                selectClauses.add(sql);
                
                // 提取不带AS的SQL表达式
                String sqlExpr = sql.substring(0, sql.lastIndexOf(" AS "));
                metricSqlMap.put(metric.getName(), sqlExpr);
            }
        }
        
        // 3. 组合所有SELECT子句
        String result = String.join(",\n    ", selectClauses);
        
        // 4. 检查SQL长度限制
        if (result.length() > MAX_SQL_LENGTH) {
            throw new IllegalArgumentException(
                String.format("生成的SQL长度 %d 超过最大限制 %d", result.length(), MAX_SQL_LENGTH)
            );
        }
        
        return result;
    }
    
    /**
     * 转换单个计算指标
     * 
     * @param metric 计算指标
     * @param dbType 数据库类型
     * @param metricSqlMap 已转换的指标SQL映射
     * @return SQL表达式
     */
    private String convertComputedMetric(
            MetricConfigDTO.ComputedMetric metric,
            SQLDialectAdapter.DatabaseType dbType,
            Map<String, String> metricSqlMap) {
        
        if (metric instanceof MetricConfigDTO.ConditionalRatioMetric) {
            return convertConditionalRatio((MetricConfigDTO.ConditionalRatioMetric) metric, dbType);
        } else if (metric instanceof MetricConfigDTO.SimpleRatioMetric) {
            return convertSimpleRatio((MetricConfigDTO.SimpleRatioMetric) metric, dbType, metricSqlMap);
        } else if (metric instanceof MetricConfigDTO.ConditionalSumMetric) {
            return convertConditionalSum((MetricConfigDTO.ConditionalSumMetric) metric, dbType);
        } else if (metric instanceof MetricConfigDTO.CustomExpressionMetric) {
            return convertCustomExpression((MetricConfigDTO.CustomExpressionMetric) metric, dbType, metricSqlMap);
        } else {
            throw new IllegalArgumentException("不支持的计算指标类型: " + metric.getClass().getName());
        }
    }
    
    /**
     * 构建指标依赖关系图
     * 需求 8.4
     * 
     * @param computedMetrics 计算指标列表
     * @return 依赖关系映射(指标名 -> 依赖的指标名集合)
     */
    private Map<String, Set<String>> buildDependencyGraph(List<MetricConfigDTO.ComputedMetric> computedMetrics) {
        Map<String, Set<String>> dependencies = new HashMap<>();
        
        for (MetricConfigDTO.ComputedMetric metric : computedMetrics) {
            Set<String> deps = new HashSet<>();
            
            if (metric instanceof MetricConfigDTO.SimpleRatioMetric) {
                MetricConfigDTO.SimpleRatioMetric ratio = (MetricConfigDTO.SimpleRatioMetric) metric;
                deps.add(ratio.getNumeratorMetric());
                deps.add(ratio.getDenominatorMetric());
            } else if (metric instanceof MetricConfigDTO.CustomExpressionMetric) {
                MetricConfigDTO.CustomExpressionMetric custom = (MetricConfigDTO.CustomExpressionMetric) metric;
                deps.addAll(expressionParser.extractMetricReferences(custom.getExpression()));
            }
            
            dependencies.put(metric.getName(), deps);
        }
        
        return dependencies;
    }
    
    /**
     * 验证嵌套深度不超过限制
     * 需求 8.4
     * 
     * @param dependencies 依赖关系图
     * @throws IllegalArgumentException 如果嵌套深度超过限制
     */
    private void validateNestingDepth(Map<String, Set<String>> dependencies) {
        for (String metricName : dependencies.keySet()) {
            int depth = expressionParser.calculateDependencyDepth(metricName, dependencies, new HashSet<>());
            if (depth > MAX_NESTING_DEPTH) {
                throw new IllegalArgumentException(
                    String.format("指标 %s 的嵌套深度 %d 超过最大限制 %d", 
                        metricName, depth, MAX_NESTING_DEPTH)
                );
            }
        }
    }
    
    /**
     * 拓扑排序计算指标(按依赖顺序)
     * 
     * @param computedMetrics 计算指标列表
     * @param dependencies 依赖关系图
     * @return 排序后的指标列表
     * @throws IllegalArgumentException 如果存在循环依赖
     */
    private List<MetricConfigDTO.ComputedMetric> topologicalSort(
            List<MetricConfigDTO.ComputedMetric> computedMetrics,
            Map<String, Set<String>> dependencies) {
        
        // 构建指标名到指标对象的映射
        Map<String, MetricConfigDTO.ComputedMetric> metricMap = new HashMap<>();
        for (MetricConfigDTO.ComputedMetric metric : computedMetrics) {
            metricMap.put(metric.getName(), metric);
        }
        
        // 计算入度
        Map<String, Integer> inDegree = new HashMap<>();
        for (String metricName : dependencies.keySet()) {
            inDegree.put(metricName, 0);
        }
        for (Set<String> deps : dependencies.values()) {
            for (String dep : deps) {
                if (dependencies.containsKey(dep)) {
                    inDegree.put(dep, inDegree.get(dep) + 1);
                }
            }
        }
        
        // 使用队列进行拓扑排序
        Queue<String> queue = new LinkedList<>();
        for (Map.Entry<String, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.offer(entry.getKey());
            }
        }
        
        List<MetricConfigDTO.ComputedMetric> result = new ArrayList<>();
        while (!queue.isEmpty()) {
            String metricName = queue.poll();
            result.add(metricMap.get(metricName));
            
            Set<String> deps = dependencies.get(metricName);
            if (deps != null) {
                for (String dep : deps) {
                    if (inDegree.containsKey(dep)) {
                        int newDegree = inDegree.get(dep) - 1;
                        inDegree.put(dep, newDegree);
                        if (newDegree == 0) {
                            queue.offer(dep);
                        }
                    }
                }
            }
        }
        
        // 检查是否存在循环依赖
        if (result.size() != computedMetrics.size()) {
            throw new IllegalArgumentException("检测到循环依赖,无法解析指标顺序");
        }
        
        return result;
    }
}
