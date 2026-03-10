package com.zjrcu.iras.bi.metric.mapper;

import com.zjrcu.iras.bi.metric.domain.MetricMetadata;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 指标元数据Mapper接口
 *
 * @author iras
 * @date 2025-02-26
 */
@Mapper
public interface MetricMetadataMapper {

    /**
     * 查询指标元数据列表
     *
     * @param metricMetadata 指标元数据
     * @return 指标元数据集合
     */
    List<MetricMetadata> selectMetricMetadataList(MetricMetadata metricMetadata);

    /**
     * 根据ID查询指标元数据
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
     * 删除指标元数据（软删除）
     *
     * @param id 指标元数据主键
     * @return 结果
     */
    int deleteMetricMetadataById(Long id);

    /**
     * 批量删除指标元数据（软删除）
     *
     * @param ids 需要删除的指标元数据主键集合
     * @return 结果
     */
    int deleteMetricMetadataByIds(Long[] ids);

    /**
     * 校验指标编码是否唯一
     *
     * @param metricMetadata 指标元数据
     * @return 结果
     */
    @Select({
            "<script>",
            "SELECT id, metric_code, metric_name, dataset_id, visualization_id,",
            "       business_definition, technical_formula, calculation_logic,",
            "       owner_dept, dept_id, data_freshness, update_frequency,",
            "       status, del_flag, create_by, create_time, update_by, update_time, remark",
            "FROM bi_metric_metadata",
            "WHERE del_flag = '0'",
            "  AND metric_code = #{metricCode}",
            "  <if test='deptId != null'>AND dept_id = #{deptId}</if>",
            "  <if test='datasetId != null'>AND dataset_id = #{datasetId}</if>",
            "LIMIT 1",
            "</script>"
    })
    MetricMetadata checkMetricCodeUnique(MetricMetadata metricMetadata);

    /**
     * 根据部门ID查询指标列表
     *
     * @param deptId 部门ID
     * @return 指标列表
     */
    List<MetricMetadata> selectMetricsByDeptId(Long deptId);

    /**
     * 根据数据集ID查询指标列表
     *
     * @param datasetId 数据集ID
     * @return 指标列表
     */
    List<MetricMetadata> selectMetricsByDatasetId(Long datasetId);

    /**
     * 批量插入指标元数据
     *
     * @param metricMetadataList 指标元数据列表
     * @return 插入行数
     */
    int batchInsertMetrics(@Param("list") List<MetricMetadata> metricMetadataList);

    /**
     * 根据ID列表批量查询指标元数据
     *
     * @param ids 指标ID列表
     * @return 指标元数据列表
     */
    List<MetricMetadata> selectMetricMetadataByIds(@Param("list") List<Long> ids);
}
