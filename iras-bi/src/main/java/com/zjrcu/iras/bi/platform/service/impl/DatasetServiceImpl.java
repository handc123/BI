package com.zjrcu.iras.bi.platform.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zjrcu.iras.bi.platform.domain.Dataset;
import com.zjrcu.iras.bi.platform.domain.Visualization;
import com.zjrcu.iras.bi.platform.domain.dto.Filter;
import com.zjrcu.iras.bi.platform.domain.dto.MetricConfigDTO;
import com.zjrcu.iras.bi.platform.domain.dto.QueryResult;
import com.zjrcu.iras.bi.platform.engine.MetricConverter;
import com.zjrcu.iras.bi.platform.engine.SQLDialectAdapter;
import com.zjrcu.iras.bi.platform.mapper.DatasetMapper;
import com.zjrcu.iras.bi.platform.mapper.VisualizationMapper;
import com.zjrcu.iras.bi.platform.security.SQLValidator;
import com.zjrcu.iras.bi.platform.service.IDatasetService;
import com.zjrcu.iras.bi.platform.service.IQueryExecutor;
import com.zjrcu.iras.common.exception.ServiceException;
import com.zjrcu.iras.common.utils.SecurityUtils;
import com.zjrcu.iras.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * BI数据集服务实现
 *
 * @author iras
 */
@Service
public class DatasetServiceImpl implements IDatasetService {
    private static final Logger log = LoggerFactory.getLogger(DatasetServiceImpl.class);

    @Autowired
    private DatasetMapper datasetMapper;

    @Autowired
    private VisualizationMapper visualizationMapper;

    @Autowired
    private IQueryExecutor queryExecutor;

    @Autowired
    private MetricConverter metricConverter;

    @Autowired
    private SQLValidator sqlValidator;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 查询数据集列表
     *
     * @param dataset 数据集查询条件
     * @return 数据集列表
     */
    @Override
    public List<Dataset> selectDatasetList(Dataset dataset) {
        return datasetMapper.selectDatasetList(dataset);
    }

    /**
     * 根据ID查询数据集
     *
     * @param id 数据集ID
     * @return 数据集信息
     */
    @Override
    public Dataset selectDatasetById(Long id) {
        if (id == null) {
            throw new ServiceException("数据集ID不能为空");
        }
        return datasetMapper.selectDatasetById(id);
    }

