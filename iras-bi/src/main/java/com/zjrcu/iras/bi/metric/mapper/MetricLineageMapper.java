package com.zjrcu.iras.bi.metric.mapper;

import com.zjrcu.iras.bi.metric.domain.MetricLineage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 指标血缘Mapper接口
 *
 * @author iras
 * @date 2025-02-26
 */
@Mapper
public interface MetricLineageMapper {

    /**
     * 查询指标血缘列表
     *
     * @param metricLineage 指标血缘
     * @return 指标血缘集合
     */
    List<MetricLineage> selectMetricLineageList(MetricLineage metricLineage);

    /**
     * 根据ID查询指标血缘
     *
     * @param id 指标血缘主键
     * @return 指标血缘
     */
    MetricLineage selectMetricLineageById(Long id);

    /**
     * 查询指标的上游血缘关系
     *
     * @param metricId 指标ID
     * @return 上游血缘列表
     */
    List<MetricLineage> selectUpstreamLineage(Long metricId);

    /**
     * 查询指标的下游血缘关系
     *
     * @param metricId 指标ID
     * @return 下游血缘列表
     */
    List<MetricLineage> selectDownstreamLineage(Long metricId);

    /**
     * 查询直接上游指标ID列表（深度为1）
     *
     * @param metricId 指标ID
     * @return 上游指标ID列表
     */
    List<Long> selectDirectUpstreamMetricIds(Long metricId);

    /**
     * 查询直接下游指标ID列表（深度为1）
     *
     * @param metricId 指标ID
     * @return 下游指标ID列表
     */
    List<Long> selectDirectDownstreamMetricIds(Long metricId);

    /**
     * 查询完整血缘图数据（包含节点和边）
     *
     * @param metricId 指标ID
     * @return 图数据
     */
    List<Map<String, Object>> selectLineageGraphData(Long metricId);

    /**
     * 检测循环依赖
     *
     * @param metricId 指标ID
     * @return 循环依赖数量
     */
    Integer checkCircularDependency(Long metricId);

    /**
     * 新增血缘关系
     *
     * @param lineage 血缘关系
     * @return 结果
     */
    int insertLineage(MetricLineage lineage);

    /**
     * 批量新增血缘关系
     *
     * @param lineages 血缘关系列表
     * @return 插入行数
     */
    int batchInsertLineage(@Param("list") List<MetricLineage> lineages);

    /**
     * 修改血缘关系
     *
     * @param lineage 血缘关系
     * @return 结果
     */
    int updateLineage(MetricLineage lineage);

    /**
     * 删除血缘关系
     *
     * @param id 血缘关系主键
     * @return 结果
     */
    int deleteLineageById(Long id);

    /**
     * 批量删除血缘关系
     *
     * @param ids 需要删除的血缘关系主键集合
     * @return 结果
     */
    int deleteLineageByIds(Long[] ids);

    /**
     * 删除指标的所有血缘关系（包括上游和下游）
     *
     * @param metricId 指标ID
     * @return 结果
     */
    int deleteLineageByMetricId(Long metricId);

    /**
     * 查询两个指标之间的血缘路径
     *
     * @param sourceId 源指标ID
     * @param targetId 目标指标ID
     * @param maxDepth 最大搜索深度
     * @return 路径列表
     */
    List<Map<String, Object>> selectLineagePath(@Param("sourceId") Long sourceId,
                                                   @Param("targetId") Long targetId,
                                                   @Param("maxDepth") int maxDepth);
}
