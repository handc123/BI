package com.zjrcu.iras.bi.platform.engine;

import com.zjrcu.iras.bi.platform.domain.Dataset;
import com.zjrcu.iras.bi.platform.domain.dto.Filter;
import com.zjrcu.iras.bi.platform.domain.dto.Metric;
import com.zjrcu.iras.bi.platform.domain.dto.QueryResult;
import com.zjrcu.iras.bi.platform.domain.dto.QueryResult.FieldMetadata;
import com.zjrcu.iras.bi.platform.mapper.DatasetMapper;
import com.zjrcu.iras.bi.platform.service.IQueryExecutor;
import com.zjrcu.iras.common.core.domain.entity.SysUser;
import com.zjrcu.iras.common.exception.ServiceException;
import com.zjrcu.iras.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.*;

/**
 * 查询执行器实现
 * 执行数据集查询并应用筛选、计算字段和权限
 *
 * @author iras
 */
@Component
public class QueryExecutor implements IQueryExecutor {
    private static final Logger log = LoggerFactory.getLogger(QueryExecutor.class);

    /**
     * 查询超时时间(秒)
     */
    private static final int QUERY_TIMEOUT = 30;

    /**
     * 最大返回行数
     */
    private static final int MAX_ROWS = 100000;

    @Autowired
    private DatasetMapper datasetMapper;

    @Autowired
    private DataSourceManager dataSourceManager;

    @Autowired(required = false)
    private com.zjrcu.iras.bi.platform.cache.CacheManager cacheManager;

    @Autowired(required = false)
    private com.zjrcu.iras.bi.platform.monitor.QueryPerformanceMonitor performanceMonitor;

    /**
     * 执行数据集查询
     *
     * @param datasetId 数据集ID
     * @param filters 筛选条件列表
     * @param user 当前用户(用于权限过滤)
     * @return 查询结果
     */
    @Override
    public QueryResult executeQuery(Long datasetId, List<Filter> filters, SysUser user) {
        if (datasetId == null) {
            return QueryResult.failure("数据集ID不能为空");
        }

        long startTime = System.currentTimeMillis();

        try {
            // 1. 获取数据集配置
            Dataset dataset = datasetMapper.selectDatasetById(datasetId);
            if (dataset == null) {
                return QueryResult.failure("数据集不存在: " + datasetId);
            }

            if ("1".equals(dataset.getStatus())) {
                return QueryResult.failure("数据集已停用");
            }

            // 2. 尝试从缓存获取结果
            QueryResult cachedResult = getCachedResult(datasetId, filters);
            if (cachedResult != null) {
                long executionTime = System.currentTimeMillis() - startTime;
                cachedResult.setExecutionTime(executionTime);
                cachedResult.setFromCache(true);
                
                // 记录性能指标
                recordPerformance(datasetId, executionTime, cachedResult.getTotalRows(), true, true);
                
                log.debug("查询结果来自缓存: datasetId={}, duration={}ms", datasetId, executionTime);
                return cachedResult;
            }

            // 3. 根据数据集类型执行查询
            QueryResult result;
            if (dataset.isDirect()) {
                result = executeDirectQuery(dataset, filters, user);
            } else if (dataset.isExtract()) {
                result = executeExtractQuery(dataset, filters, user);
            } else {
                return QueryResult.failure("不支持的数据集类型: " + dataset.getType());
            }

            long executionTime = System.currentTimeMillis() - startTime;
            result.setExecutionTime(executionTime);
            result.setFromCache(false);

            // 4. 将结果存入缓存
            cacheQueryResult(datasetId, filters, result);

            // 5. 记录性能指标
            recordPerformance(datasetId, executionTime, result.getTotalRows(), false, result.isSuccess());

            log.info("查询执行成功: datasetId={}, type={}, rows={}, duration={}ms",
                    datasetId, dataset.getType(), result.getTotalRows(), executionTime);

            return result;

        } catch (SQLTimeoutException e) {
            long duration = System.currentTimeMillis() - startTime;
            log.warn("查询超时: datasetId={}, duration={}ms", datasetId, duration);
            
            // 记录失败的性能指标
            recordPerformance(datasetId, duration, 0, false, false);
            
            return QueryResult.failure("查询超时,建议转换为抽取数据集");
        } catch (SQLException e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("查询执行失败: datasetId={}, error={}", datasetId, e.getMessage());
            
            // 记录失败的性能指标
            recordPerformance(datasetId, duration, 0, false, false);
            
            return QueryResult.failure("查询执行失败: " + e.getMessage());
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("查询执行异常: datasetId={}, error={}", datasetId, e.getMessage(), e);
            
            // 记录失败的性能指标
            recordPerformance(datasetId, duration, 0, false, false);
            return QueryResult.failure("查询执行异常: " + e.getMessage());
        }
    }