    /**
     * 新增数据集
     *
     * @param dataset 数据集信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertDataset(Dataset dataset) {
        // 1. 验证数据集配置
        String validationError = validateDatasetConfig(dataset);
        if (StringUtils.isNotEmpty(validationError)) {
            throw new ServiceException(validationError);
        }

        // 2. 设置创建信息
        dataset.setCreateBy(SecurityUtils.getUsername());

        // 3. 设置默认状态
        if (StringUtils.isEmpty(dataset.getStatus())) {
            dataset.setStatus("0");
        }

        // 4. 插入数据集
        int result = datasetMapper.insertDataset(dataset);

        log.info("新增数据集成功: id={}, name={}, type={}", 
                dataset.getId(), dataset.getName(), dataset.getType());

        return result;
    }

    /**
     * 修改数据集
     *
     * @param dataset 数据集信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateDataset(Dataset dataset) {
        if (dataset.getId() == null) {
            throw new ServiceException("数据集ID不能为空");
        }

        // 1. 检查数据集是否存在
        Dataset existingDataset = datasetMapper.selectDatasetById(dataset.getId());
        if (existingDataset == null) {
            throw new ServiceException("数据集不存在");
        }

        // 2. 验证数据集配置
        String validationError = validateDatasetConfig(dataset);
        if (StringUtils.isNotEmpty(validationError)) {
            throw new ServiceException(validationError);
        }

        // 3. 设置更新信息
        dataset.setUpdateBy(SecurityUtils.getUsername());

        // 4. 更新数据集
        int result = datasetMapper.updateDataset(dataset);

        log.info("更新数据集成功: id={}, name={}, type={}", 
                dataset.getId(), dataset.getName(), dataset.getType());

        return result;
    }

    /**
     * 删除数据集
     *
     * @param id 数据集ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteDatasetById(Long id) {
        if (id == null) {
            throw new ServiceException("数据集ID不能为空");
        }

        // 1. 检查数据集是否存在
        Dataset dataset = datasetMapper.selectDatasetById(id);
        if (dataset == null) {
            throw new ServiceException("数据集不存在");
        }

        // 2. 检查是否有可视化依赖此数据集
        List<Visualization> visualizations = visualizationMapper.selectVisualizationByDatasetId(id);
        if (visualizations != null && !visualizations.isEmpty()) {
            throw new ServiceException("数据集被" + visualizations.size() + "个可视化使用,无法删除");
        }

        // 3. 删除数据集
        int result = datasetMapper.deleteDatasetById(id);

        log.info("删除数据集成功: id={}, name={}", id, dataset.getName());

        return result;
    }

    /**
     * 批量删除数据集
     *
     * @param ids 数据集ID数组
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteDatasetByIds(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new ServiceException("数据集ID不能为空");
        }

        // 检查每个数据集的依赖关系
        for (Long id : ids) {
            List<Visualization> visualizations = visualizationMapper.selectVisualizationByDatasetId(id);
            if (visualizations != null && !visualizations.isEmpty()) {
                Dataset dataset = datasetMapper.selectDatasetById(id);
                String datasetName = dataset != null ? dataset.getName() : String.valueOf(id);
                throw new ServiceException("数据集[" + datasetName + "]被" + visualizations.size() + "个可视化使用,无法删除");
            }
        }

        // 批量删除
        int result = datasetMapper.deleteDatasetByIds(ids);

        log.info("批量删除数据集成功: count={}", result);

        return result;
    }

    /**
     * 预览数据集数据
     *
     * @param id 数据集ID
     * @param filters 筛选条件列表
     * @return 查询结果
     */
    @Override
    public QueryResult previewDataset(Long id, List<Filter> filters) {
        if (id == null) {
            return QueryResult.failure("数据集ID不能为空");
        }

        try {
            // 使用QueryExecutor执行查询
            // 注意: 预览时不应用用户权限过滤,传入null
            QueryResult result = queryExecutor.executeQuery(id, filters, null);

            log.info("预览数据集成功: id={}, rows={}", id, result.getTotalRows());

            return result;

        } catch (Exception e) {
            log.error("预览数据集失败: id={}, error={}", id, e.getMessage(), e);
            return QueryResult.failure("预览数据集失败: " + e.getMessage());
        }
    }

    /**
     * 验证数据集配置
     *
     * @param dataset 数据集信息
     * @return 验证结果消息,如果验证通过返回null
     */
    @Override
    public String validateDatasetConfig(Dataset dataset) {
        if (dataset == null) {
            return "数据集信息不能为空";
        }

        // 1. 验证基本字段
        if (StringUtils.isEmpty(dataset.getName())) {
            return "数据集名称不能为空";
        }

        if (dataset.getDatasourceId() == null) {
            return "数据源ID不能为空";
        }

        if (StringUtils.isEmpty(dataset.getType())) {
            return "数据集类型不能为空";
        }

        // 2. 验证数据集类型
        if (!"direct".equalsIgnoreCase(dataset.getType()) && 
            !"extract".equalsIgnoreCase(dataset.getType())) {
            return "数据集类型必须是direct或extract";
        }

        // 3. 验证查询配置
        if (StringUtils.isEmpty(dataset.getQueryConfig())) {
            return "查询配置不能为空";
        }

        String queryConfigError = validateQueryConfig(dataset.getQueryConfig());
        if (StringUtils.isNotEmpty(queryConfigError)) {
            return queryConfigError;
        }

        // 4. 验证字段配置(如果提供)
        if (StringUtils.isNotEmpty(dataset.getFieldConfig())) {
            String fieldConfigError = validateFieldConfig(dataset.getFieldConfig());
            if (StringUtils.isNotEmpty(fieldConfigError)) {
                return fieldConfigError;
            }
        }

        // 5. 验证抽取配置(仅抽取类型)
        if (dataset.isExtract()) {
            if (StringUtils.isEmpty(dataset.getExtractConfig())) {
                return "抽取数据集必须提供抽取配置";
            }

            String extractConfigError = validateExtractConfig(dataset.getExtractConfig());
            if (StringUtils.isNotEmpty(extractConfigError)) {
                return extractConfigError;
            }
        }

        return null;
    }

