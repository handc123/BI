package com.zjrcu.iras.bi.platform.service;

import com.zjrcu.iras.bi.platform.domain.QueryCondition;
import com.zjrcu.iras.bi.platform.domain.ConditionMapping;
import com.zjrcu.iras.bi.platform.domain.dto.ChartQueryRequest;
import com.zjrcu.iras.bi.platform.domain.dto.QueryResult;
import com.zjrcu.iras.bi.platform.domain.dto.QueryConditionConfigDTO;
import com.zjrcu.iras.bi.platform.domain.dto.ValidationResult;

import java.util.List;
import java.util.Map;

/**
 * 查询条件Service接口
 * 
 * @author zjrcu
 * @date 2026-01-20
 */
public interface IQueryConditionService {
    /**
     * 查询查询条件
     * 
     * @param id 查询条件主键
     * @return 查询条件
     */
    public QueryCondition selectConditionById(Long id);

    /**
     * 查询查询条件列表
     * 
     * @param condition 查询条件
     * @return 查询条件集合
     */
    public List<QueryCondition> selectConditionList(QueryCondition condition);

    /**
     * 新增查询条件
     * 
     * @param condition 查询条件
     * @return 结果
     */
    public int insertCondition(QueryCondition condition);

    /**
     * 修改查询条件
     * 
     * @param condition 查询条件
     * @return 结果
     */
    public int updateCondition(QueryCondition condition);

    /**
     * 批量删除查询条件
     * 
     * @param ids 需要删除的查询条件主键集合
     * @return 结果
     */
    public int deleteConditionByIds(Long[] ids);

    /**
     * 删除查询条件信息
     * 
     * @param id 查询条件主键
     * @return 结果
     */
    public int deleteConditionById(Long id);

    /**
     * 获取仪表板的条件映射
     * 
     * @param dashboardId 仪表板ID
     * @return 条件映射集合
     */
    public List<ConditionMapping> getMappings(Long dashboardId);

    /**
     * 保存条件映射
     * 
     * @param dashboardId 仪表板ID
     * @param mappings 条件映射列表
     * @return 结果
     */
    public int saveMappings(Long dashboardId, List<ConditionMapping> mappings);

    /**
     * 获取级联选项
     * 
     * @param conditionId 条件ID
     * @param parentValues 父条件值
     * @return 选项列表
     */
    public List<Map<String, Object>> getCascadeOptions(Long conditionId, Map<String, Object> parentValues);

    /**
     * 执行图表查询
     * 
     * @param queryRequest 查询请求
     * @return 查询结果
     */
    public QueryResult executeChartQuery(ChartQueryRequest queryRequest);

    /**
     * 验证查询条件配置
     * 
     * @param config 查询条件配置
     * @return 验证结果
     */
    public ValidationResult validateConditionConfig(QueryConditionConfigDTO config);

    /**
     * 根据组件ID查询查询条件列表
     * 
     * @param componentId 组件ID
     * @return 查询条件集合
     */
    public List<QueryCondition> selectConditionsByComponentId(Long componentId);

    /**
     * 保存查询条件配置
     * 
     * @param config 查询条件配置
     * @return 结果
     */
    public int saveConditionConfig(QueryConditionConfigDTO config);

    /**
     * 批量更新查询条件显示顺序
     * 
     * @param orders 条件排序列表
     * @return 结果
     */
    public int reorderConditions(List<com.zjrcu.iras.bi.platform.domain.dto.ConditionOrderDTO> orders);

    /**
     * 复制查询条件到新组件
     * 
     * @param sourceComponentId 源组件ID
     * @param targetComponentId 目标组件ID
     * @return 结果
     */
    public int copyConditionsToComponent(Long sourceComponentId, Long targetComponentId);
}
