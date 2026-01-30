package com.zjrcu.iras.bi.platform.service;

import com.zjrcu.iras.bi.platform.domain.Visualization;
import com.zjrcu.iras.bi.platform.domain.dto.Filter;
import com.zjrcu.iras.bi.platform.domain.dto.QueryResult;

import java.util.List;

/**
 * BI可视化服务接口
 *
 * @author iras
 */
public interface IVisualizationService {

    /**
     * 查询可视化列表
     *
     * @param visualization 可视化查询条件
     * @return 可视化列表
     */
    List<Visualization> selectVisualizationList(Visualization visualization);

    /**
     * 根据ID查询可视化
     *
     * @param id 可视化ID
     * @return 可视化信息
     */
    Visualization selectVisualizationById(Long id);

    /**
     * 根据数据集ID查询可视化列表
     *
     * @param datasetId 数据集ID
     * @return 可视化列表
     */
    List<Visualization> selectVisualizationByDatasetId(Long datasetId);

    /**
     * 新增可视化
     *
     * @param visualization 可视化信息
     * @return 结果
     */
    int insertVisualization(Visualization visualization);

    /**
     * 修改可视化
     *
     * @param visualization 可视化信息
     * @return 结果
     */
    int updateVisualization(Visualization visualization);

    /**
     * 删除可视化
     *
     * @param id 可视化ID
     * @return 结果
     */
    int deleteVisualizationById(Long id);

    /**
     * 批量删除可视化
     *
     * @param ids 可视化ID数组
     * @return 结果
     */
    int deleteVisualizationByIds(Long[] ids);

    /**
     * 获取可视化数据
     *
     * @param id 可视化ID
     * @param filters 筛选条件列表
     * @return 查询结果
     */
    QueryResult getVisualizationData(Long id, List<Filter> filters);

    /**
     * 验证可视化配置
     *
     * @param visualization 可视化信息
     * @return 验证结果消息,如果验证通过返回null
     */
    String validateVisualizationConfig(Visualization visualization);
}
