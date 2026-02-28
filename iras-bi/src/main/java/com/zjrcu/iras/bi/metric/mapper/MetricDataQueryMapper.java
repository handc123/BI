package com.zjrcu.iras.bi.metric.mapper;

import com.zjrcu.iras.bi.metric.domain.MetricDataQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 指标数据查询模板Mapper接口
 *
 * @author iras
 * @date 2025-02-26
 */
@Mapper
public interface MetricDataQueryMapper {

    /**
     * 查询指标数据查询模板列表
     *
     * @param metricDataQuery 指标数据查询模板
     * @return 指标数据查询模板集合
     */
    List<MetricDataQuery> selectMetricDataQueryList(MetricDataQuery metricDataQuery);

    /**
     * 根据ID查询指标数据查询模板
     *
     * @param id 指标数据查询模板主键
     * @return 指标数据查询模板
     */
    MetricDataQuery selectMetricDataQueryById(Long id);

    /**
     * 根据指标ID查询查询模板
     *
     * @param metricId 指标ID
     * @return 查询模板列表
     */
    List<MetricDataQuery> selectQueryTemplatesByMetricId(Long metricId);

    /**
     * 新增指标数据查询模板
     *
     * @param metricDataQuery 指标数据查询模板
     * @return 结果
     */
    int insertMetricDataQuery(MetricDataQuery metricDataQuery);

    /**
     * 修改指标数据查询模板
     *
     * @param metricDataQuery 指标数据查询模板
     * @return 结果
     */
    int updateMetricDataQuery(MetricDataQuery metricDataQuery);

    /**
     * 删除指标数据查询模板
     *
     * @param id 指标数据查询模板主键
     * @return 结果
     */
    int deleteMetricDataQueryById(Long id);

    /**
     * 批量删除指标数据查询模板
     *
     * @param ids 需要删除的指标数据查询模板主键集合
     * @return 结果
     */
    int deleteMetricDataQueryByIds(Long[] ids);

    /**
     * 根据指标ID删除所有查询模板
     *
     * @param metricId 指标ID
     * @return 结果
     */
    int deleteMetricDataQueryByMetricId(Long metricId);

    /**
     * 执行指标数据查询
     *
     * @param metricId 指标ID
     * @param params 查询参数
     * @return 查询结果
     */
    List<Map<String, Object>> executeMetricQuery(@Param("metricId") Long metricId,
                                              @Param("params") Map<String, Object> params);

    /**
     * 统计指标数据行数
     *
     * @param metricId 指标ID
     * @param params 查询参数
     * @return 行数
     */
    Long countMetricData(@Param("metricId") Long metricId,
                          @Param("params") Map<String, Object> params);

    /**
     * 获取指标数据概览
     *
     * @param metricId 指标ID
     * @return 概览信息
     */
    Map<String, Object> getMetricDataOverview(Long metricId);
}
