package com.zjrcu.iras.bi.platform.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zjrcu.iras.bi.platform.domain.Dataset;
import com.zjrcu.iras.bi.platform.domain.Visualization;
import com.zjrcu.iras.bi.platform.domain.dto.Filter;
import com.zjrcu.iras.bi.platform.domain.dto.Metric;
import com.zjrcu.iras.bi.platform.domain.dto.QueryResult;
import com.zjrcu.iras.bi.platform.mapper.DatasetMapper;
import com.zjrcu.iras.bi.platform.mapper.DashboardMapper;
import com.zjrcu.iras.bi.platform.mapper.VisualizationMapper;
import com.zjrcu.iras.bi.platform.service.IQueryExecutor;
import com.zjrcu.iras.bi.platform.service.IVisualizationService;
import com.zjrcu.iras.common.exception.ServiceException;
import com.zjrcu.iras.common.utils.SecurityUtils;
import com.zjrcu.iras.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * BI可视化服务实现
 *
 * @author iras
 */
@Service
public class VisualizationServiceImpl implements IVisualizationService {
    private static final Logger log = LoggerFactory.getLogger(VisualizationServiceImpl.class);

    @Autowired
    private VisualizationMapper visualizationMapper;

    @Autowired
    private DatasetMapper datasetMapper;

    @Autowired
    private DashboardMapper dashboardMapper;

    @Autowired
    private IQueryExecutor queryExecutor;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 查询可视化列表
     *
     * @param visualization 可视化查询条件
     * @return 可视化列表
     */
    @Override
    public List<Visualization> selectVisualizationList(Visualization visualization) {
        return visualizationMapper.selectVisualizationList(visualization);
    }

    /**
     * 根据ID查询可视化
     *
     * @param id 可视化ID
     * @return 可视化信息
     */
    @Override
    public Visualization selectVisualizationById(Long id) {
        if (id == null) {
            throw new ServiceException("可视化ID不能为空");
        }
        return visualizationMapper.selectVisualizationById(id);
    }

    /**
     * 根据数据集ID查询可视化列表
     *
     * @param datasetId 数据集ID
     * @return 可视化列表
     */
    @Override
    public List<Visualization> selectVisualizationByDatasetId(Long datasetId) {
        if (datasetId == null) {
            throw new ServiceException("数据集ID不能为空");
        }
        return visualizationMapper.selectVisualizationByDatasetId(datasetId);
    }

    /**
     * 新增可视化
     *
     * @param visualization 可视化信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertVisualization(Visualization visualization) {
        // 1. 验证可视化配置
        String validationError = validateVisualizationConfig(visualization);
        if (StringUtils.isNotEmpty(validationError)) {
            throw new ServiceException(validationError);
        }

        // 2. 设置创建信息
        visualization.setCreateBy(SecurityUtils.getUsername());

        // 3. 插入可视化
        int result = visualizationMapper.insertVisualization(visualization);

        log.info("新增可视化成功: id={}, name={}, type={}", 
                visualization.getId(), visualization.getName(), visualization.getType());

        return result;
    }

    /**
     * 修改可视化
     *
     * @param visualization 可视化信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateVisualization(Visualization visualization) {
        if (visualization.getId() == null) {
            throw new ServiceException("可视化ID不能为空");
        }

        // 1. 检查可视化是否存在
        Visualization existingVisualization = visualizationMapper.selectVisualizationById(visualization.getId());
        if (existingVisualization == null) {
            throw new ServiceException("可视化不存在");
        }

        // 2. 验证可视化配置
        String validationError = validateVisualizationConfig(visualization);
        if (StringUtils.isNotEmpty(validationError)) {
            throw new ServiceException(validationError);
        }

        // 3. 设置更新信息
        visualization.setUpdateBy(SecurityUtils.getUsername());

        // 4. 更新可视化
        int result = visualizationMapper.updateVisualization(visualization);

        log.info("更新可视化成功: id={}, name={}, type={}", 
                visualization.getId(), visualization.getName(), visualization.getType());

        return result;
    }

    /**
     * 删除可视化
     *
     * @param id 可视化ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteVisualizationById(Long id) {
        if (id == null) {
            throw new ServiceException("可视化ID不能为空");
        }

        // 1. 检查可视化是否存在
        Visualization visualization = visualizationMapper.selectVisualizationById(id);
        if (visualization == null) {
            throw new ServiceException("可视化不存在");
        }

        // 2. 检查是否有仪表板使用此可视化
        // TODO: 实现检查逻辑
        // 当前简化实现,直接删除

        // 3. 删除可视化
        int result = visualizationMapper.deleteVisualizationById(id);

        log.info("删除可视化成功: id={}, name={}", id, visualization.getName());

        return result;
    }

    /**
     * 批量删除可视化
     *
     * @param ids 可视化ID数组
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteVisualizationByIds(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new ServiceException("可视化ID不能为空");
        }

        // 检查每个可视化的依赖关系
        // TODO: 实现检查逻辑

        // 批量删除
        int result = visualizationMapper.deleteVisualizationByIds(ids);

        log.info("批量删除可视化成功: count={}", result);

        return result;
    }

    /**
     * 获取可视化数据
     *
     * @param id 可视化ID
     * @param filters 筛选条件列表
     * @return 查询结果
     */
    @Override
    public QueryResult getVisualizationData(Long id, List<Filter> filters) {
        if (id == null) {
            return QueryResult.failure("可视化ID不能为空");
        }

        try {
            // 1. 获取可视化配置
            Visualization visualization = visualizationMapper.selectVisualizationById(id);
            if (visualization == null) {
                return QueryResult.failure("可视化不存在: " + id);
            }

            // 2. 解析可视化配置
            Map<String, Object> config = visualization.getConfigMap();

            // 3. 合并筛选条件
            List<Filter> allFilters = new ArrayList<>();
            if (filters != null) {
                allFilters.addAll(filters);
            }

            // 从配置中获取筛选条件
            if (config.containsKey("filters")) {
                List<Object> configFilters = (List<Object>) config.get("filters");
                // TODO: 转换为Filter对象并添加到allFilters
            }

            // 4. 根据可视化类型执行查询
            String type = visualization.getType();
            if ("table".equalsIgnoreCase(type) || "kpi".equalsIgnoreCase(type)) {
                // 表格和指标卡: 直接查询
                return queryExecutor.executeQuery(visualization.getDatasetId(), allFilters, SecurityUtils.getLoginUser().getUser());
            } else {
                // 图表类型: 执行聚合查询
                return executeAggregationQuery(visualization, allFilters);
            }

        } catch (Exception e) {
            log.error("获取可视化数据失败: id={}, error={}", id, e.getMessage(), e);
            return QueryResult.failure("获取可视化数据失败: " + e.getMessage());
        }
    }

