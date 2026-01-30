package com.zjrcu.iras.bi.platform.mapper;

import com.zjrcu.iras.bi.platform.domain.ConditionMapping;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 条件映射Mapper接口
 * 
 * @author zjrcu
 * @date 2026-01-20
 */
@Mapper
public interface ConditionMappingMapper {
    /**
     * 查询条件映射
     * 
     * @param id 条件映射主键
     * @return 条件映射
     */
    public ConditionMapping selectMappingById(Long id);

    /**
     * 查询条件映射列表
     * 
     * @param mapping 条件映射
     * @return 条件映射集合
     */
    public List<ConditionMapping> selectMappingList(ConditionMapping mapping);

    /**
     * 根据仪表板ID查询条件映射列表
     * 
     * @param dashboardId 仪表板ID
     * @return 条件映射集合
     */
    public List<ConditionMapping> selectMappingByDashboardId(Long dashboardId);

    /**
     * 根据条件ID查询条件映射列表
     * 
     * @param conditionId 条件ID
     * @return 条件映射集合
     */
    public List<ConditionMapping> selectMappingByConditionId(Long conditionId);

    /**
     * 根据组件ID查询条件映射列表
     * 
     * @param componentId 组件ID
     * @return 条件映射集合
     */
    public List<ConditionMapping> selectMappingByComponentId(Long componentId);

    /**
     * 新增条件映射
     * 
     * @param mapping 条件映射
     * @return 结果
     */
    public int insertMapping(ConditionMapping mapping);

    /**
     * 修改条件映射
     * 
     * @param mapping 条件映射
     * @return 结果
     */
    public int updateMapping(ConditionMapping mapping);

    /**
     * 删除条件映射
     * 
     * @param id 条件映射主键
     * @return 结果
     */
    public int deleteMappingById(Long id);

    /**
     * 批量删除条件映射
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteMappingByIds(Long[] ids);

    /**
     * 根据仪表板ID删除条件映射
     * 
     * @param dashboardId 仪表板ID
     * @return 结果
     */
    public int deleteMappingByDashboardId(Long dashboardId);

    /**
     * 根据条件ID删除条件映射
     * 
     * @param conditionId 条件ID
     * @return 结果
     */
    public int deleteMappingByConditionId(Long conditionId);

    /**
     * 根据组件ID删除条件映射
     * 
     * @param componentId 组件ID
     * @return 结果
     */
    public int deleteMappingByComponentId(Long componentId);

    /**
     * 批量插入条件映射
     * 
     * @param mappings 条件映射列表
     * @return 结果
     */
    public int batchInsertMappings(List<ConditionMapping> mappings);
}
