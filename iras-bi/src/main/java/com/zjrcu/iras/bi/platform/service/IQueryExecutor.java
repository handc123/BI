package com.zjrcu.iras.bi.platform.service;

import com.zjrcu.iras.bi.platform.domain.dto.Filter;
import com.zjrcu.iras.bi.platform.domain.dto.Metric;
import com.zjrcu.iras.bi.platform.domain.dto.QueryResult;
import com.zjrcu.iras.common.core.domain.entity.SysUser;

import java.util.List;

/**
 * 查询执行器接口
 * 负责执行数据集查询并应用筛选、计算字段和权限
 *
 * @author iras
 */
public interface IQueryExecutor {

    /**
     * 执行数据集查询
     *
     * @param datasetId 数据集ID
     * @param filters 筛选条件列表
     * @param user 当前用户(用于权限过滤)
     * @return 查询结果
     */
    QueryResult executeQuery(Long datasetId, List<Filter> filters, SysUser user);

    /**
     * 执行聚合查询
     *
     * @param datasetId 数据集ID
     * @param dimensions 维度字段列表
     * @param metrics 度量字段列表
     * @param filters 筛选条件列表
     * @param user 当前用户
     * @return 聚合结果
     */
    QueryResult executeAggregation(Long datasetId, List<String> dimensions,
                                   List<Metric> metrics, List<Filter> filters, SysUser user);
}