    /**
     * 验证查询配置JSON结构
     *
     * @param queryConfig 查询配置JSON字符串
     * @return 验证结果消息,如果验证通过返回null
     */
    private String validateQueryConfig(String queryConfig) {
        try {
            Map<String, Object> config = objectMapper.readValue(queryConfig, Map.class);

            // 1. 验证sourceType字段
            String sourceType = (String) config.get("sourceType");
            if (StringUtils.isEmpty(sourceType)) {
                return "查询配置必须包含sourceType字段";
            }

            if (!"table".equalsIgnoreCase(sourceType) && !"sql".equalsIgnoreCase(sourceType)) {
                return "sourceType必须是table或sql";
            }

            // 2. 根据sourceType验证必需字段
            if ("table".equalsIgnoreCase(sourceType)) {
                String tableName = (String) config.get("tableName");
                if (StringUtils.isEmpty(tableName)) {
                    return "sourceType为table时必须提供tableName字段";
                }
            } else if ("sql".equalsIgnoreCase(sourceType)) {
                String sql = (String) config.get("sql");
                if (StringUtils.isEmpty(sql)) {
                    return "sourceType为sql时必须提供sql字段";
                }
            }

            // 3. 验证joins配置(可选)
            if (config.containsKey("joins")) {
                Object joins = config.get("joins");
                if (!(joins instanceof List)) {
                    return "joins字段必须是数组";
                }

                List<Object> joinList = (List<Object>) joins;
                for (Object joinObj : joinList) {
                    if (!(joinObj instanceof Map)) {
                        return "joins数组元素必须是对象";
                    }

                    Map<String, Object> join = (Map<String, Object>) joinObj;
                    if (!join.containsKey("type") || !join.containsKey("table") || !join.containsKey("on")) {
                        return "join对象必须包含type、table和on字段";
                    }
                }
            }

            return null;

        } catch (JsonProcessingException e) {
            return "查询配置JSON格式错误: " + e.getMessage();
        } catch (Exception e) {
            return "查询配置验证失败: " + e.getMessage();
        }
    }

    /**
     * 验证字段配置JSON结构
     *
     * @param fieldConfig 字段配置JSON字符串
     * @return 验证结果消息,如果验证通过返回null
     */
    private String validateFieldConfig(String fieldConfig) {
        try {
            Map<String, Object> config = objectMapper.readValue(fieldConfig, Map.class);

            // 1. 验证fields字段
            if (!config.containsKey("fields")) {
                return "字段配置必须包含fields字段";
            }

            Object fields = config.get("fields");
            if (!(fields instanceof List)) {
                return "fields字段必须是数组";
            }

            // 2. 验证每个字段配置
            List<Object> fieldList = (List<Object>) fields;
            for (Object fieldObj : fieldList) {
                if (!(fieldObj instanceof Map)) {
                    return "fields数组元素必须是对象";
                }

                Map<String, Object> field = (Map<String, Object>) fieldObj;

                // 验证必需字段
                if (!field.containsKey("name")) {
                    return "字段配置必须包含name字段";
                }

                String name = (String) field.get("name");
                if (StringUtils.isEmpty(name)) {
                    return "字段name不能为空";
                }

                // 验证计算字段
                Boolean calculated = (Boolean) field.getOrDefault("calculated", false);
                if (calculated) {
                    String expression = (String) field.get("expression");
                    if (StringUtils.isEmpty(expression)) {
                        return "计算字段必须提供expression字段";
                    }
                }

                // 验证字段类型(可选)
                if (field.containsKey("type")) {
                    String type = (String) field.get("type");
                    if (StringUtils.isEmpty(type)) {
                        return "字段type不能为空字符串";
                    }
                }
            }

            return null;

        } catch (JsonProcessingException e) {
            return "字段配置JSON格式错误: " + e.getMessage();
        } catch (Exception e) {
            return "字段配置验证失败: " + e.getMessage();
        }
    }

