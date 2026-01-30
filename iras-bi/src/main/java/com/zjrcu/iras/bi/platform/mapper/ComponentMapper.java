package com.zjrcu.iras.bi.platform.mapper;

import com.zjrcu.iras.bi.platform.domain.DashboardComponent;
import com.zjrcu.iras.bi.platform.domain.dto.ComponentPosition;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 仪表板组件Mapper接口
 * 
 * @author zjrcu
 * @date 2026-01-20
 */
@Mapper
public interface ComponentMapper {
    /**
     * 查询组件
     * 
     * @param id 组件主键
     * @return 组件
     */
    public DashboardComponent selectComponentById(Long id);

    /**
     * 查询组件列表
     * 
     * @param component 组件
     * @return 组件集合
     */
    public List<DashboardComponent> selectComponentList(DashboardComponent component);

    /**
     * 根据仪表板ID查询组件列表
     * 
     * @param dashboardId 仪表板ID
     * @return 组件集合
     */
    public List<DashboardComponent> selectComponentByDashboardId(Long dashboardId);

    /**
     * 新增组件
     * 
     * @param component 组件
     * @return 结果
     */
    public int insertComponent(DashboardComponent component);

    /**
     * 修改组件
     * 
     * @param component 组件
     * @return 结果
     */
    public int updateComponent(DashboardComponent component);

    /**
     * 删除组件
     * 
     * @param id 组件主键
     * @return 结果
     */
    public int deleteComponentById(Long id);

    /**
     * 批量删除组件
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteComponentByIds(Long[] ids);

    /**
     * 根据仪表板ID删除组件
     * 
     * @param dashboardId 仪表板ID
     * @return 结果
     */
    public int deleteComponentByDashboardId(Long dashboardId);

    /**
     * 批量更新组件位置
     * 
     * @param position 组件位置信息
     * @return 结果
     */
    public int updateComponentPosition(ComponentPosition position);
}
