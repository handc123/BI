package com.zjrcu.iras.bi.metric.service;

import com.zjrcu.iras.bi.metric.dto.MetricDataVO;
import com.zjrcu.iras.bi.metric.dto.MetricQueryRequest;
import com.zjrcu.iras.common.core.page.TableDataInfo;
import jakarta.servlet.http.HttpServletResponse;


import java.util.List;
import java.util.Map;

/**
 * 指标数据查询服务接口
 *
 * @author iras
 * @date 2025-02-26
 */
public interface IMetricDataService {

    /**
     * 查询指标数据（分页）
     *
     * @param metricId 指标ID
     * @param request 查询请求
     * @return 分页结果
     */
    TableDataInfo queryMetricData(Long metricId, MetricQueryRequest request);

    /**
     * 查询指标数据（不分页，用于导出）
     *
     * @param metricId 指标ID
     * @param request 查询请求
     * @return 数据列表
     */
    List<MetricDataVO> queryMetricDataForExport(Long metricId, MetricQueryRequest request);

    /**
     * 聚合查询指标数据
     * 按维度分组聚合数据
     *
     * @param metricId 指标ID
     * @param request 查询请求
     * @param groupByFields 分组字段
     * @return 聚合结果
     */
    List<Map<String, Object>> aggregateMetricData(Long metricId, MetricQueryRequest request, String[] groupByFields);

    /**
     * 导出指标数据到Excel
     *
     * @param metricId 指标ID
     * @param request 查询请求
     * @param response HTTP响应
     */
    void exportMetricData(Long metricId, MetricQueryRequest request, HttpServletResponse response);

    /**
     * 获取指标数据概览
     * 包含数据行数、最新数据时间等信息
     *
     * @param metricId 指标ID
     * @return 概览信息
     */
    Map<String, Object> getMetricDataOverview(Long metricId);

    /**
     * 获取指标数据概览（带时间范围）
     * 包含数据行数、最新数据时间等信息
     *
     * @param metricId 指标ID
     * @param timeRange 时间范围（可选）
     * @return 概览信息
     */
    Map<String, Object> getMetricDataOverview(Long metricId, String timeRange);

    /**
     * 获取指标实时数据
     *
     * @param metricId 指标ID
     * @param limit 数据点数量
     * @return 实时数据列表
     */
    List<Map<String, Object>> getRealtimeMetricData(Long metricId, Integer limit);

    /**
     * 获取指标聚合数据
     *
     * @param metricId 指标ID
     * @param query 查询请求
     * @return 聚合数据
     */
    Map<String, Object> getAggregateMetricData(Long metricId, MetricQueryRequest query);

    /**
     * 获取指标趋势数据
     *
     * @param metricId 指标ID
     * @param granularity 时间粒度: hour/day/week/month
     * @param points 数据点数量
     * @return 趋势数据
     */
    List<Map<String, Object>> getMetricTrendData(Long metricId, String granularity, Integer points);

    /**
     * 获取指标对比数据
     *
     * @param metricId 指标ID
     * @param compareMetricIds 对比指标ID列表
     * @param params 额外参数
     * @return 对比数据
     */
    Map<String, Object> getMetricCompareData(Long metricId, List<Long> compareMetricIds, Map<String, Object> params);

    /**
     * 刷新指标数据缓存
     *
     * @param metricId 指标ID
     * @return 是否成功
     */
    boolean refreshMetricData(Long metricId);

    /**
     * 验证查询请求
     * 检查查询参数是否合法，防止SQL注入
     *
     * @param request 查询请求
     * @throws IllegalArgumentException 参数不合法时抛出
     */
    void validateQueryRequest(MetricQueryRequest request);

    /**
     * 构建安全SQL查询
     * 基于查询模板和参数构建安全的SQL语句
     *
     * @param metricId 指标ID
     * @param request 查询请求
     * @return SQL语句和参数
     */
    Map<String, Object> buildSafeQuery(Long metricId, MetricQueryRequest request);

    /**
     * 对敏感数据进行脱敏处理
     *
     * @param dataList 原始数据列表
     * @return 脱敏后的数据列表
     */
    List<MetricDataVO> maskSensitiveData(List<MetricDataVO> dataList);

    /**
     * 记录数据查询日志
     * 用于审计和监控
     *
     * @param metricId 指标ID
     * @param request 查询请求
     * @param resultCount 结果数量
     */
    void logDataQuery(Long metricId, MetricQueryRequest request, int resultCount);

    /**
     * 检查导出权限
     * 验证用户是否有导出该指标数据的权限
     *
     * @param metricId 指标ID
     * @return 是否有权限
     */
    boolean checkExportPermission(Long metricId);

    /**
     * 获取指标查询模板
     *
     * @param metricId 指标ID
     * @return 查询模板
     */
    String getQueryTemplate(Long metricId);

    /**
     * 执行自定义查询
     * 用户可以自定义SQL查询（需要严格校验）
     *
     * @param metricId 指标ID
     * @param customSql 自定义SQL
     * @param params 查询参数
     * @return 查询结果
     */
    List<Map<String, Object>> executeCustomQuery(Long metricId, String customSql, Map<String, Object> params);
}