    /**
     * 验证抽取配置JSON结构
     *
     * @param extractConfig 抽取配置JSON字符串
     * @return 验证结果消息,如果验证通过返回null
     */
    private String validateExtractConfig(String extractConfig) {
        try {
            Map<String, Object> config = objectMapper.readValue(extractConfig, Map.class);

            // 1. 验证schedule字段
            if (!config.containsKey("schedule")) {
                return "抽取配置必须包含schedule字段";
            }

            Object schedule = config.get("schedule");
            if (!(schedule instanceof Map)) {
                return "schedule字段必须是对象";
            }

            Map<String, Object> scheduleMap = (Map<String, Object>) schedule;

            // 2. 验证schedule必需字段
            if (!scheduleMap.containsKey("enabled")) {
                return "schedule必须包含enabled字段";
            }

            Boolean enabled = (Boolean) scheduleMap.get("enabled");
            if (enabled != null && enabled) {
                // 如果启用了调度,必须提供cronExpression
                String cronExpression = (String) scheduleMap.get("cronExpression");
                if (StringUtils.isEmpty(cronExpression)) {
                    return "启用调度时必须提供cronExpression字段";
                }
            }

            // 3. 验证增量抽取配置(可选)
            if (config.containsKey("incremental")) {
                Boolean incremental = (Boolean) config.get("incremental");
                if (incremental != null && incremental) {
                    String incrementalField = (String) config.get("incrementalField");
                    if (StringUtils.isEmpty(incrementalField)) {
                        return "启用增量抽取时必须提供incrementalField字段";
                    }
                }
            }

            return null;

        } catch (JsonProcessingException e) {
            return "抽取配置JSON格式错误: " + e.getMessage();
        } catch (Exception e) {
            return "抽取配置验证失败: " + e.getMessage();
        }
    }

    /**
     * 获取数据集字段元数据
     *
     * @param id 数据集ID
     * @return 字段元数据列表
     */
    @Override
    public List<Map<String, Object>> getDatasetFields(Long id) {
        if (id == null) {
            throw new ServiceException("数据集ID不能为空");
        }

        try {
            // 1. 检查数据集是否存在
            Dataset dataset = datasetMapper.selectDatasetById(id);
            if (dataset == null) {
                throw new ServiceException("数据集不存在");
            }

            // 2. 获取数据集的字段配置（包含中文别名）
            Map<String, Object> fieldConfigMap = dataset.getFieldConfigMap();
            Map<String, String> fieldAliasMap = new java.util.HashMap<>();
            
            if (fieldConfigMap != null && fieldConfigMap.containsKey("fields")) {
                List<Map<String, Object>> configFields = (List<Map<String, Object>>) fieldConfigMap.get("fields");
                if (configFields != null) {
                    for (Map<String, Object> configField : configFields) {
                        String name = (String) configField.get("name");
                        String alias = (String) configField.get("alias");
                        if (name != null && alias != null) {
                            fieldAliasMap.put(name, alias);
                        }
                    }
                }
            }

            // 3. 执行预览查询获取字段元数据(只获取1行数据以获取字段信息)
            QueryResult queryResult = queryExecutor.executeQuery(id, null, null);

            if (!queryResult.isSuccess()) {
                throw new ServiceException("获取字段元数据失败: " + queryResult.getErrorMessage());
            }

            // 4. 转换字段元数据为前端需要的格式
            List<QueryResult.FieldMetadata> fields = queryResult.getFields();
            List<Map<String, Object>> result = new java.util.ArrayList<>();

            for (QueryResult.FieldMetadata field : fields) {
                Map<String, Object> fieldMap = new java.util.HashMap<>();

                String fieldName = field.getName();
                
                // fieldName：原始数据库字段名（英文），用于SQL查询
                fieldMap.put("fieldName", fieldName);

                // comment：显示名称（优先使用fieldConfig中的alias，其次使用查询结果的alias，最后使用字段名）
                String comment = fieldAliasMap.getOrDefault(fieldName, 
                    field.getAlias() != null && !field.getAlias().isEmpty() ? field.getAlias() : fieldName);
                fieldMap.put("comment", comment);

                // dbFieldName：原始数据库字段名（与fieldName相同）
                fieldMap.put("dbFieldName", fieldName);

                fieldMap.put("fieldType", classifyFieldType(field.getType()));
                // 添加数据库字段类型，用于前端自动判断显示类型
                fieldMap.put("dataType", field.getType());
                result.add(fieldMap);
            }

            log.info("获取数据集字段元数据成功: id={}, fieldCount={}", id, result.size());

            return result;

        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取数据集字段元数据失败: id={}, error={}", id, e.getMessage(), e);
            throw new ServiceException("获取数据集字段元数据失败: " + e.getMessage());
        }
    }

