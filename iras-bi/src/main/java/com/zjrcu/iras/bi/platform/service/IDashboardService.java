package com.zjrcu.iras.bi.platform.service;

import com.zjrcu.iras.bi.platform.domain.Dashboard;
import com.zjrcu.iras.bi.platform.domain.dto.DashboardConfig;

import java.util.List;

/**
 * 仪表板Service接口
 * 
 * @author zjrcu
 * @date 2026-01-20
 */
public interface IDashboardService {
    /**
     * 查询仪表板
     * 
     * @param id 仪表板主键
     * @return 仪表板
     */
    public Dashboard selectDashboardById(Long id);

    /**
     * 查询仪表板列表
     * 
     * @param dashboard 仪表板
     * @return 仪表板集合
     */
    public List<Dashboard> selectDashboardList(Dashboard dashboard);

    /**
     * 新增仪表板
     * 
     * @param dashboard 仪表板
     * @return 结果
     */
    public int insertDashboard(Dashboard dashboard);

    /**
     * 修改仪表板
     * 
     * @param dashboard 仪表板
     * @return 结果
     */
    public int updateDashboard(Dashboard dashboard);

    /**
     * 批量删除仪表板
     * 
     * @param ids 需要删除的仪表板主键集合
     * @return 结果
     */
    public int deleteDashboardByIds(Long[] ids);

    /**
     * 删除仪表板信息
     * 
     * @param id 仪表板主键
     * @return 结果
     */
    public int deleteDashboardById(Long id);

    /**
     * 获取仪表板完整配置
     * 
     * @param id 仪表板主键
     * @return 仪表板配置
     */
    public DashboardConfig getDashboardConfig(Long id);

    /**
     * 保存仪表板完整配置
     * 
     * @param dashboardId 仪表板主键
     * @param config 仪表板配置
     * @return 结果
     */
    public int saveDashboardConfig(Long dashboardId, DashboardConfig config);

    /**
     * 发布仪表板
     * 
     * @param id 仪表板主键
     * @return 结果
     */
    public int publishDashboard(Long id);

    /**
     * 取消发布仪表板
     * 
     * @param id 仪表板主键
     * @return 结果
     */
    public int unpublishDashboard(Long id);

    /**
     * 编辑已发布的仪表板 - 创建草稿副本
     * 
     * @param id 仪表板主键
     * @return 草稿副本ID
     */
    public Long createDraftFromPublished(Long id);
}
