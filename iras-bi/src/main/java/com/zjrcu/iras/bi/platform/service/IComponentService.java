package com.zjrcu.iras.bi.platform.service;

import com.zjrcu.iras.bi.platform.domain.DashboardComponent;
import com.zjrcu.iras.bi.platform.domain.dto.ComponentPosition;

import java.util.List;

/**
 * 仪表板组件Service接口
 * 
 * @author zjrcu
 * @date 2026-01-20
 */
public interface IComponentService {
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
     * 批量删除组件
     * 
     * @param ids 需要删除的组件主键集合
     * @return 结果
     */
    public int deleteComponentByIds(Long[] ids);

    /**
     * 删除组件信息
     * 
     * @param id 组件主键
     * @return 结果
     */
    public int deleteComponentById(Long id);

    /**
     * 批量更新组件位置
     * 
     * @param positions 组件位置列表
     * @return 结果
     */
    public int batchUpdatePosition(List<ComponentPosition> positions);

    /**
     * 复制组件
     * 
     * @param id 组件主键
     * @return 新组件
     */
    public DashboardComponent copyComponent(Long id);
}
