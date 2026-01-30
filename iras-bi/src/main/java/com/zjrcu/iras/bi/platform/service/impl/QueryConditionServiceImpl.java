package com.zjrcu.iras.bi.platform.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.zjrcu.iras.bi.platform.domain.QueryCondition;
import com.zjrcu.iras.bi.platform.domain.ConditionMapping;
import com.zjrcu.iras.bi.platform.domain.dto.ChartQueryRequest;
import com.zjrcu.iras.bi.platform.domain.dto.Filter;
import com.zjrcu.iras.bi.platform.domain.dto.QueryResult;
import com.zjrcu.iras.bi.platform.domain.dto.QueryConditionConfigDTO;
import com.zjrcu.iras.bi.platform.domain.dto.ValidationResult;
import com.zjrcu.iras.bi.platform.domain.dto.ValidationError;
import com.zjrcu.iras.bi.platform.mapper.QueryConditionMapper;
import com.zjrcu.iras.bi.platform.mapper.ConditionMappingMapper;
import com.zjrcu.iras.bi.platform.service.IQueryConditionService;
import com.zjrcu.iras.bi.platform.service.IQueryExecutor;
import com.zjrcu.iras.common.exception.ServiceException;
import com.zjrcu.iras.common.utils.SecurityUtils;
import com.zjrcu.iras.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 查询条件Service业务层处理
 * 
 * @author zjrcu
 * @date 2026-01-20
 */
@Service
public class QueryConditionServiceImpl implements IQueryConditionService {
    private static final Logger log = LoggerFactory.getLogger(QueryConditionServiceImpl.class);

    @Autowired
    private QueryConditionMapper queryConditionMapper;

    @Autowired
    private ConditionMappingMapper conditionMappingMapper;

    @Autowired
    private IQueryExecutor queryExecutor;

    /**
     * 查询查询条件
     * 
     * @param id 查询条件主键
     * @return 查询条件
     */
    @Override
    public QueryCondition selectConditionById(Long id) {
        return queryConditionMapper.selectConditionById(id);
    }

    /**
     * 查询查询条件列表
     * 
     * @param condition 查询条件
     * @return 查询条件
     */
    @Override
    public List<QueryCondition> selectConditionList(QueryCondition condition) {
        return queryConditionMapper.selectConditionList(condition);
    }

    /**
     * 新增查询条件
     * 
     * @param condition 查询条件
     * @return 结果
     */
    @Override
    public int insertCondition(QueryCondition condition) {
        // 验证必填字段
        if (condition.getDashboardId() == null) {
            throw new ServiceException("仪表板ID不能为空");
        }
        if (StringUtils.isEmpty(condition.getConditionName())) {
            throw new ServiceException("条件名称不能为空");
        }
        if (StringUtils.isEmpty(condition.getConditionType())) {
            throw new ServiceException("条件类型不能为空");
        }

        // 验证JSON配置
        validateJsonConfig(condition.getConfig(), "条件配置");

        // 设置默认值
        if (condition.getDisplayOrder() == null) {
            condition.setDisplayOrder(0);
        }
        if (StringUtils.isEmpty(condition.getIsRequired())) {
            condition.setIsRequired("0");
        }
        if (StringUtils.isEmpty(condition.getIsVisible())) {
            condition.setIsVisible("1");
        }

        return queryConditionMapper.insertCondition(condition);
    }

    /**
     * 修改查询条件
     * 
     * @param condition 查询条件
     * @return 结果
     */
    @Override
    public int updateCondition(QueryCondition condition) {
        // 验证条件存在
        QueryCondition existing = queryConditionMapper.selectConditionById(condition.getId());
        if (existing == null) {
            throw new ServiceException("查询条件不存在");
        }

        // 验证JSON配置
        validateJsonConfig(condition.getConfig(), "条件配置");

        return queryConditionMapper.updateCondition(condition);
    }

    /**
     * 批量删除查询条件
     * 
     * @param ids 需要删除的查询条件主键
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteConditionByIds(Long[] ids) {
        // 先删除关联的条件映射
        for (Long id : ids) {
            conditionMappingMapper.deleteMappingByConditionId(id);
        }
        
        // 再删除条件本身
        return queryConditionMapper.deleteConditionByIds(ids);
    }

    /**
     * 删除查询条件信息
     * 
     * @param id 查询条件主键
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteConditionById(Long id) {
        // 先删除关联的条件映射
        conditionMappingMapper.deleteMappingByConditionId(id);
        
        // 再删除条件本身
        return queryConditionMapper.deleteConditionById(id);
    }

    /**
     * 获取仪表板的条件映射
     * 
     * @param dashboardId 仪表板ID
     * @return 条件映射集合
     */
    @Override
    public List<ConditionMapping> getMappings(Long dashboardId) {
        return conditionMappingMapper.selectMappingByDashboardId(dashboardId);
    }

