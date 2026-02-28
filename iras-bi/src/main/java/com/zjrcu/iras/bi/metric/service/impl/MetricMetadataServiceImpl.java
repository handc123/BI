package com.zjrcu.iras.bi.metric.service.impl;

import com.zjrcu.iras.bi.metric.domain.MetricMetadata;
import com.zjrcu.iras.bi.metric.mapper.MetricMetadataMapper;
import com.zjrcu.iras.bi.metric.service.IMetricMetadataService;
import com.zjrcu.iras.common.core.page.TableDataInfo;
import com.zjrcu.iras.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 指标元数据服务实现
 *
 * @author iras
 * @date 2025-02-26
 */
@Service
public class MetricMetadataServiceImpl implements IMetricMetadataService {

    @Autowired
    private MetricMetadataMapper metricMetadataMapper;

    /**
     * 查询指标元数据列表
     *
     * @param metricMetadata 指标元数据
     * @return 指标元数据
     */
    @Override
    public List<MetricMetadata> selectMetricMetadataList(MetricMetadata metricMetadata) {
        return metricMetadataMapper.selectMetricMetadataList(metricMetadata);
    }

    /**
     * 查询指标元数据分页
     *
     * @param metricMetadata 指标元数据
     * @return 分页结果
     */
    @Override
    public TableDataInfo selectMetricMetadataPage(MetricMetadata metricMetadata) {
        // 注意：实际分页由Controller层的PageHelper处理
        // 这里只返回列表，PageHelper会自动处理分页
        List<MetricMetadata> list = metricMetadataMapper.selectMetricMetadataList(metricMetadata);
        TableDataInfo dataInfo = new TableDataInfo();
        dataInfo.setRows(list);
        return dataInfo;
    }

    /**
     * 查询指标元数据详情
     *
     * @param id 指标元数据主键
     * @return 指标元数据
     */
    @Override
    public MetricMetadata selectMetricMetadataById(Long id) {
        return metricMetadataMapper.selectMetricMetadataById(id);
    }

    /**
     * 根据指标编码查询指标元数据
     *
     * @param metricCode 指标编码
     * @return 指标元数据
     */
    @Override
    public MetricMetadata selectMetricMetadataByCode(String metricCode) {
        return metricMetadataMapper.selectMetricMetadataByCode(metricCode);
    }

    /**
     * 新增指标元数据
     *
     * @param metricMetadata 指标元数据
     * @return 结果
     */
    @Override
    public int insertMetricMetadata(MetricMetadata metricMetadata) {
        metricMetadata.setCreateTime(new Date());
        return metricMetadataMapper.insertMetricMetadata(metricMetadata);
    }

    /**
     * 修改指标元数据
     *
     * @param metricMetadata 指标元数据
     * @return 结果
     */
    @Override
    public int updateMetricMetadata(MetricMetadata metricMetadata) {
        metricMetadata.setUpdateTime(new Date());
        return metricMetadataMapper.updateMetricMetadata(metricMetadata);
    }

    /**
     * 批量删除指标元数据
     *
     * @param ids 需要删除的指标元数据主键
     * @return 结果
     */
    @Override
    public int deleteMetricMetadataByIds(Long[] ids) {
        return metricMetadataMapper.deleteMetricMetadataByIds(ids);
    }

    /**
     * 删除指标元数据
     *
     * @param id 指标元数据主键
     * @return 结果
     */
    @Override
    public int deleteMetricMetadataById(Long id) {
        return metricMetadataMapper.deleteMetricMetadataById(id);
    }

    /**
     * 校验指标编码是否唯一
     *
     * @param metricMetadata 指标元数据
     * @return 结果
     */
    @Override
    public boolean checkMetricCodeUnique(MetricMetadata metricMetadata) {
        MetricMetadata existing = metricMetadataMapper.checkMetricCodeUnique(metricMetadata);
        if (existing == null) {
            return true;
        }
        // 如果是编辑，排除自己
        if (metricMetadata.getId() != null && metricMetadata.getId().equals(existing.getId())) {
            return true;
        }
        return false;
    }

    /**
     * 导入指标元数据
     *
     * @param metricMetadataList 指标元数据列表
     * @param isUpdateSupport 是否更新支持
     * @param operName 操作用户
     * @return 结果
     */
    @Override
    public String importMetricMetadata(List<MetricMetadata> metricMetadataList, Boolean isUpdateSupport, String operName) {
        if (StringUtils.isNull(metricMetadataList) || metricMetadataList.isEmpty()) {
            return "请至少导入一条数据";
        }

        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();

        for (MetricMetadata metric : metricMetadataList) {
            try {
                // 校验编码唯一性
                if (!checkMetricCodeUnique(metric)) {
                    failureNum++;
                    failureMsg.append("<br/>").append(failureNum).append(". ")
                        .append(metric.getMetricName()).append(" 编码已存在");
                    continue;
                }

                int row = metricMetadataMapper.insertMetricMetadata(metric);
                if (row > 0) {
                    successNum++;
                    successMsg.append("<br/>").append(successNum).append(". ")
                        .append(metric.getMetricName());
                }
            } catch (Exception e) {
                failureNum++;
                String msg = "<br/>" + failureNum + ". " + metric.getMetricName() + " : " + e.getMessage();
                failureMsg.append(msg);
            }
        }

        if (failureNum > 0) {
            failureMsg.insert(0, "很抱歉，导入失败！共 " + failureNum + " 条数据格式不正确，错误如下：");
        }
        if (successNum > 0) {
            successMsg.insert(0, "恭喜您，数据已全部导入成功！共 " + successNum + " 条，数据如下：");
        }

        return successMsg.toString();
    }
}
