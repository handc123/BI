package com.zjrcu.iras.bi.platform.service;

import com.zjrcu.iras.bi.platform.domain.DataSource;
import com.zjrcu.iras.bi.platform.domain.dto.ConnectionTestResult;

import java.util.List;
import java.util.Map;

/**
 * BI数据源服务接口
 *
 * @author iras
 */
public interface IDataSourceService {

    /**
     * 查询数据源列表
     *
     * @param dataSource 数据源查询条件
     * @return 数据源列表
     */
    List<DataSource> selectDataSourceList(DataSource dataSource);

    /**
     * 根据ID查询数据源
     *
     * @param id 数据源ID
     * @return 数据源信息
     */
    DataSource selectDataSourceById(Long id);

    /**
     * 新增数据源
     *
     * @param dataSource 数据源信息
     * @return 结果
     */
    int insertDataSource(DataSource dataSource);

    /**
     * 修改数据源
     *
     * @param dataSource 数据源信息
     * @return 结果
     */
    int updateDataSource(DataSource dataSource);

    /**
     * 删除数据源
     *
     * @param id 数据源ID
     * @return 结果
     */
    int deleteDataSourceById(Long id);

    /**
     * 批量删除数据源
     *
     * @param ids 数据源ID数组
     * @return 结果
     */
    int deleteDataSourceByIds(Long[] ids);

    /**
     * 测试数据源连接
     *
     * @param dataSource 数据源配置
     * @return 连接测试结果
     */
    ConnectionTestResult testConnection(DataSource dataSource);

    /**
     * 获取数据源的表列表
     *
     * @param dataSourceId 数据源ID
     * @return 表名列表
     */
    List<String> getTableList(Long dataSourceId);

    /**
     * 获取数据源的表列表（包含表名和注释）
     *
     * @param dataSourceId 数据源ID
     * @return 表信息列表，每个元素包含 tableName 和 tableComment
     */
    List<Map<String, String>> getTableListWithComments(Long dataSourceId);

    /**
     * 获取表结构信息
     *
     * @param dataSourceId 数据源ID
     * @param tableName    表名
     * @return 表结构信息
     */
    com.zjrcu.iras.bi.platform.domain.dto.TableSchemaInfo getTableSchema(Long dataSourceId, String tableName);

    /**
     * 获取表数据预览
     *
     * @param dataSourceId 数据源ID
     * @param tableName    表名
     * @param limit        限制行数
     * @return 表数据预览
     */
    com.zjrcu.iras.bi.platform.domain.dto.TablePreviewData getTablePreview(Long dataSourceId, String tableName, int limit);
}
