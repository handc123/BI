package com.zjrcu.iras.bi.platform.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.zjrcu.iras.bi.platform.domain.Dataset;
import com.zjrcu.iras.bi.platform.domain.DashboardComponent;
import com.zjrcu.iras.bi.platform.domain.dto.CalculatedFieldDTO;
import com.zjrcu.iras.bi.platform.domain.dto.CalculatedFieldValidationResponse;
import com.zjrcu.iras.bi.platform.domain.dto.ComponentPosition;
import com.zjrcu.iras.bi.platform.domain.dto.QueryResult;
import com.zjrcu.iras.bi.platform.engine.CalculatedFieldConverter;
import com.zjrcu.iras.bi.platform.engine.DataSourceManager;
import com.zjrcu.iras.bi.platform.engine.ExpressionParser;
import com.zjrcu.iras.bi.platform.engine.QueryExecutor;
import com.zjrcu.iras.bi.platform.mapper.ComponentMapper;
import com.zjrcu.iras.bi.platform.security.SQLValidator;
import com.zjrcu.iras.bi.platform.service.IComponentService;
import com.zjrcu.iras.bi.platform.service.IDatasetService;
import com.zjrcu.iras.common.exception.ServiceException;
import com.zjrcu.iras.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 仪表板组件Service业务层处理
 * 
 * @author zjrcu
 * @date 2026-01-20
 */
@Service
public class ComponentServiceImpl implements IComponentService {
    private static final Logger log = LoggerFactory.getLogger(ComponentServiceImpl.class);

    @Autowired
    private ComponentMapper componentMapper;

    @Autowired
    private IDatasetService datasetService;

    @Autowired
    private CalculatedFieldConverter calculatedFieldConverter;

    @Autowired
    private SQLValidator sqlValidator;

    @Autowired
    private ExpressionParser expressionParser;

    @Autowired
    private QueryExecutor queryExecutor;

    @Autowired
    private DataSourceManager dataSourceManager;

    /**
     * 查询组件
     * 
     * @param id 组件主键
     * @return 组件
     */
    @Override
    public DashboardComponent selectComponentById(Long id) {
        return componentMapper.selectComponentById(id);
    }

    /**
     * 查询组件列表
     * 
     * @param component 组件
     * @return 组件
     */
    @Override
    public List<DashboardComponent> selectComponentList(DashboardComponent component) {
        return componentMapper.selectComponentList(component);
    }

    /**
     * 新增组件
     * 
     * @param component 组件
     * @return 结果
     */
    @Override
    public int insertComponent(DashboardComponent component) {
        // 验证必填字段
        if (component.getDashboardId() == null) {
            throw new ServiceException("仪表板ID不能为空");
        }
        if (StringUtils.isEmpty(component.getComponentType())) {
            throw new ServiceException("组件类型不能为空");
        }
        if (component.getPositionX() == null || component.getPositionY() == null) {
            throw new ServiceException("组件位置不能为空");
        }
        if (component.getWidth() == null || component.getHeight() == null) {
            throw new ServiceException("组件尺寸不能为空");
        }

        // 验证JSON配置
        validateJsonConfig(component.getDataConfig(), "数据配置");
        validateJsonConfig(component.getStyleConfig(), "样式配置");
        validateJsonConfig(component.getAdvancedConfig(), "高级配置");

        // 验证计算字段配置(如果存在)
        if (StringUtils.isNotEmpty(component.getDataConfig())) {
            validateCalculatedFieldsInConfig(component.getDataConfig());
        }

        // 设置默认值
        if (component.getZIndex() == null) {
            component.setZIndex(0);
        }

        return componentMapper.insertComponent(component);
    }

    /**
     * 修改组件
     * 
     * @param component 组件
     * @return 结果
     */
    @Override
    public int updateComponent(DashboardComponent component) {
        // 验证组件存在
        DashboardComponent existing = componentMapper.selectComponentById(component.getId());
        if (existing == null) {
            throw new ServiceException("组件不存在");
        }

        // 验证JSON配置
        validateJsonConfig(component.getDataConfig(), "数据配置");
        validateJsonConfig(component.getStyleConfig(), "样式配置");
        validateJsonConfig(component.getAdvancedConfig(), "高级配置");

        // 验证计算字段配置(如果存在)
        if (StringUtils.isNotEmpty(component.getDataConfig())) {
            validateCalculatedFieldsInConfig(component.getDataConfig());
        }

        return componentMapper.updateComponent(component);
    }

