package com.zjrcu.iras.bi.platform.mapper;

import com.zjrcu.iras.bi.platform.domain.Dashboard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 仪表板Mapper接口
 * 
 * @author zjrcu
 * @date 2026-01-20
 */
@Mapper
public interface DashboardMapper {
    /**
     * 查询仪表板
     * 
     * @param id 仪表板主键
     * @return 仪表板
     */
    public Dashboard selectDashboardById(Long id);

    /**
     * 根据名称查询仪表板
     * 
     * @param dashboardName 仪表板名称
     * @return 仪表板
     */
    public Dashboard selectDashboardByName(String dashboardName);

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
     * 删除仪表板
     * 
     * @param id 仪表板主键
     * @return 结果
     */
    public int deleteDashboardById(Long id);

    /**
     * 批量删除仪表板
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteDashboardByIds(Long[] ids);

    /**
     * 发布仪表板
     * 
     * @param id 仪表板主键
     * @return 结果
     */
    public int publishDashboard(@Param("id") Long id, @Param("publishedVersion") Integer publishedVersion);
}
