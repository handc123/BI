package com.zjrcu.iras.bi.metric.service;

import com.zjrcu.iras.bi.metric.domain.MetricMetadata;
import com.zjrcu.iras.bi.metric.dto.MetricResolveRequestDTO;
import com.zjrcu.iras.bi.metric.dto.MetricResolveResponseDTO;
import com.zjrcu.iras.bi.metric.dto.MetricQueryRequest;
import com.zjrcu.iras.common.core.page.TableDataInfo;

import java.util.List;

/**
 * 指标元数据服务接口
 *
 * @author iras
 * @date 2025-02-26
 */
public interface IMetricMetadataService {

    /**
     * 查询指标元数据列表
     *
     * @param metricMetadata 指标元数据
     * @return 指标元数据集合
     */
    List<MetricMetadata> selectMetricMetadataList(MetricMetadata metricMetadata);

    /**
     * 分页查询指标元数据列表
     *
     * @param metricMetadata 指标元数据
     * @return 分页结果
     */
    TableDataInfo selectMetricMetadataPage(MetricMetadata metricMetadata);

    /**
     * 查询指标元数据详情
     *
     * @param id 指标元数据主键
     * @return 指标元数据
     */
    MetricMetadata selectMetricMetadataById(Long id);

    /**
     * 根据指标编码查询指标元数据
     *
     * @param metricCode 指标编码
     * @return 指标元数据
     */
    MetricMetadata selectMetricMetadataByCode(String metricCode);

    /**
     * 新增指标元数据
     *
     * @param metricMetadata 指标元数据
     * @return 结果
     */
    int insertMetricMetadata(MetricMetadata metricMetadata);

    /**
     * 修改指标元数据
     *
     * @param metricMetadata 指标元数据
     * @return 结果
     */
    int updateMetricMetadata(MetricMetadata metricMetadata);

    /**
     * 批量删除指标元数据
     *
     * @param ids 需要删除的指标元数据主键集合
     * @return 结果
     */
    int deleteMetricMetadataByIds(Long[] ids);

    /**
     * 删除指标元数据信息
     *
     * @param id 指标元数据主键
     * @return 结果
     */
    int deleteMetricMetadataById(Long id);

    /**
     * 校验指标编码是否唯一
     *
     * @param metricMetadata 指标元数据
     * @return 结果
     */
    boolean checkMetricCodeUnique(MetricMetadata metricMetadata);

    /**
     * 导入指标元数据
     *
     * @param metricMetadataList 指标元数据列表
     * @param isUpdateSupport 是否更新支持
     * @param operName 操作用户
     * @return 结果
     */
    String importMetricMetadata(List<MetricMetadata> metricMetadataList, Boolean isUpdateSupport, String operName);

    /**
     * 按当前登录机构解析指标
     *
     * @param request 解析请求
     * @return 解析结果
     */
    MetricResolveResponseDTO resolveMetric(MetricResolveRequestDTO request);
}