    /**
     * 批量删除组件
     * 
     * @param ids 需要删除的组件主键
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteComponentByIds(Long[] ids) {
        // 删除关联的条件映射会通过数据库外键级联删除
        return componentMapper.deleteComponentByIds(ids);
    }

    /**
     * 删除组件信息
     * 
     * @param id 组件主键
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteComponentById(Long id) {
        // 删除关联的条件映射会通过数据库外键级联删除
        return componentMapper.deleteComponentById(id);
    }

    /**
     * 批量更新组件位置
     * 
     * @param positions 组件位置列表
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdatePosition(List<ComponentPosition> positions) {
        int count = 0;
        for (ComponentPosition position : positions) {
            count += componentMapper.updateComponentPosition(position);
        }
        return count;
    }

    /**
     * 复制组件
     * 
     * @param id 组件主键
     * @return 新组件
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public DashboardComponent copyComponent(Long id) {
        DashboardComponent original = componentMapper.selectComponentById(id);
        if (original == null) {
            throw new ServiceException("组件不存在");
        }

        // 创建副本
        DashboardComponent copy = new DashboardComponent();
        copy.setDashboardId(original.getDashboardId());
        copy.setComponentType(original.getComponentType());
        copy.setComponentName(original.getComponentName() + " - 副本");
        // 位置偏移
        copy.setPositionX(original.getPositionX() + 20);
        copy.setPositionY(original.getPositionY() + 20);
        copy.setWidth(original.getWidth());
        copy.setHeight(original.getHeight());
        copy.setZIndex(original.getZIndex() + 1);
        copy.setDataConfig(original.getDataConfig());
        copy.setStyleConfig(original.getStyleConfig());
        copy.setAdvancedConfig(original.getAdvancedConfig());

        componentMapper.insertComponent(copy);

        return copy;
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
     * 验证数据配置中的计算字段
     * 
     * @param dataConfig 数据配置JSON字符串
     */
    private void validateCalculatedFieldsInConfig(String dataConfig) {
        try {
            JSONObject config = JSONObject.parseObject(dataConfig);
            if (config.containsKey("calculatedFields")) {
                Object calculatedFieldsObj = config.get("calculatedFields");
                if (calculatedFieldsObj instanceof java.util.List) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> calculatedFields = (List<Map<String, Object>>) calculatedFieldsObj;
                    
                    for (Map<String, Object> fieldMap : calculatedFields) {
                        // 验证必填字段
                        if (!fieldMap.containsKey("name") || StringUtils.isEmpty((String) fieldMap.get("name"))) {
                            throw new ServiceException("计算字段名称不能为空");
                        }
                        if (!fieldMap.containsKey("expression") || StringUtils.isEmpty((String) fieldMap.get("expression"))) {
                            throw new ServiceException("计算字段表达式不能为空");
                        }
                        if (!fieldMap.containsKey("fieldType") || StringUtils.isEmpty((String) fieldMap.get("fieldType"))) {
                            throw new ServiceException("计算字段类型不能为空");
                        }
                        
                        // 验证字段类型
                        String fieldType = (String) fieldMap.get("fieldType");
                        if (!"dimension".equals(fieldType) && !"metric".equals(fieldType)) {
                            throw new ServiceException("计算字段类型必须是dimension或metric");
                        }
                        
                        // 验证聚合方式(如果是指标类型)
                        if ("metric".equals(fieldType) && fieldMap.containsKey("aggregation")) {
                            String aggregation = (String) fieldMap.get("aggregation");
                            if (!Arrays.asList("AUTO", "SUM", "AVG", "MAX", "MIN", "COUNT").contains(aggregation)) {
                                throw new ServiceException("无效的聚合方式: " + aggregation);
                            }
                        }
                    }
                }
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.warn("验证计算字段配置失败: {}", e.getMessage());
            // 不阻止保存,只记录警告
        }
    }