    /**
     * 根据数据类型分类字段为维度或指标
     *
     * @param dataType 数据库字段类型
     * @return 'dimension' 或 'metric'
     */
    private String classifyFieldType(String dataType) {
        if (dataType == null) {
            return "dimension";
        }

        // 统一转为大写进行比较
        String upperType = dataType.toUpperCase();

        // 数值类型归类为指标
        if (upperType.contains("INT") ||
            upperType.contains("BIGINT") ||
            upperType.contains("SMALLINT") ||
            upperType.contains("TINYINT") ||
            upperType.contains("DECIMAL") ||
            upperType.contains("NUMERIC") ||
            upperType.contains("FLOAT") ||
            upperType.contains("DOUBLE") ||
            upperType.contains("REAL") ||
            upperType.contains("BIT")) {
            return "metric";
        }

        // 其他类型(字符串、日期、布尔等)归类为维度
        return "dimension";
    }

    /**
     * 验证指标配置
     * 需求: 9.1, 9.3, 9.5, 11.5, 12.1, 12.2
     *
     * @param config 指标配置
     * @return 验证结果消息,如果验证通过返回null
     */
    @Override
    public String validateMetricConfig(MetricConfigDTO config) {
        if (config == null) {
            return "指标配置不能为空";
        }

        try {
            // 1. 验证基础指标
            List<MetricConfigDTO.BaseMetric> baseMetrics = config.getBaseMetrics();
            if (baseMetrics != null) {
                for (MetricConfigDTO.BaseMetric metric : baseMetrics) {
                    // 验证字段名
                    try {
                        sqlValidator.validateFieldName(metric.getField());
                    } catch (SecurityException e) {
                        return "基础指标 [" + metric.getName() + "] 字段名验证失败: " + e.getMessage();
                    }

                    // 验证聚合函数
                    try {
                        sqlValidator.validateAggregation(metric.getAggregation());
                    } catch (SecurityException e) {
                        return "基础指标 [" + metric.getName() + "] 聚合函数验证失败: " + e.getMessage();
                    }
                }
            }

            // 2. 验证计算指标
            List<MetricConfigDTO.ComputedMetric> computedMetrics = config.getComputedMetrics();
            if (computedMetrics != null) {
                for (MetricConfigDTO.ComputedMetric metric : computedMetrics) {
                    String error = validateComputedMetric(metric);
                    if (error != null) {
                        return "计算指标 [" + metric.getName() + "] 验证失败: " + error;
                    }
                }
            }

            // 3. 验证指标数量限制
            int totalMetrics = (baseMetrics != null ? baseMetrics.size() : 0) +
                              (computedMetrics != null ? computedMetrics.size() : 0);
            if (totalMetrics > 20) {
                return "指标数量 " + totalMetrics + " 超过最大限制 20";
            }

            // 4. 验证指标名称唯一性
            Set<String> metricNames = new java.util.HashSet<>();
            if (baseMetrics != null) {
                for (MetricConfigDTO.BaseMetric metric : baseMetrics) {
                    if (!metricNames.add(metric.getName())) {
                        return "指标名称重复: " + metric.getName();
                    }
                }
            }
            if (computedMetrics != null) {
                for (MetricConfigDTO.ComputedMetric metric : computedMetrics) {
                    if (!metricNames.add(metric.getName())) {
                        return "指标名称重复: " + metric.getName();
                    }
                }
            }

            return null;

        } catch (Exception e) {
            log.error("验证指标配置失败: {}", e.getMessage(), e);
            return "验证指标配置失败: " + e.getMessage();
        }
    }

