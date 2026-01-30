package com.zjrcu.iras.bi.platform.mapper;

import com.zjrcu.iras.bi.platform.domain.Visualization;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * BI可视化Mapper接口
 *
 * @author iras
 */
@Mapper
public interface VisualizationMapper {

    /**
     * 查询可视化
     *
     * @param id 可视化主键
     * @return 可视化
     */
    Visualization selectVisualizationById(Long id);

    /**
     * 查询可视化列表
     *
     * @param visualization 可视化
     * @return 可视化集合
     */
    List<Visualization> selectVisualizationList(Visualization visualization);

    /**
     * 新增可视化
     *
     * @param visualization 可视化
     * @return 结果
     */
    int insertVisualization(Visualization visualization);

    /**
     * 修改可视化
     *
     * @param visualization 可视化
     * @return 结果
     */
    int updateVisualization(Visualization visualization);

    /**
     * 删除可视化
     *
     * @param id 可视化主键
     * @return 结果
     */
    int deleteVisualizationById(Long id);

    /**
     * 批量删除可视化
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    int deleteVisualizationByIds(Long[] ids);

    /**
     * 根据数据集ID查询可视化列表
     *
     * @param datasetId 数据集ID
     * @return 可视化集合
     */
    List<Visualization> selectVisualizationByDatasetId(Long datasetId);
}