    /**
     * 验证计算字段
     * 
     * @param datasetId 数据集ID
     * @param field 计算字段配置
     * @return 验证响应
     */
    @Override
    public CalculatedFieldValidationResponse validateCalculatedField(Long datasetId, CalculatedFieldDTO field) {
        try {
            // 1. 获取数据集信息
            Dataset dataset = datasetService.selectDatasetById(datasetId);
            if (dataset == null) {
                return new CalculatedFieldValidationResponse(false, "数据集不存在", null);
            }

            // 2. 获取数据集字段
            List<Map<String, Object>> datasetFieldMaps = datasetService.getDatasetFields(datasetId);
            if (datasetFieldMaps == null || datasetFieldMaps.isEmpty()) {
                return new CalculatedFieldValidationResponse(false, "数据集字段为空", null);
            }

            // 转换为DatasetFieldVO列表
            List<com.zjrcu.iras.bi.platform.domain.dto.DatasetFieldVO> datasetFields = convertToDatasetFieldVOs(datasetFieldMaps);

            // 3. 验证表达式语法
            String expression = field.getExpression();
            if (StringUtils.isEmpty(expression)) {
                return new CalculatedFieldValidationResponse(false, "表达式不能为空", null);
            }

            // 4. SQL注入验证
            try {
                sqlValidator.validateExpression(expression);
            } catch (Exception e) {
                return new CalculatedFieldValidationResponse(false, "表达式包含不安全的SQL模式: " + e.getMessage(), null);
            }

            // 5. 验证字段引用
            Set<String> referencedFields = expressionParser.extractFieldReferences(expression);
            Set<String> availableFields = datasetFields.stream()
                    .map(f -> f.getFieldName())
                    .collect(Collectors.toSet());

            for (String refField : referencedFields) {
                if (!availableFields.contains(refField)) {
                    return new CalculatedFieldValidationResponse(false, "引用的字段不存在: " + refField, null);
                }
            }

            // 6. 验证函数调用
            try {
                expressionParser.validateFunctionSyntax(expression);
            } catch (Exception e) {
                return new CalculatedFieldValidationResponse(false, "函数语法错误: " + e.getMessage(), null);
            }

            // 7. 验证括号匹配
            if (!isParenthesesBalanced(expression)) {
                return new CalculatedFieldValidationResponse(false, "括号不匹配", null);
            }

            // 8. 生成SQL预览
            String dbType = dataset.getDataSource() != null ? dataset.getDataSource().getType() : "MySQL";
            String sqlPreview = calculatedFieldConverter.convertToSQL(field, datasetFields, dbType);

            return new CalculatedFieldValidationResponse(true, "验证通过", sqlPreview);

        } catch (Exception e) {
            log.error("验证计算字段失败", e);
            return new CalculatedFieldValidationResponse(false, "验证失败: " + e.getMessage(), null);
        }
    }