    /**
     * 执行聚合查询
     *
     * @param datasetId 数据集ID
     * @param dimensions 维度字段列表
     * @param metrics 度量字段列表
     * @param filters 筛选条件列表
     * @param user 当前用户
     * @return 聚合结果
     */
    @Override
    public QueryResult executeAggregation(Long datasetId, List<String> dimensions,
                                         List<Metric> metrics, List<Filter> filters, SysUser user) {
        if (datasetId == null) {
            return QueryResult.failure("数据集ID不能为空");
        }

        if ((dimensions == null || dimensions.isEmpty()) && (metrics == null || metrics.isEmpty())) {
            return QueryResult.failure("维度和度量不能同时为空");
        }

        long startTime = System.currentTimeMillis();

        try {
            // 1. 获取数据集配置
            Dataset dataset = datasetMapper.selectDatasetById(datasetId);
            if (dataset == null) {
                return QueryResult.failure("数据集不存在: " + datasetId);
            }

            if ("1".equals(dataset.getStatus())) {
                return QueryResult.failure("数据集已停用");
            }

            // 2. 根据数据集类型执行聚合查询
            QueryResult result;
            if (dataset.isDirect()) {
                result = executeDirectAggregation(dataset, dimensions, metrics, filters, user);
            } else if (dataset.isExtract()) {
                result = executeExtractAggregation(dataset, dimensions, metrics, filters, user);
            } else {
                return QueryResult.failure("不支持的数据集类型: " + dataset.getType());
            }

            long executionTime = System.currentTimeMillis() - startTime;
            result.setExecutionTime(executionTime);

            log.info("聚合查询执行成功: datasetId={}, type={}, rows={}, duration={}ms",
                    datasetId, dataset.getType(), result.getTotalRows(), executionTime);

            return result;

        } catch (SQLTimeoutException e) {
            long duration = System.currentTimeMillis() - startTime;
            log.warn("聚合查询超时: datasetId={}, duration={}ms", datasetId, duration);
            return QueryResult.failure("查询超时,建议转换为抽取数据集");
        } catch (SQLException e) {
            log.error("聚合查询执行失败: datasetId={}, error={}", datasetId, e.getMessage());
            return QueryResult.failure("聚合查询执行失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("聚合查询执行异常: datasetId={}, error={}", datasetId, e.getMessage(), e);
            return QueryResult.failure("聚合查询执行异常: " + e.getMessage());
        }
    }

    /**
     * 执行直连数据集查询
     *
     * @param dataset 数据集
     * @param filters 筛选条件
     * @param user 当前用户
     * @return 查询结果
     */
    private QueryResult executeDirectQuery(Dataset dataset, List<Filter> filters, SysUser user) throws SQLException {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            // 1. 获取数据源连接
            connection = dataSourceManager.getConnection(dataset.getDatasourceId());

            // 2. 构建SQL查询
            String sql = buildDirectQuerySql(dataset, filters, user);
            log.debug("执行直连查询SQL: {}", sql);

            // 3. 执行查询
            statement = connection.createStatement();
            statement.setQueryTimeout(QUERY_TIMEOUT);
            statement.setMaxRows(MAX_ROWS);
            resultSet = statement.executeQuery(sql);

            // 4. 解析结果
            return parseResultSet(resultSet, dataset);

        } finally {
            closeResources(resultSet, statement, connection);
        }
    }