    /**
     * 验证单个计算指标
     *
     * @param metric 计算指标
     * @return 验证结果消息,如果验证通过返回null
     */
    private String validateComputedMetric(MetricConfigDTO.ComputedMetric metric) {
        try {
            if (metric instanceof MetricConfigDTO.ConditionalRatioMetric) {
                MetricConfigDTO.ConditionalRatioMetric ratio = (MetricConfigDTO.ConditionalRatioMetric) metric;
                sqlValidator.validateFieldName(ratio.getField());
                sqlValidator.validateCondition(ratio.getNumeratorCondition());
                // 分母条件允许为空（表示所有记录）
                sqlValidator.validateCondition(ratio.getDenominatorCondition(), true);
            } else if (metric instanceof MetricConfigDTO.SimpleRatioMetric) {
                MetricConfigDTO.SimpleRatioMetric ratio = (MetricConfigDTO.SimpleRatioMetric) metric;
                sqlValidator.validateFieldName(ratio.getNumeratorMetric());
                sqlValidator.validateFieldName(ratio.getDenominatorMetric());
            } else if (metric instanceof MetricConfigDTO.ConditionalSumMetric) {
                MetricConfigDTO.ConditionalSumMetric sum = (MetricConfigDTO.ConditionalSumMetric) metric;
                sqlValidator.validateFieldName(sum.getField());
                sqlValidator.validateCondition(sum.getCondition());
            } else if (metric instanceof MetricConfigDTO.CustomExpressionMetric) {
                MetricConfigDTO.CustomExpressionMetric custom = (MetricConfigDTO.CustomExpressionMetric) metric;
                sqlValidator.validateExpression(custom.getExpression());
            } else {
                return "不支持的计算指标类型: " + metric.getClass().getName();
            }
            return null;
        } catch (SecurityException e) {
            return e.getMessage();
        }
    }

    /**
     * 测试指标
     * 需求: 11.5, 12.1, 12.2
     *
     * @param datasetId 数据集ID
     * @param config 指标配置
     * @return 查询结果
     */
    @Override
    public QueryResult testMetric(Long datasetId, MetricConfigDTO config) {
        if (datasetId == null) {
            return QueryResult.failure("数据集ID不能为空");
        }

        if (config == null) {
            return QueryResult.failure("指标配置不能为空");
        }

        long startTime = System.currentTimeMillis();

        try {
            // 1. 验证指标配置
            String validationError = validateMetricConfig(config);
            if (validationError != null) {
                return QueryResult.failure("指标配置验证失败: " + validationError);
            }

            // 2. 获取数据集信息
            Dataset dataset = datasetMapper.selectDatasetById(datasetId);
            if (dataset == null) {
                return QueryResult.failure("数据集不存在");
            }

            // 3. 获取数据源类型
            SQLDialectAdapter.DatabaseType dbType = detectDatabaseType(dataset.getDatasourceId());

            // 4. 生成测试SQL
            String metricSql = metricConverter.convertMetricConfig(config, dbType);

            // 5. 构建完整的测试查询(添加LIMIT子句)
            String testQuery = buildTestQuery(dataset, metricSql);

            log.info("测试指标SQL: {}", testQuery);

            // 6. 执行测试查询
            QueryResult result = queryExecutor.executeQuery(datasetId, null, null);

            // 7. 添加执行时间
            long duration = System.currentTimeMillis() - startTime;
            log.info("测试指标成功: datasetId={}, duration={}ms", datasetId, duration);

            return result;

        } catch (Exception e) {
            log.error("测试指标失败: datasetId={}, error={}", datasetId, e.getMessage(), e);
            return QueryResult.failure("测试指标失败: " + e.getMessage());
        }
    }

    /**
     * 构建测试查询SQL
     *
     * @param dataset 数据集
     * @param metricSql 指标SQL
     * @return 完整的测试查询
     */
    private String buildTestQuery(Dataset dataset, String metricSql) throws JsonProcessingException {
        // 解析查询配置
        Map<String, Object> queryConfig = objectMapper.readValue(dataset.getQueryConfig(), Map.class);
        String sourceType = (String) queryConfig.get("sourceType");

        StringBuilder query = new StringBuilder("SELECT ");
        query.append(metricSql);
        query.append("\nFROM ");

        if ("table".equalsIgnoreCase(sourceType)) {
            String tableName = (String) queryConfig.get("tableName");
            query.append(tableName);
        } else if ("sql".equalsIgnoreCase(sourceType)) {
            String sql = (String) queryConfig.get("sql");
            query.append("(").append(sql).append(") AS subquery");
        }

        // 添加LIMIT子句限制返回行数
        query.append("\nLIMIT 10");

        return query.toString();
    }

