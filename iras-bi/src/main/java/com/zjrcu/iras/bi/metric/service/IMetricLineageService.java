package com.zjrcu.iras.bi.metric.service;

import com.zjrcu.iras.bi.metric.domain.MetricLineage;
import com.zjrcu.iras.bi.metric.dto.LineageGraphDTO;
import com.zjrcu.iras.bi.metric.dto.LineagePathDTO;

import java.util.List;

/**
 * 指标血缘服务接口
 *
 * @author iras
 * @date 2025-02-26
 */
public interface IMetricLineageService {

    /**
     * 获取指标的完整血缘图
     *
     * @param metricId 指标ID
     * @param mode 模式: graph-全量, upstream-上游, downstream-下游
     * @return 血缘图数据
     */
    LineageGraphDTO getLineageGraph(Long metricId, String mode);

    /**
     * 获取指标的完整血缘图（带深度控制）
     *
     * @param metricId 指标ID
     * @param maxDepth 最大遍历深度(默认5,建议不超过10)
     * @return 血缘图数据
     */
    LineageGraphDTO getMetricLineageGraph(Long metricId, int maxDepth);

    /**
     * 获取指标的上游血缘路径
     *
     * @param metricId 指标ID
     * @param maxDepth 最大深度(默认5)
     * @return 上游指标列表
     */
    List<MetricLineage> getUpstreamLineage(Long metricId, int maxDepth);

    /**
     * 获取指标的下游血缘路径
     *
     * @param metricId 指标ID
     * @param maxDepth 最大深度(默认5)
     * @return 下游指标列表
     */
    List<MetricLineage> getDownstreamLineage(Long metricId, int maxDepth);

    /**
     * 查找两个指标之间的血缘路径
     *
     * @param sourceId 源指标ID
     * @param targetId 目标指标ID
     * @param maxDepth 最大搜索深度(默认10)
     * @return 路径列表，按路径长度排序
     */
    List<LineagePathDTO> findLineagePath(Long sourceId, Long targetId, int maxDepth);

    /**
     * 创建血缘关系
     *
     * @param lineage 血缘关系
     * @return 结果
     */
    int insertLineage(MetricLineage lineage);

    /**
     * 创建血缘关系
     *
     * @param lineage 血缘关系
     * @return 结果
     */
    int insertMetricLineage(MetricLineage lineage);

    /**
     * 验证血缘关系是否有效
     *
     * @param lineage 血缘关系
     * @return 是否有效
     */
    boolean validateLineage(MetricLineage lineage);

    /**
     * 批量创建血缘关系
     *
     * @param lineages 血缘关系列表
     * @return 结果
     */
    int batchInsertLineage(List<MetricLineage> lineages);

    /**
     * 删除血缘关系
     *
     * @param id 血缘关系ID
     * @return 结果
     */
    int deleteLineageById(Long id);

    /**
     * 批量删除血缘关系
     *
     * @param ids 血缘关系ID列表
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
     * 更新血缘关系
     *
     * @param lineage 血缘关系
     * @return 结果
     */
    int updateLineage(MetricLineage lineage);

    /**
     * 检测血缘关系中是否存在循环依赖
     *
     * @param metricId 指标ID
     * @return 是否存在循环依赖
     */
    boolean hasCircularDependency(Long metricId);

    /**
     * 获取指标的直接上游指标列表（深度为1）
     *
     * @param metricId 指标ID
     * @return 上游指标ID列表
     */
    List<Long> getDirectUpstreamMetricIds(Long metricId);

    /**
     * 获取指标的直接下游指标列表（深度为1）
     *
     * @param metricId 指标ID
     * @return 下游指标ID列表
     */
    List<Long> getDirectDownstreamMetricIds(Long metricId);

    /**
     * 清除血缘图缓存
     * 当血缘关系变更时调用
     *
     * @param metricId 指标ID，如果为null则清除所有缓存
     */
    void clearLineageCache(Long metricId);

    /**
     * 同步血缘节点数据
     * 从指标元数据同步到血缘节点表
     *
     * @param metricId 指标ID
     * @return 结果
     */
    int syncLineageNode(Long metricId);
}
