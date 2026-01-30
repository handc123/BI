package com.zjrcu.iras.bi.platform.service;

import com.zjrcu.iras.bi.platform.domain.Dataset;
import com.zjrcu.iras.bi.platform.domain.dto.Filter;
import com.zjrcu.iras.bi.platform.domain.dto.QueryResult;

import java.util.List;
import java.util.Map;

/**
 * BI数据集服务接口
 *
 * @author iras
 */
public interface IDatasetService {

    /**
     * 查询数据集列表
     *
     * @param dataset 数据集查询条件
     * @return 数据集列表
     */
    List<Dataset> selectDatasetList(Dataset dataset);

    /**
     * 根据ID查询数据集
     *
     * @param id 数据集ID
     * @return 数据集信息
     */
    Dataset selectDatasetById(Long id);

    /**
     * 新增数据集
     *
     * @param dataset 数据集信息
     * @return 结果
     */
    int insertDataset(Dataset dataset);

    /**
     * 修改数据集
     *
     * @param dataset 数据集信息
     * @return 结果
     */
    int updateDataset(Dataset dataset);

    /**
     * 删除数据集
     *
     * @param id 数据集ID
     * @return 结果
     */
    int deleteDatasetById(Long id);

    /**
     * 批量删除数据集
     *
     * @param ids 数据集ID数组
     * @return 结果
     */
    int deleteDatasetByIds(Long[] ids);

    /**
     * 预览数据集数据
     *
     * @param id 数据集ID
     * @param filters 筛选条件列表
     * @return 查询结果
     */
    QueryResult previewDataset(Long id, List<Filter> filters);

    /**
     * 验证数据集配置
     *
     * @param dataset 数据集信息
     * @return 验证结果消息,如果验证通过返回null
     */
    String validateDatasetConfig(Dataset dataset);

    /**
     * 获取数据集字段元数据
     *
     * @param id 数据集ID
     * @return 字段元数据列表
     */
    List<Map<String, Object>> getDatasetFields(Long id);
}