    /**
     * 执行抽取数据集查询
     *
     * @param dataset 数据集
     * @param filters 筛选条件
     * @param user 当前用户
     * @return 查询结果
     */
    private QueryResult executeExtractQuery(Dataset dataset, List<Filter> filters, SysUser user) throws SQLException {
        // 抽取数据集从bi_extract_data表查询
        // 注意: 这里需要获取系统默认的MySQL连接,而不是数据集绑定的数据源
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            // 获取系统数据库连接(用于查询bi_extract_data表)
            // 这里暂时使用数据集的数据源连接,实际应该使用系统数据库连接
            connection = dataSourceManager.getConnection(dataset.getDatasourceId());

            // 构建查询抽取数据的SQL
            String sql = buildExtractQuerySql(dataset, filters, user);
            log.debug("执行抽取查询SQL: {}", sql);

            statement = connection.createStatement();
            statement.setQueryTimeout(QUERY_TIMEOUT);
            statement.setMaxRows(MAX_ROWS);
            resultSet = statement.executeQuery(sql);

            return parseResultSet(resultSet, dataset);

        } finally {
            closeResources(resultSet, statement, connection);
        }
    }

    /**
     * 执行直连数据集聚合查询
     *
     * @param dataset 数据集
     * @param dimensions 维度字段
     * @param metrics 度量字段
     * @param filters 筛选条件
     * @param user 当前用户
     * @return 查询结果
     */
    private QueryResult executeDirectAggregation(Dataset dataset, List<String> dimensions,
                                                List<Metric> metrics, List<Filter> filters, SysUser user) throws SQLException {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            connection = dataSourceManager.getConnection(dataset.getDatasourceId());

            String sql = buildAggregationSql(dataset, dimensions, metrics, filters, user);
            log.debug("执行直连聚合查询SQL: {}", sql);

            statement = connection.createStatement();
            statement.setQueryTimeout(QUERY_TIMEOUT);
            statement.setMaxRows(MAX_ROWS);
            resultSet = statement.executeQuery(sql);

            return parseResultSet(resultSet, dataset);

        } finally {
            closeResources(resultSet, statement, connection);
        }
    }

    /**
     * 执行抽取数据集聚合查询
     *
     * @param dataset 数据集
     * @param dimensions 维度字段
     * @param metrics 度量字段
     * @param filters 筛选条件
     * @param user 当前用户
     * @return 查询结果
     */
    private QueryResult executeExtractAggregation(Dataset dataset, List<String> dimensions,
                                                  List<Metric> metrics, List<Filter> filters, SysUser user) throws SQLException {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            connection = dataSourceManager.getConnection(dataset.getDatasourceId());

            String sql = buildExtractAggregationSql(dataset, dimensions, metrics, filters, user);
            log.debug("执行抽取聚合查询SQL: {}", sql);

            statement = connection.createStatement();
            statement.setQueryTimeout(QUERY_TIMEOUT);
            statement.setMaxRows(MAX_ROWS);
            resultSet = statement.executeQuery(sql);

            return parseResultSet(resultSet, dataset);

        } finally {
            closeResources(resultSet, statement, connection);
        }
    }

    /**
     * 构建直连查询SQL
     *
     * @param dataset 数据集
     * @param filters 筛选条件
     * @param user 当前用户
     * @return SQL语句
     */
    private String buildDirectQuerySql(Dataset dataset, List<Filter> filters, SysUser user) {
        StringBuilder sql = new StringBuilder();
        Map<String, Object> queryConfig = dataset.getQueryConfigMap();
        Map<String, Object> fieldConfig = dataset.getFieldConfigMap();

        // 1. 构建SELECT子句(包含计算字段)
        sql.append("SELECT ");
        sql.append(buildSelectClause(fieldConfig));

        // 2. 构建FROM子句
        sql.append(" FROM ");
        String sourceType = (String) queryConfig.get("sourceType");
        if ("table".equalsIgnoreCase(sourceType)) {
            String tableName = (String) queryConfig.get("tableName");
            sql.append(tableName);
        } else if ("sql".equalsIgnoreCase(sourceType)) {
            String customSql = (String) queryConfig.get("sql");
            sql.append("(").append(customSql).append(") AS t");
        }

        // 3. 构建WHERE子句(包含筛选条件和权限过滤)
        String whereClause = buildWhereClause(filters, user);
        if (StringUtils.isNotEmpty(whereClause)) {
            sql.append(" WHERE ").append(whereClause);
        }

        return sql.toString();
    }

    /**
     * 构建抽取查询SQL
     *
     * @param dataset 数据集
     * @param filters 筛选条件
     * @param user 当前用户
     * @return SQL语句
     */
    private String buildExtractQuerySql(Dataset dataset, List<Filter> filters, SysUser user) {
        StringBuilder sql = new StringBuilder();

        // 从bi_extract_data表查询
        sql.append("SELECT data_content FROM bi_extract_data");
        sql.append(" WHERE dataset_id = ").append(dataset.getId());

        // 注意: 这是简化实现,实际应该解析JSON并应用筛选
        // 完整实现需要使用MySQL的JSON函数或在应用层过滤

        return sql.toString();
    }

    /**
     * 构建聚合查询SQL
     *
     * @param dataset 数据集
     * @param dimensions 维度字段
     * @param metrics 度量字段
     * @param filters 筛选条件
     * @param user 当前用户
     * @return SQL语句
     */
    private String buildAggregationSql(Dataset dataset, List<String> dimensions,
                                      List<Metric> metrics, List<Filter> filters, SysUser user) {
        StringBuilder sql = new StringBuilder();
        Map<String, Object> queryConfig = dataset.getQueryConfigMap();

        // 1. 构建SELECT子句(维度 + 聚合度量)
        sql.append("SELECT ");

        List<String> selectItems = new ArrayList<>();

        // 添加维度字段
        if (dimensions != null && !dimensions.isEmpty()) {
            selectItems.addAll(dimensions);
        }

        // 添加度量字段
        if (metrics != null && !metrics.isEmpty()) {
            for (Metric metric : metrics) {
                String metricExpr = metric.getAggregation() + "(" + metric.getField() + ")";
                if (StringUtils.isNotEmpty(metric.getAlias())) {
                    metricExpr += " AS " + metric.getAlias();
                }
                selectItems.add(metricExpr);
            }
        }

        sql.append(String.join(", ", selectItems));

        // 2. 构建FROM子句
        sql.append(" FROM ");
        String sourceType = (String) queryConfig.get("sourceType");
        if ("table".equalsIgnoreCase(sourceType)) {
            String tableName = (String) queryConfig.get("tableName");
            sql.append(tableName);
        } else if ("sql".equalsIgnoreCase(sourceType)) {
            String customSql = (String) queryConfig.get("sql");
            sql.append("(").append(customSql).append(") AS t");
        }

        // 3. 构建WHERE子句
        String whereClause = buildWhereClause(filters, user);
        if (StringUtils.isNotEmpty(whereClause)) {
            sql.append(" WHERE ").append(whereClause);
        }

        // 4. 构建GROUP BY子句 - 给维度字段加上反引号
        if (dimensions != null && !dimensions.isEmpty()) {
            String quotedDimensions = dimensions.stream()
                    .map(this::quoteFieldName)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");
            sql.append(" GROUP BY ").append(quotedDimensions);
        }

        return sql.toString();
    }

    /**
     * 构建抽取聚合查询SQL
     *
     * @param dataset 数据集
     * @param dimensions 维度字段
     * @param metrics 度量字段
     * @param filters 筛选条件
     * @param user 当前用户
     * @return SQL语句
     */
    private String buildExtractAggregationSql(Dataset dataset, List<String> dimensions,
                                             List<Metric> metrics, List<Filter> filters, SysUser user) {
        // 简化实现: 从bi_extract_data表查询并在应用层聚合
        // 完整实现需要使用MySQL的JSON函数进行聚合
        return buildExtractQuerySql(dataset, filters, user);
    }

    /**
     * 构建SELECT子句(包含计算字段)
     *
     * @param fieldConfig 字段配置
     * @return SELECT子句
     */
    private String buildSelectClause(Map<String, Object> fieldConfig) {
        if (fieldConfig == null || fieldConfig.isEmpty()) {
            return "*";
        }

        List<Object> fields = (List<Object>) fieldConfig.get("fields");
        if (fields == null || fields.isEmpty()) {
            return "*";
        }

        List<String> selectItems = new ArrayList<>();
        for (Object fieldObj : fields) {
            Map<String, Object> field = (Map<String, Object>) fieldObj;
            Boolean visible = (Boolean) field.getOrDefault("visible", true);

            if (!visible) {
                continue;
            }

            String name = (String) field.get("name");
            String alias = (String) field.get("alias");
            Boolean calculated = (Boolean) field.getOrDefault("calculated", false);

            if (calculated) {
                // 计算字段: 使用表达式
                String expression = (String) field.get("expression");
                if (StringUtils.isNotEmpty(expression)) {
                    String quotedAlias = quoteFieldName(name);
                    selectItems.add("(" + expression + ") AS " + quotedAlias);
                }
            } else {
                // 普通字段 - 给字段名加上反引号
                String quotedName = quoteFieldName(name);
                if (StringUtils.isNotEmpty(alias) && !alias.equals(name)) {
                    String quotedAlias = quoteFieldName(alias);
                    selectItems.add(quotedName + " AS " + quotedAlias);
                } else {
                    selectItems.add(quotedName);
                }
            }
        }

        return selectItems.isEmpty() ? "*" : String.join(", ", selectItems);
    }

    /**
     * 构建WHERE子句(包含筛选条件和权限过滤)
     *
     * @param filters 筛选条件
     * @param user 当前用户
     * @return WHERE子句
     */
    private String buildWhereClause(List<Filter> filters, SysUser user) {
        List<String> conditions = new ArrayList<>();

        // 1. 添加筛选条件
        if (filters != null && !filters.isEmpty()) {
            for (Filter filter : filters) {
                String condition = buildFilterCondition(filter);
                if (StringUtils.isNotEmpty(condition)) {
                    conditions.add(condition);
                }
            }
        }

        // 2. 添加行级权限过滤(基于用户部门ID)
        // 注意: 暂时注释掉硬编码的dept_id过滤，因为不是所有表都有这个字段
        // 数据权限应该在数据集配置中定义，而不是硬编码
        // TODO: 实现基于数据集配置的灵活权限过滤机制
        /*
        if (user != null && user.getDeptId() != null) {
            // 假设数据表中有dept_id字段用于权限过滤
            // 实际实现中应该根据数据集配置决定是否应用权限过滤
            conditions.add("dept_id = " + user.getDeptId());
        }
        */

        return conditions.isEmpty() ? "" : String.join(" AND ", conditions);
    }

    /**
     * 构建单个筛选条件
     *
     * @param filter 筛选条件
     * @return SQL条件表达式
     */
    private String buildFilterCondition(Filter filter) {
        if (filter == null || StringUtils.isEmpty(filter.getField())) {
            return "";
        }

        String field = filter.getField();
        String operator = filter.getOperator();
        Object value = filter.getValue();

        if (StringUtils.isEmpty(operator)) {
            operator = "eq";
        }

        // 给字段名加上反引号，支持中文字段名和特殊字符
        String quotedField = quoteFieldName(field);

        switch (operator.toLowerCase()) {
            case "eq":
                return quotedField + " = " + formatValue(value);
            case "ne":
                return quotedField + " != " + formatValue(value);
            case "gt":
                return quotedField + " > " + formatValue(value);
            case "gte":
                return quotedField + " >= " + formatValue(value);
            case "lt":
                return quotedField + " < " + formatValue(value);
            case "lte":
                return quotedField + " <= " + formatValue(value);
            case "like":
                return quotedField + " LIKE '%" + value + "%'";
            case "in":
                List<Object> values = filter.getValues();
                if (values != null && !values.isEmpty()) {
                    String inValues = values.stream()
                            .map(this::formatValue)
                            .reduce((a, b) -> a + ", " + b)
                            .orElse("");
                    return quotedField + " IN (" + inValues + ")";
                }
                return "";
            case "between":
                List<Object> rangeValues = filter.getValues();
                if (rangeValues != null && rangeValues.size() == 2) {
                    return quotedField + " BETWEEN " + formatValue(rangeValues.get(0)) +
                            " AND " + formatValue(rangeValues.get(1));
                }
                return "";
            default:
                log.warn("不支持的筛选操作符: {}", operator);
                return "";
        }
    }

    /**
     * 给字段名加上反引号，支持中文字段名和特殊字符
     *
     * @param fieldName 字段名
     * @return 加上反引号的字段名
     */
    private String quoteFieldName(String fieldName) {
        if (fieldName == null || fieldName.isEmpty()) {
            return "";
        }

        // 如果已经包含反引号，直接返回
        if (fieldName.startsWith("`") && fieldName.endsWith("`")) {
            return fieldName;
        }

        // 如果字段名包含特殊字符或中文，需要加上反引号
        if (!isSimpleIdentifier(fieldName)) {
            return "`" + fieldName + "`";
        }

        return fieldName;
    }

    /**
     * 判断字段名是否是简单标识符（只包含字母、数字、下划线，且不以数字开头）
     *
     * @param identifier 标识符
     * @return 是否是简单标识符
     */
    private boolean isSimpleIdentifier(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            return false;
        }

        // 检查是否只包含字母、数字、下划线
        boolean isSimple = identifier.matches("^[a-zA-Z_][a-zA-Z0-9_]*$");

        // 如果不是简单的英文标识符，可能包含中文或其他特殊字符
        return isSimple;
    }

    /**
     * 格式化值(添加引号等)
     *
     * @param value 值
     * @return 格式化后的值
     */
    private String formatValue(Object value) {
        if (value == null) {
            return "NULL";
        }

        if (value instanceof String) {
            // 字符串值需要添加单引号,并转义特殊字符
            String strValue = value.toString().replace("'", "''");
            return "'" + strValue + "'";
        } else if (value instanceof Number) {
            return value.toString();
        } else if (value instanceof Boolean) {
            return ((Boolean) value) ? "1" : "0";
        } else {
            return "'" + value.toString() + "'";
        }
    }

    /**
     * 解析ResultSet为QueryResult
     *
     * @param resultSet 结果集
     * @param dataset 数据集
     * @return 查询结果
     */
    private QueryResult parseResultSet(ResultSet resultSet, Dataset dataset) throws SQLException {
        List<Map<String, Object>> data = new ArrayList<>();
        List<FieldMetadata> fields = new ArrayList<>();

        // 获取元数据
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();

        // 构建字段元数据
        Map<String, Object> fieldConfig = dataset.getFieldConfigMap();
        for (int i = 1; i <= columnCount; i++) {
            // getColumnName() 返回原始数据库字段名（英文）
            // getColumnLabel() 返回显示标签（可能是别名）
            String originalColumnName = metaData.getColumnName(i);  // 原始字段名
            String columnLabel = metaData.getColumnLabel(i);        // 显示标签
            String columnType = metaData.getColumnTypeName(i);

            // 从字段配置中获取别名和计算字段标记
            String alias = columnLabel;  // 默认使用显示标签作为别名
            boolean calculated = false;

            if (fieldConfig != null && !fieldConfig.isEmpty()) {
                List<Object> fieldList = (List<Object>) fieldConfig.get("fields");
                if (fieldList != null) {
                    for (Object fieldObj : fieldList) {
                        Map<String, Object> field = (Map<String, Object>) fieldObj;
                        String name = (String) field.get("name");
                        // 匹配配置中的字段名（可能是原始字段名或标签）
                        if (name.equals(originalColumnName) || name.equals(columnLabel)) {
                            alias = (String) field.getOrDefault("alias", columnLabel);
                            calculated = (Boolean) field.getOrDefault("calculated", false);
                            break;
                        }
                    }
                }
            }

            // 使用原始字段名作为name，显示标签作为alias
            // 这样前端就能获取到真实的数据库字段名了
            fields.add(new FieldMetadata(originalColumnName, alias, columnType, calculated));
        }

        // 读取数据行
        while (resultSet.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                // 使用原始字段名(英文)作为数据键，与FieldMetadata的name保持一致
                String columnName = metaData.getColumnName(i);
                Object value = resultSet.getObject(i);
                row.put(columnName, value);
            }
            data.add(row);
        }

        return QueryResult.success(data, fields, 0);
    }

    /**
     * 关闭资源
     *
     * @param resultSet 结果集
     * @param statement 语句
     * @param connection 连接
     */
    private void closeResources(ResultSet resultSet, Statement statement, Connection connection) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                log.warn("关闭ResultSet失败: {}", e.getMessage());
            }
        }

        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                log.warn("关闭Statement失败: {}", e.getMessage());
            }
        }

        if (connection != null) {
            dataSourceManager.releaseConnection(connection);
        }
    }

    /**
     * 从缓存获取查询结果
     *
     * @param datasetId 数据集ID
     * @param filters 筛选条件
     * @return 缓存的查询结果,如果不存在则返回null
     */
    private QueryResult getCachedResult(Long datasetId, List<Filter> filters) {
        if (cacheManager == null) {
            return null;
        }

        try {
            String filterHash = generateFilterHash(filters);
            String cacheKey = cacheManager.generateCacheKey(datasetId, filterHash);
            return cacheManager.get(cacheKey);
        } catch (Exception e) {
            log.warn("从缓存获取结果失败: datasetId={}, error={}", datasetId, e.getMessage());
            return null;
        }
    }

    /**
     * 将查询结果存入缓存
     *
     * @param datasetId 数据集ID
     * @param filters 筛选条件
     * @param result 查询结果
     */
    private void cacheQueryResult(Long datasetId, List<Filter> filters, QueryResult result) {
        if (cacheManager == null || result == null || !result.isSuccess()) {
            return;
        }

        try {
            String filterHash = generateFilterHash(filters);
            String cacheKey = cacheManager.generateCacheKey(datasetId, filterHash);
            
            // 默认缓存5分钟
            int ttl = 300;
            
            cacheManager.put(cacheKey, result, ttl);
            log.debug("查询结果已缓存: datasetId={}, cacheKey={}, ttl={}秒", datasetId, cacheKey, ttl);
        } catch (Exception e) {
            log.warn("缓存查询结果失败: datasetId={}, error={}", datasetId, e.getMessage());
        }
    }

    /**
     * 生成筛选条件哈希值
     *
     * @param filters 筛选条件列表
     * @return 哈希值
     */
    private String generateFilterHash(List<Filter> filters) {
        if (filters == null || filters.isEmpty()) {
            return "nofilter";
        }

        // 对筛选条件进行排序以确保相同筛选条件生成相同哈希
        List<Filter> sortedFilters = new ArrayList<>(filters);
        sortedFilters.sort(Comparator.comparing(Filter::getField));

        StringBuilder sb = new StringBuilder();
        for (Filter filter : sortedFilters) {
            sb.append(filter.getField())
              .append(":")
              .append(filter.getOperator())
              .append(":")
              .append(filter.getValue())
              .append(";");
        }

        // 使用简单的哈希算法
        return Integer.toHexString(sb.toString().hashCode());
    }

    /**
     * 使数据集缓存失效
     *
     * @param datasetId 数据集ID
     */
    public void invalidateCache(Long datasetId) {
        if (cacheManager != null && datasetId != null) {
            cacheManager.invalidate(datasetId);
            log.info("数据集缓存已失效: datasetId={}", datasetId);
        }
    }

    /**
     * 记录查询性能指标
     *
     * @param datasetId 数据集ID
     * @param executionTime 执行时间
     * @param rowCount 返回行数
     * @param fromCache 是否来自缓存
     * @param success 是否成功
     */
    private void recordPerformance(Long datasetId, long executionTime, long rowCount, boolean fromCache, boolean success) {
        if (performanceMonitor != null) {
            performanceMonitor.recordQuery(datasetId, executionTime, rowCount, fromCache, success);
        }
    }
}

