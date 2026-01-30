package com.zjrcu.iras.bi.platform.mapper;

import com.zjrcu.iras.bi.platform.domain.QueryCondition;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 查询条件Mapper接口
 * 
 * @author zjrcu
 * @date 2026-01-20
 */
@Mapper
public interface QueryConditionMapper {
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
     * 根据仪表板ID查询查询条件列表
     * 
     * @param dashboardId 仪表板ID
     * @return 查询条件集合
     */
    public List<QueryCondition> selectConditionByDashboardId(Long dashboardId);

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
     * 删除查询条件
     * 
     * @param id 查询条件主键
     * @return 结果
     */
    public int deleteConditionById(Long id);

    /**
     * 批量删除查询条件
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteConditionByIds(Long[] ids);

    /**
     * 根据仪表板ID删除查询条件
     * 
     * @param dashboardId 仪表板ID
     * @return 结果
     */
    public int deleteConditionByDashboardId(Long dashboardId);

    /**
     * 根据组件ID查询查询条件列表
     * 
     * @param componentId 组件ID
     * @return 查询条件集合
     */
    public List<QueryCondition> selectConditionsByComponentId(Long componentId);

    /**
     * 批量更新查询条件显示顺序
     * 
     * @param conditionId 条件ID
     * @param displayOrder 显示顺序
     * @return 结果
     */
    public int updateConditionDisplayOrder(Long conditionId, Integer displayOrder);

    /**
     * 根据组件ID删除查询条件
     * 
     * @param componentId 组件ID
     * @return 结果
     */
    public int deleteConditionByComponentId(Long componentId);

    /**
     * 批量更新查询条件显示顺序
     * 
     * @param orders 条件排序列表
     * @return 结果
     */
    public int batchUpdateDisplayOrder(List<com.zjrcu.iras.bi.platform.domain.dto.ConditionOrderDTO> orders);

    /**
     * 根据ID列表查询查询条件
     * 
     * @param ids 条件ID列表
     * @return 查询条件集合
     */
    public List<QueryCondition> selectConditionsByIds(List<Long> ids);
}