    /**
     * 保存条件映射
     * 
     * @param dashboardId 仪表板ID
     * @param mappings 条件映射列表
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int saveMappings(Long dashboardId, List<ConditionMapping> mappings) {
        // 删除旧映射
        conditionMappingMapper.deleteMappingByDashboardId(dashboardId);

        // 插入新映射
        int count = 0;
        if (mappings != null) {
            for (ConditionMapping mapping : mappings) {
                // 验证必填字段
                if (mapping.getConditionId() == null) {
                    throw new ServiceException("条件ID不能为空");
                }
                if (mapping.getComponentId() == null) {
                    throw new ServiceException("组件ID不能为空");
                }
                if (StringUtils.isEmpty(mapping.getFieldName())) {
                    throw new ServiceException("映射字段名不能为空");
                }

                // 设置默认值
                if (StringUtils.isEmpty(mapping.getMappingType())) {
                    mapping.setMappingType("auto");
                }

                count += conditionMappingMapper.insertMapping(mapping);
            }
        }

        return count;
    }

    /**
     * 获取级联选项
     * 
     * @param conditionId 条件ID
     * @param parentValues 父条件值
     * @return 选项列表
     */
    @Override
    public List<Map<String, Object>> getCascadeOptions(Long conditionId, Map<String, Object> parentValues) {
        QueryCondition condition = queryConditionMapper.selectConditionById(conditionId);
        if (condition == null) {
            throw new ServiceException("查询条件不存在");
        }

        // 检查是否有父条件
        if (condition.getParentConditionId() == null) {
            throw new ServiceException("该条件不是级联条件");
        }

        // 检查父条件是否有值
        Object parentValue = parentValues.get(condition.getParentConditionId().toString());
        if (parentValue == null) {
            return new ArrayList<>();
        }

        // 解析条件配置
        JSONObject config;
        try {
            config = JSONObject.parseObject(condition.getConfig());
        } catch (Exception e) {
            throw new ServiceException("条件配置格式错误: " + e.getMessage());
        }

        // 获取数据源配置
        JSONObject dataSource = config.getJSONObject("dataSource");
        if (dataSource == null) {
            throw new ServiceException("缺少数据源配置");
        }

        String url = dataSource.getString("url");
        if (StringUtils.isEmpty(url)) {
            throw new ServiceException("数据源URL不能为空");
        }

        // 获取参数配置
        JSONObject params = dataSource.getJSONObject("params");
        if (params == null) {
            params = new JSONObject();
        }

        // 替换参数中的占位符
        JSONObject resolvedParams = new JSONObject();
        for (String key : params.keySet()) {
            String value = params.getString(key);
            if (value != null && value.startsWith("${") && value.endsWith("}")) {
                // 提取条件ID: ${conditionId} -> conditionId
                String refConditionId = value.substring(2, value.length() - 1);
                Object refValue = parentValues.get(refConditionId);
                if (refValue != null) {
                    resolvedParams.put(key, refValue);
                }
            } else {
                resolvedParams.put(key, value);
            }
        }

        // TODO: 这里需要实际调用外部API获取选项
        // 由于我们不知道具体的API实现,这里返回一个示例结构
        // 实际使用时,应该使用RestTemplate或其他HTTP客户端调用API
        
        // 示例返回格式
        List<Map<String, Object>> options = new ArrayList<>();
        
        // 注意: 实际实现应该调用真实的API
        // 例如:
        // RestTemplate restTemplate = new RestTemplate();
        // String apiUrl = url + "?" + buildQueryString(resolvedParams);
        // ResponseEntity<List> response = restTemplate.getForEntity(apiUrl, List.class);
        // List<Map<String, Object>> data = response.getBody();
        // 
        // String labelField = dataSource.getString("labelField");
        // String valueField = dataSource.getString("valueField");
        // 
        // for (Map<String, Object> item : data) {
        //     Map<String, Object> option = new HashMap<>();
        //     option.put("label", item.get(labelField));
        //     option.put("value", item.get(valueField));
        //     options.add(option);
        // }
        
        return options;
    }