    /**
     * 执行聚合查询
     *
     * @param visualization 可视化配置
     * @param filters 筛选条件
     * @return 查询结果
     */
    private QueryResult executeAggregationQuery(Visualization visualization, List<Filter> filters) {
        Map<String, Object> config = visualization.getConfigMap();

        // 1. 获取维度字段
        List<String> dimensions = new ArrayList<>();
        if (config.containsKey("dimensions")) {
            Object dimensionsObj = config.get("dimensions");
            if (dimensionsObj instanceof List) {
                dimensions = (List<String>) dimensionsObj;
            }
        }

        // 2. 获取度量字段
        List<Metric> metrics = new ArrayList<>();
        if (config.containsKey("metrics")) {
            List<Object> metricsConfig = (List<Object>) config.get("metrics");
            for (Object metricObj : metricsConfig) {
                if (metricObj instanceof Map) {
                    Map<String, Object> metricMap = (Map<String, Object>) metricObj;
                    Metric metric = new Metric();
                    metric.setField((String) metricMap.get("field"));
                    metric.setAggregation((String) metricMap.getOrDefault("aggregation", "SUM"));
                    metric.setAlias((String) metricMap.get("alias"));
                    metrics.add(metric);
                }
            }
        }

        // 3. 执行聚合查询
        return queryExecutor.executeAggregation(
                visualization.getDatasetId(),
                dimensions,
                metrics,
                filters,
                SecurityUtils.getLoginUser().getUser()
        );
    }

    /**
     * 验证可视化配置
     *
     * @param visualization 可视化信息
     * @return 验证结果消息,如果验证通过返回null
     */
    @Override
    public String validateVisualizationConfig(Visualization visualization) {
        if (visualization == null) {
            return "可视化信息不能为空";
        }

        // 1. 验证基本字段
        if (StringUtils.isEmpty(visualization.getName())) {
            return "可视化名称不能为空";
        }

        if (visualization.getDatasetId() == null) {
            return "数据集ID不能为空";
        }

        if (StringUtils.isEmpty(visualization.getType())) {
            return "可视化类型不能为空";
        }

        // 2. 验证数据集是否存在
        Dataset dataset = datasetMapper.selectDatasetById(visualization.getDatasetId());
        if (dataset == null) {
            return "数据集不存在: " + visualization.getDatasetId();
        }

        // 3. 验证可视化类型
        String[] validTypes = {"kpi", "line", "bar", "map", "table", "pie", "donut", "funnel"};
        boolean validType = false;
        for (String type : validTypes) {
            if (type.equalsIgnoreCase(visualization.getType())) {
                validType = true;
                break;
            }
        }
        if (!validType) {
            return "不支持的可视化类型: " + visualization.getType();
        }

        // 4. 验证配置JSON
        if (StringUtils.isEmpty(visualization.getConfig())) {
            return "可视化配置不能为空";
        }

        String configError = validateConfig(visualization.getConfig());
        if (StringUtils.isNotEmpty(configError)) {
            return configError;
        }

        return null;
    }

    /**
     * 验证配置JSON结构
     *
     * @param config 配置JSON字符串
     * @return 验证结果消息,如果验证通过返回null
     */
    private String validateConfig(String config) {
        try {
            Map<String, Object> configMap = objectMapper.readValue(config, Map.class);

            // 验证必需字段
            // 不同类型的可视化有不同的必需字段
            // 这里进行基本验证

            return null;

        } catch (JsonProcessingException e) {
            return "配置JSON格式错误: " + e.getMessage();
        } catch (Exception e) {
            return "配置验证失败: " + e.getMessage();
        }
    }
}