    /**
     * 测试计算字段
     * 
     * @param datasetId 数据集ID
     * @param field 计算字段配置
     * @return 查询结果
     */
    @Override
    public QueryResult testCalculatedField(Long datasetId, CalculatedFieldDTO field) {
        Connection connection = null;
        java.sql.Statement statement = null;
        java.sql.ResultSet resultSet = null;

        try {
            // 1. 先验证计算字段
            CalculatedFieldValidationResponse validation = validateCalculatedField(datasetId, field);
            if (!validation.isValid()) {
                throw new ServiceException("计算字段验证失败: " + validation.getMessage());
            }

            // 2. 获取数据集信息
            Dataset dataset = datasetService.selectDatasetById(datasetId);
            if (dataset == null) {
                throw new ServiceException("数据集不存在");
            }

            // 3. 获取数据集字段
            List<Map<String, Object>> datasetFieldMaps = datasetService.getDatasetFields(datasetId);

            // 转换为DatasetFieldVO列表
            List<com.zjrcu.iras.bi.platform.domain.dto.DatasetFieldVO> datasetFields = convertToDatasetFieldVOs(datasetFieldMaps);

            // 4. 构建测试SQL
            String dbType = dataset.getDataSource() != null ? dataset.getDataSource().getType() : "MySQL";
            String calculatedFieldSQL = calculatedFieldConverter.convertToSQL(field, datasetFields, dbType);

            // 5. 从数据集查询配置中获取基础SQL
            Map<String, Object> queryConfig = dataset.getQueryConfigMap();
            String baseSQL = (String) queryConfig.get("sql");
            if (StringUtils.isEmpty(baseSQL)) {
                throw new ServiceException("数据集查询配置为空");
            }

            // 6. 构建完整的测试SQL (添加计算字段并限制10行)
            String testSQL = String.format("SELECT *, %s FROM (%s) AS base_query LIMIT 10", 
                    calculatedFieldSQL, baseSQL);

            log.debug("测试计算字段SQL: {}", testSQL);

            // 7. 执行查询
            long startTime = System.currentTimeMillis();
            connection = dataSourceManager.getConnection(dataset.getDatasourceId());
            statement = connection.createStatement();
            statement.setQueryTimeout(30);
            resultSet = statement.executeQuery(testSQL);

            // 8. 处理结果
            QueryResult result = new QueryResult();
            result.setSuccess(true);

            // 获取列元数据
            java.sql.ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            List<QueryResult.FieldMetadata> fields = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnLabel(i);
                String columnType = metaData.getColumnTypeName(i);
                // 判断是否为计算字段
                boolean isCalculated = columnName.equals(field.getName());
                fields.add(new QueryResult.FieldMetadata(columnName, columnName, columnType, isCalculated));
            }
            result.setFields(fields);

            // 获取数据行
            List<Map<String, Object>> rows = new ArrayList<>();
            while (resultSet.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(metaData.getColumnLabel(i), resultSet.getObject(i));
                }
                rows.add(row);
            }
            result.setData(rows);
            result.setTotalRows(rows.size());

            long duration = System.currentTimeMillis() - startTime;
            result.setExecutionTime(duration);

            log.info("测试计算字段成功: datasetId={}, field={}, rows={}, duration={}ms", 
                    datasetId, field.getName(), rows.size(), duration);

            return result;

        } catch (SQLException e) {
            log.error("测试计算字段SQL执行失败", e);
            throw new ServiceException("SQL执行失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("测试计算字段失败", e);
            throw new ServiceException("测试计算字段失败: " + e.getMessage());
        } finally {
            // 关闭资源
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    log.warn("关闭ResultSet失败", e);
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    log.warn("关闭Statement失败", e);
                }
            }
            if (connection != null) {
                dataSourceManager.releaseConnection(connection);
            }
        }
    }

    /**
     * 检查括号是否匹配
     * 
     * @param expression 表达式
     * @return true表示匹配
     */
    private boolean isParenthesesBalanced(String expression) {
        int count = 0;
        for (char c : expression.toCharArray()) {
            if (c == '(') {
                count++;
            } else if (c == ')') {
                count--;
                if (count < 0) {
                    return false;
                }
            }
        }
        return count == 0;
    }

    /**
     * 转换Map列表为DatasetFieldVO列表
     * 
     * @param fieldMaps 字段Map列表
     * @return DatasetFieldVO列表
     */
    private List<com.zjrcu.iras.bi.platform.domain.dto.DatasetFieldVO> convertToDatasetFieldVOs(
            List<Map<String, Object>> fieldMaps) {
        if (fieldMaps == null || fieldMaps.isEmpty()) {
            return new ArrayList<>();
        }

        List<com.zjrcu.iras.bi.platform.domain.dto.DatasetFieldVO> result = new ArrayList<>();
        for (Map<String, Object> map : fieldMaps) {
            com.zjrcu.iras.bi.platform.domain.dto.DatasetFieldVO vo = 
                    new com.zjrcu.iras.bi.platform.domain.dto.DatasetFieldVO();
            vo.setFieldName((String) map.get("name"));
            vo.setFieldType((String) map.get("type"));
            vo.setFieldComment((String) map.get("comment"));
            result.add(vo);
        }
        return result;
    }
}