    /**
     * 验证JSON配置格式
     * 
     * @param jsonConfig JSON配置字符串
     * @param configName 配置名称
     */
    private void validateJsonConfig(String jsonConfig, String configName) {
        if (StringUtils.isNotEmpty(jsonConfig)) {
            try {
                JSONObject.parseObject(jsonConfig);
            } catch (Exception e) {
                throw new ServiceException(configName + "格式错误: " + e.getMessage());
            }
        }
    }

    /**
     * 执行图表查询
     * 
     * @param queryRequest 查询请求
     * @return 查询结果
     */
    @Override
    public QueryResult executeChartQuery(ChartQueryRequest queryRequest) {
        if (queryRequest == null) {
            return QueryResult.failure("查询请求不能为空");
        }

        if (queryRequest.getDatasetId() == null) {
            return QueryResult.failure("数据集ID不能为空");
        }

        try {
            // 1. 构建过滤条件列表
            List<Filter> filters = new ArrayList<>();
            
            // 添加图表级过滤器
            if (queryRequest.getFilters() != null) {
                filters.addAll(queryRequest.getFilters());
            }
            
            // 添加查询参数作为过滤条件
            if (queryRequest.getParams() != null && !queryRequest.getParams().isEmpty()) {
                for (Map.Entry<String, Object> entry : queryRequest.getParams().entrySet()) {
                    if (entry.getValue() != null) {
                        Filter filter = new Filter();
                        filter.setField(entry.getKey());
                        filter.setOperator("eq");
                        filter.setValue(entry.getValue());
                        filters.add(filter);
                    }
                }
            }

            // 2. 执行查询
            // 注意: 这里使用基础的executeQuery,它会返回原始数据
            // 前端需要根据维度和指标配置来处理数据
            QueryResult result = queryExecutor.executeQuery(
                queryRequest.getDatasetId(), 
                filters, 
                SecurityUtils.getLoginUser().getUser()
            );

            // 3. 应用结果限制
            if (result.isSuccess() && queryRequest.getLimit() != null && queryRequest.getLimit() > 0) {
                // 修复: 使用long类型避免类型转换损失
                long limit = Math.min(queryRequest.getLimit().longValue(), result.getTotalRows());
                if (limit < result.getTotalRows()) {
                    // 截取数据
                    List<Map<String, Object>> limitedData = result.getData().subList(0, (int)limit);
                    result.setData(limitedData);
                    result.setTotalRows(limit);
                }
            }

            // 注意: QueryResult没有metadata字段，维度和指标信息已经在请求中
            // 前端可以直接使用请求中的dimensions和metrics配置来处理数据

            log.info("图表查询成功: datasetId={}, rows={}, duration={}ms", 
                queryRequest.getDatasetId(), result.getTotalRows(), result.getExecutionTime());

            return result;

        } catch (Exception e) {
            log.error("图表查询失败: datasetId={}, error={}", queryRequest.getDatasetId(), e.getMessage(), e);
            return QueryResult.failure("查询失败: " + e.getMessage());
        }
    }