    /**
     * 检测数据库类型
     *
     * @param datasourceId 数据源ID
     * @return 数据库类型
     */
    private SQLDialectAdapter.DatabaseType detectDatabaseType(Long datasourceId) {
        // TODO: 从数据源配置中获取数据库类型
        // 暂时返回MySQL作为默认值
        return SQLDialectAdapter.DatabaseType.MYSQL;
    }

    /**
     * 保存指标配置
     * 需求: 9.1, 9.3, 9.5
     *
     * @param datasetId 数据集ID
     * @param config 指标配置
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int saveMetricConfig(Long datasetId, MetricConfigDTO config) {
        if (datasetId == null) {
            throw new ServiceException("数据集ID不能为空");
        }

        if (config == null) {
            throw new ServiceException("指标配置不能为空");
        }

        try {
            // 1. 验证指标配置
            String validationError = validateMetricConfig(config);
            if (validationError != null) {
                throw new ServiceException("指标配置验证失败: " + validationError);
            }

            // 2. 检查数据集是否存在
            Dataset dataset = datasetMapper.selectDatasetById(datasetId);
            if (dataset == null) {
                throw new ServiceException("数据集不存在");
            }

            // 3. 序列化指标配置为JSON
            String configJson = objectMapper.writeValueAsString(config);

            // 4. 更新数据集的config字段
            dataset.setConfig(configJson);
            dataset.setUpdateBy(SecurityUtils.getUsername());

            int result = datasetMapper.updateDataset(dataset);

            log.info("保存指标配置成功: datasetId={}, baseMetrics={}, computedMetrics={}",
                    datasetId,
                    config.getBaseMetrics() != null ? config.getBaseMetrics().size() : 0,
                    config.getComputedMetrics() != null ? config.getComputedMetrics().size() : 0);

            return result;

        } catch (JsonProcessingException e) {
            log.error("序列化指标配置失败: datasetId={}, error={}", datasetId, e.getMessage(), e);
            throw new ServiceException("序列化指标配置失败: " + e.getMessage());
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("保存指标配置失败: datasetId={}, error={}", datasetId, e.getMessage(), e);
            throw new ServiceException("保存指标配置失败: " + e.getMessage());
        }
    }

    /**
     * 加载指标配置
     * 需求: 9.3, 9.5
     *
     * @param datasetId 数据集ID
     * @return 指标配置
     */
    @Override
    public MetricConfigDTO loadMetricConfig(Long datasetId) {
        if (datasetId == null) {
            throw new ServiceException("数据集ID不能为空");
        }

        try {
            // 1. 获取数据集
            Dataset dataset = datasetMapper.selectDatasetById(datasetId);
            if (dataset == null) {
                throw new ServiceException("数据集不存在");
            }

            // 2. 检查是否有配置
            String configJson = dataset.getConfig();
            if (StringUtils.isEmpty(configJson)) {
                // 返回空配置
                return new MetricConfigDTO();
            }

            // 3. 反序列化JSON配置
            MetricConfigDTO config = objectMapper.readValue(configJson, MetricConfigDTO.class);

            // 4. 验证反序列化的配置
            String validationError = validateMetricConfig(config);
            if (validationError != null) {
                log.warn("加载的指标配置验证失败: datasetId={}, error={}", datasetId, validationError);
                throw new ServiceException("加载的指标配置无效: " + validationError);
            }

            log.info("加载指标配置成功: datasetId={}, baseMetrics={}, computedMetrics={}",
                    datasetId,
                    config.getBaseMetrics() != null ? config.getBaseMetrics().size() : 0,
                    config.getComputedMetrics() != null ? config.getComputedMetrics().size() : 0);

            return config;

        } catch (JsonProcessingException e) {
            log.error("反序列化指标配置失败: datasetId={}, error={}", datasetId, e.getMessage(), e);
            throw new ServiceException("反序列化指标配置失败: " + e.getMessage());
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("加载指标配置失败: datasetId={}, error={}", datasetId, e.getMessage(), e);
            throw new ServiceException("加载指标配置失败: " + e.getMessage());
        }
    }
}