    /**
     * 验证查询条件配置
     * 
     * @param config 查询条件配置
     * @return 验证结果
     */
    @Override
    public ValidationResult validateConditionConfig(QueryConditionConfigDTO config) {
        ValidationResult result = new ValidationResult();

        if (config == null) {
            result.addError(null, "config", "配置不能为空");
            return result;
        }

        if (config.getComponentId() == null) {
            result.addError(null, "componentId", "组件ID不能为空");
        }

        if (config.getDashboardId() == null) {
            result.addError(null, "dashboardId", "仪表板ID不能为空");
        }

        if (config.getConditions() == null || config.getConditions().isEmpty()) {
            result.addError(null, "conditions", "条件列表不能为空");
            return result;
        }

        // 验证条件名称唯一性
        Set<String> conditionNames = new HashSet<>();
        for (QueryConditionConfigDTO.QueryConditionDTO condition : config.getConditions()) {
            String conditionId = condition.getId() != null ? condition.getId().toString() : "new";

            // 验证条件名称非空
            if (StringUtils.isEmpty(condition.getConditionName())) {
                result.addError(conditionId, "conditionName", "条件名称不能为空");
                continue;
            }

            // 验证条件名称唯一性
            if (conditionNames.contains(condition.getConditionName())) {
                result.addError(conditionId, "conditionName", 
                    "条件名称重复: " + condition.getConditionName());
            } else {
                conditionNames.add(condition.getConditionName());
            }

            // 验证字段映射存在性
            if (condition.getMappings() == null || condition.getMappings().isEmpty()) {
                result.addError(conditionId, "mappings", "条件必须至少有一个字段映射");
            } else {
                // 验证每个映射的必填字段
                for (int i = 0; i < condition.getMappings().size(); i++) {
                    QueryConditionConfigDTO.ConditionMappingDTO mapping = condition.getMappings().get(i);
                    
                    if (StringUtils.isEmpty(mapping.getTableName())) {
                        result.addError(conditionId, "mappings[" + i + "].tableName", "表名不能为空");
                    }
                    
                    if (StringUtils.isEmpty(mapping.getFieldName())) {
                        result.addError(conditionId, "mappings[" + i + "].fieldName", "字段名不能为空");
                    }
                }
            }

            // 验证配置对象
            if (condition.getConfig() != null) {
                QueryConditionConfigDTO.ConditionConfigDTO configDto = condition.getConfig();

                // 验证时间类型条件的时间粒度
                if (isTimeBasedType(condition.getConditionType()) || 
                    isTimeBasedType(configDto.getDisplayType())) {
                    
                    if (StringUtils.isEmpty(configDto.getTimeGranularity())) {
                        result.addError(conditionId, "config.timeGranularity", 
                            "时间类型条件必须指定时间粒度");
                    } else {
                        // 验证时间粒度值是否有效
                        if (!isValidTimeGranularity(configDto.getTimeGranularity())) {
                            result.addError(conditionId, "config.timeGranularity", 
                                "无效的时间粒度: " + configDto.getTimeGranularity());
                        }
                    }
                }

                // 验证显示类型兼容性
                if (StringUtils.isNotEmpty(configDto.getDisplayType())) {
                    // 获取字段类型进行兼容性验证
                    // 注意: 这里简化处理,实际应该查询数据库获取字段类型
                    // 如果需要严格验证,可以在这里添加数据库查询逻辑
                    if (!isValidDisplayType(configDto.getDisplayType())) {
                        result.addError(conditionId, "config.displayType", 
                            "无效的显示类型: " + configDto.getDisplayType());
                    }
                }

                // 验证默认值格式
                if (StringUtils.isNotEmpty(condition.getDefaultValue())) {
                    String displayType = StringUtils.isNotEmpty(configDto.getDisplayType()) 
                        ? configDto.getDisplayType() 
                        : condition.getConditionType();
                    
                    if (!validateDefaultValueFormat(condition.getDefaultValue(), displayType)) {
                        result.addError(conditionId, "defaultValue", 
                            "默认值格式与显示类型不匹配");
                    }
                }
            }
        }

        return result;
    }

    /**
     * 判断是否为时间类型
     * 
     * @param type 类型
     * @return true如果是时间类型
     */
    private boolean isTimeBasedType(String type) {
        if (StringUtils.isEmpty(type)) {
            return false;
        }
        return "time".equalsIgnoreCase(type) || 
               "date".equalsIgnoreCase(type) || 
               "datetime".equalsIgnoreCase(type) ||
               "daterange".equalsIgnoreCase(type);
    }

    /**
     * 验证时间粒度是否有效
     * 
     * @param granularity 时间粒度
     * @return true如果有效
     */
    private boolean isValidTimeGranularity(String granularity) {
        if (StringUtils.isEmpty(granularity)) {
            return false;
        }
        Set<String> validGranularities = new HashSet<>();
        validGranularities.add("year");
        validGranularities.add("month");
        validGranularities.add("day");
        validGranularities.add("hour");
        validGranularities.add("minute");
        validGranularities.add("second");
        return validGranularities.contains(granularity.toLowerCase());
    }

    /**
     * 验证显示类型是否有效
     * 
     * @param displayType 显示类型
     * @return true如果有效
     */
    private boolean isValidDisplayType(String displayType) {
        if (StringUtils.isEmpty(displayType)) {
            return false;
        }
        Set<String> validTypes = new HashSet<>();
        validTypes.add("time");
        validTypes.add("date");
        validTypes.add("datetime");
        validTypes.add("daterange");
        validTypes.add("dropdown");
        validTypes.add("text");
        validTypes.add("number");
        validTypes.add("range");
        return validTypes.contains(displayType.toLowerCase());
    }

    /**
     * 验证默认值格式
     * 
     * @param defaultValue 默认值
     * @param displayType 显示类型
     * @return true如果格式有效
     */
    private boolean validateDefaultValueFormat(String defaultValue, String displayType) {
        if (StringUtils.isEmpty(defaultValue) || StringUtils.isEmpty(displayType)) {
            return true; // 空值认为有效
        }

        try {
            switch (displayType.toLowerCase()) {
                case "number":
                    // 验证是否为数字
                    Double.parseDouble(defaultValue);
                    return true;
                case "time":
                case "date":
                case "datetime":
                    // 简单验证日期格式 (实际应该使用DateTimeFormatter)
                    // 这里只检查基本格式,不做严格验证
                    return defaultValue.matches("\\d{4}-\\d{2}-\\d{2}.*") || 
                           defaultValue.matches("\\d{2}:\\d{2}.*");
                case "daterange":
                    // 验证日期范围格式 (例如: "2024-01-01,2024-12-31")
                    return defaultValue.contains(",") || defaultValue.contains("~");
                case "dropdown":
                case "text":
                case "range":
                default:
                    // 文本类型和其他类型不做格式验证
                    return true;
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 根据组件ID查询查询条件列表
     * 
     * @param componentId 组件ID
     * @return 查询条件集合
     */
    @Override
    public List<QueryCondition> selectConditionsByComponentId(Long componentId) {
        if (componentId == null) {
            throw new ServiceException("组件ID不能为空");
        }
        return queryConditionMapper.selectConditionsByComponentId(componentId);
    }

    /**
     * 保存查询条件配置
     * 
     * @param config 查询条件配置
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int saveConditionConfig(QueryConditionConfigDTO config) {
        if (config == null) {
            throw new ServiceException("配置不能为空");
        }

        if (config.getComponentId() == null) {
            throw new ServiceException("组件ID不能为空");
        }

        if (config.getDashboardId() == null) {
            throw new ServiceException("仪表板ID不能为空");
        }

        // 验证配置
        ValidationResult validationResult = validateConditionConfig(config);
        if (!validationResult.isValid()) {
            StringBuilder errorMsg = new StringBuilder("配置验证失败: ");
            for (ValidationError error : validationResult.getErrors()) {
                errorMsg.append(error.getMessage()).append("; ");
            }
            throw new ServiceException(errorMsg.toString());
        }

        int totalCount = 0;

        // 删除组件的旧条件和映射
        conditionMappingMapper.deleteMappingByComponentId(config.getComponentId());
        queryConditionMapper.deleteConditionByComponentId(config.getComponentId());

        // 保存新条件和映射
        if (config.getConditions() != null && !config.getConditions().isEmpty()) {
            for (QueryConditionConfigDTO.QueryConditionDTO conditionDto : config.getConditions()) {
                // 创建条件实体
                QueryCondition condition = new QueryCondition();
                condition.setDashboardId(config.getDashboardId());
                condition.setComponentId(config.getComponentId());
                condition.setConditionName(conditionDto.getConditionName());
                condition.setConditionType(conditionDto.getConditionType());
                condition.setDisplayOrder(conditionDto.getDisplayOrder());
                condition.setIsRequired(conditionDto.getIsRequired() != null ? conditionDto.getIsRequired() : "0");
                condition.setIsVisible(conditionDto.getIsVisible() != null ? conditionDto.getIsVisible() : "1");
                condition.setDefaultValue(conditionDto.getDefaultValue());

                // 将配置对象转换为JSON字符串
                if (conditionDto.getConfig() != null) {
                    try {
                        condition.setConfig(JSONObject.toJSONString(conditionDto.getConfig()));
                    } catch (Exception e) {
                        throw new ServiceException("条件配置序列化失败: " + e.getMessage());
                    }
                }

                // 插入条件
                int result = queryConditionMapper.insertCondition(condition);
                if (result > 0) {
                    totalCount++;

                    // 保存字段映射
                    if (conditionDto.getMappings() != null && !conditionDto.getMappings().isEmpty()) {
                        List<ConditionMapping> mappings = new ArrayList<>();
                        for (QueryConditionConfigDTO.ConditionMappingDTO mappingDto : conditionDto.getMappings()) {
                            ConditionMapping mapping = new ConditionMapping();
                            mapping.setConditionId(condition.getId());
                            mapping.setComponentId(config.getComponentId());
                            mapping.setTableName(mappingDto.getTableName());
                            mapping.setFieldName(mappingDto.getFieldName());
                            mapping.setMappingType(mappingDto.getMappingType() != null ? mappingDto.getMappingType() : "auto");
                            mappings.add(mapping);
                        }

                        // 批量插入映射
                        if (!mappings.isEmpty()) {
                            conditionMappingMapper.batchInsertMappings(mappings);
                        }
                    }
                }
            }
        }

        log.info("保存查询条件配置成功: componentId={}, count={}", config.getComponentId(), totalCount);
        return totalCount;
    }

    /**
     * 批量更新查询条件显示顺序
     * 
     * @param orders 条件排序列表
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int reorderConditions(List<com.zjrcu.iras.bi.platform.domain.dto.ConditionOrderDTO> orders) {
        if (orders == null || orders.isEmpty()) {
            throw new ServiceException("排序列表不能为空");
        }

        int count = 0;
        for (com.zjrcu.iras.bi.platform.domain.dto.ConditionOrderDTO order : orders) {
            if (order.getConditionId() == null) {
                throw new ServiceException("条件ID不能为空");
            }
            if (order.getDisplayOrder() == null) {
                throw new ServiceException("显示顺序不能为空");
            }

            count += queryConditionMapper.updateConditionDisplayOrder(
                order.getConditionId(), 
                order.getDisplayOrder()
            );
        }

        log.info("批量更新查询条件显示顺序成功: count={}", count);
        return count;
    }

    /**
     * 复制查询条件到新组件
     * 
     * @param sourceComponentId 源组件ID
     * @param targetComponentId 目标组件ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int copyConditionsToComponent(Long sourceComponentId, Long targetComponentId) {
        if (sourceComponentId == null) {
            throw new ServiceException("源组件ID不能为空");
        }
        if (targetComponentId == null) {
            throw new ServiceException("目标组件ID不能为空");
        }

        // 查询源组件的所有条件
        List<QueryCondition> sourceConditions = queryConditionMapper.selectConditionsByComponentId(sourceComponentId);
        if (sourceConditions == null || sourceConditions.isEmpty()) {
            log.info("源组件没有查询条件,无需复制: sourceComponentId={}", sourceComponentId);
            return 0;
        }

        int totalCount = 0;

        // 复制每个条件
        for (QueryCondition sourceCondition : sourceConditions) {
            // 创建新条件
            QueryCondition newCondition = new QueryCondition();
            newCondition.setDashboardId(sourceCondition.getDashboardId());
            newCondition.setComponentId(targetComponentId); // 使用目标组件ID
            newCondition.setConditionName(sourceCondition.getConditionName());
            newCondition.setConditionType(sourceCondition.getConditionType());
            newCondition.setDisplayOrder(sourceCondition.getDisplayOrder());
            newCondition.setIsRequired(sourceCondition.getIsRequired());
            newCondition.setIsVisible(sourceCondition.getIsVisible());
            newCondition.setDefaultValue(sourceCondition.getDefaultValue());
            newCondition.setConfig(sourceCondition.getConfig());
            newCondition.setParentConditionId(sourceCondition.getParentConditionId());

            // 插入新条件
            int result = queryConditionMapper.insertCondition(newCondition);
            if (result > 0) {
                totalCount++;

                // 查询源条件的映射
                List<ConditionMapping> sourceMappings = conditionMappingMapper.selectMappingByConditionId(sourceCondition.getId());
                if (sourceMappings != null && !sourceMappings.isEmpty()) {
                    // 复制映射
                    List<ConditionMapping> newMappings = new ArrayList<>();
                    for (ConditionMapping sourceMapping : sourceMappings) {
                        ConditionMapping newMapping = new ConditionMapping();
                        newMapping.setConditionId(newCondition.getId()); // 使用新条件ID
                        newMapping.setComponentId(targetComponentId); // 使用目标组件ID
                        newMapping.setTableName(sourceMapping.getTableName());
                        newMapping.setFieldName(sourceMapping.getFieldName());
                        newMapping.setMappingType(sourceMapping.getMappingType());
                        newMappings.add(newMapping);
                    }

                    // 批量插入映射
                    if (!newMappings.isEmpty()) {
                        conditionMappingMapper.batchInsertMappings(newMappings);
                    }
                }
            }
        }

        log.info("复制查询条件成功: sourceComponentId={}, targetComponentId={}, count={}", 
            sourceComponentId, targetComponentId, totalCount);
        return totalCount;
    }
}
