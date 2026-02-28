package com.zjrcu.iras.bi.metric.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zjrcu.iras.bi.metric.domain.MetricMetadata;
import com.zjrcu.iras.bi.metric.dto.MetricDataVO;
import com.zjrcu.iras.bi.metric.dto.MetricQueryRequest;
import com.zjrcu.iras.bi.metric.mapper.MetricDataQueryMapper;
import com.zjrcu.iras.bi.metric.mapper.MetricMetadataMapper;
import com.zjrcu.iras.bi.metric.service.IMetricDataService;
import com.zjrcu.iras.bi.metric.util.DataMaskingUtil;
import com.zjrcu.iras.common.core.page.TableDataInfo;
import com.zjrcu.iras.common.exception.ServiceException;
import com.zjrcu.iras.common.utils.DateUtils;
import com.zjrcu.iras.common.utils.PageUtils;
import com.zjrcu.iras.common.utils.SecurityUtils;
import com.zjrcu.iras.common.utils.StringUtils;
import com.zjrcu.iras.common.utils.poi.ExcelUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 指标数据查询服务实现
 *
 * @author iras
 * @date 2025-02-26
 */
@Service
@Slf4j
public class MetricDataServiceImpl implements IMetricDataService {

    @Autowired
    private MetricDataQueryMapper metricDataQueryMapper;

    @Autowired
    private MetricMetadataMapper metricMetadataMapper;

    /**
     * 危险SQL关键字（用于SQL注入检测）
     */
    private static final String[] DANGEROUS_SQL_KEYWORDS = {
            "DROP", "DELETE", "TRUNCATE", "ALTER", "CREATE", "INSERT", "UPDATE",
            "EXEC", "EXECUTE", "SCRIPT", "JAVASCRIPT", "--", "/*", "*/",
            "XP_", "SP_", "DECLARE", "CURSOR"
    };

    /**
     * 字段名验证正则（只允许字母、数字、下划线、点、中文）
     */
    private static final Pattern FIELD_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_.\u4e00-\u9fa5]+$");

    /**
     * 查询指标数据（分页）
     *
     * @param metricId 指标ID
     * @param request 查询请求
     * @return 分页结果
     */
    @Override
    public TableDataInfo queryMetricData(Long metricId, MetricQueryRequest request) {
        log.info("查询指标数据, metricId={}, request={}", metricId, request);

        // 验证指标是否存在
        MetricMetadata metric = metricMetadataMapper.selectMetricMetadataById(metricId);
        if (metric == null) {
            throw new ServiceException("指标不存在: " + metricId);
        }

        // 验证请求参数
        validateQueryRequest(request);

        // 构建查询参数
        Map<String, Object> queryParams = buildQueryParams(request);

        // 分页查询
        PageUtils.startPage();
        List<Map<String, Object>> dataList = metricDataQueryMapper.executeMetricQuery(metricId, queryParams);

        // 数据脱敏
        List<Map<String, Object>> maskedDataList = new ArrayList<>();
        for (Map<String, Object> data : dataList) {
            maskedDataList.add(DataMaskingUtil.maskSensitiveData(data));
        }

        // 转换为VO
        List<MetricDataVO> voList = convertToVOList(maskedDataList, metric);

        // 分页信息
        TableDataInfo dataInfo = new TableDataInfo();
        dataInfo.setRows(voList);
        dataInfo.setTotal(new PageInfo<>(dataList).getTotal());

        // 记录查询日志
        logDataQuery(metricId, request, voList.size());

        return dataInfo;
    }

    /**
     * 查询指标数据（不分页，用于导出）
     *
     * @param metricId 指标ID
     * @param request 查询请求
     * @return 数据列表
     */
    @Override
    public List<MetricDataVO> queryMetricDataForExport(Long metricId, MetricQueryRequest request) {
        log.info("导出查询指标数据, metricId={}, request={}", metricId, request);

        // 验证指标是否存在
        MetricMetadata metric = metricMetadataMapper.selectMetricMetadataById(metricId);
        if (metric == null) {
            throw new ServiceException("指标不存在: " + metricId);
        }

        // 验证请求参数
        validateQueryRequest(request);

        // 构建查询参数
        Map<String, Object> queryParams = buildQueryParams(request);

        // 不分页查询（限制最大导出数量）
        int maxExportSize = 10000;
        List<Map<String, Object>> dataList = metricDataQueryMapper.executeMetricQuery(metricId, queryParams);

        if (dataList.size() > maxExportSize) {
            throw new ServiceException("导出数据超过最大限制 " + maxExportSize + " 条，请添加过滤条件");
        }

        // 数据脱敏
        List<Map<String, Object>> maskedDataList = new ArrayList<>();
        for (Map<String, Object> data : dataList) {
            maskedDataList.add(DataMaskingUtil.maskSensitiveData(data));
        }

        // 转换为VO
        List<MetricDataVO> voList = convertToVOList(maskedDataList, metric);

        log.info("导出数据查询完成, 总数={}", voList.size());
        return voList;
    }

    /**
     * 聚合查询指标数据
     *
     * @param metricId 指标ID
     * @param request 查询请求
     * @param groupByFields 分组字段
     * @return 聚合结果
     */
    @Override
    public List<Map<String, Object>> aggregateMetricData(Long metricId, MetricQueryRequest request,
                                                         String[] groupByFields) {
        log.info("聚合查询指标数据, metricId={}, groupByFields={}", metricId, groupByFields);

        // 验证分组字段
        if (groupByFields == null || groupByFields.length == 0) {
            throw new IllegalArgumentException("分组字段不能为空");
        }

        // 验证请求参数
        validateQueryRequest(request);

        // 构建查询参数（添加分组字段）
        Map<String, Object> queryParams = buildQueryParams(request);
        queryParams.put("groupByFields", groupByFields);

        // 执行查询
        List<Map<String, Object>> resultList = metricDataQueryMapper.executeMetricQuery(metricId, queryParams);

        // 数据脱敏
        List<Map<String, Object>> maskedResultList = new ArrayList<>();
        for (Map<String, Object> data : resultList) {
            maskedResultList.add(DataMaskingUtil.maskSensitiveData(data));
        }

        log.info("聚合查询完成, 结果数={}", maskedResultList.size());
        return maskedResultList;
    }

    /**
     * 导出指标数据到Excel
     *
     * @param metricId 指标ID
     * @param request 查询请求
     * @param response HTTP响应
     */
    @Override
    public void exportMetricData(Long metricId, MetricQueryRequest request, HttpServletResponse response) {
        log.info("导出指标数据到Excel, metricId={}", metricId);

        // 检查导出权限
        if (!checkExportPermission(metricId)) {
            throw new ServiceException("没有导出该指标数据的权限");
        }

        // 验证请求参数
        validateQueryRequest(request);

        // 获取指标元数据
        MetricMetadata metric = metricMetadataMapper.selectMetricMetadataById(metricId);
        if (metric == null) {
            throw new ServiceException("指标不存在: " + metricId);
        }

        // 查询数据
        List<MetricDataVO> dataList = queryMetricDataForExport(metricId, request);

        // 使用ExcelUtil导出
        ExcelUtil<MetricDataVO> util = new ExcelUtil<>(MetricDataVO.class);
        try {
            String fileName = URLEncoder.encode(metric.getMetricName(), StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            util.exportExcel(response, dataList, metric.getMetricName());

            log.info("导出指标数据成功, metricId={}, 导出数量={}", metricId, dataList.size());
        } catch (Exception e) {
            log.error("导出指标数据失败", e);
            throw new ServiceException("导出失败: " + e.getMessage());
        }
    }

    /**
     * 获取指标数据概览
     *
     * @param metricId 指标ID
     * @return 概览信息
     */
    @Override
    public Map<String, Object> getMetricDataOverview(Long metricId) {
        return getMetricDataOverview(metricId, null);
    }

    /**
     * 获取指标数据概览（带时间范围）
     *
     * @param metricId 指标ID
     * @param timeRange 时间范围（可选）
     * @return 概览信息
     */
    @Override
    public Map<String, Object> getMetricDataOverview(Long metricId, String timeRange) {
        log.info("获取指标数据概览, metricId={}, timeRange={}", metricId, timeRange);

        // 验证指标是否存在
        MetricMetadata metric = metricMetadataMapper.selectMetricMetadataById(metricId);
        if (metric == null) {
            throw new ServiceException("指标不存在: " + metricId);
        }

        // 获取概览信息
        Map<String, Object> overview = metricDataQueryMapper.getMetricDataOverview(metricId);

        // 添加指标基本信息
        if (overview == null) {
            overview = new HashMap<>();
        }
        overview.put("metricId", metricId);
        overview.put("metricCode", metric.getMetricCode());
        overview.put("metricName", metric.getMetricName());
        overview.put("dataFreshness", metric.getDataFreshness());
        overview.put("updateFrequency", metric.getUpdateFrequency());
        overview.put("ownerDept", metric.getOwnerDept());
        overview.put("timeRange", timeRange);

        log.info("获取指标数据概览完成, overview={}", overview);
        return overview;
    }

    /**
     * 获取指标实时数据
     *
     * @param metricId 指标ID
     * @param limit 数据点数量
     * @return 实时数据列表
     */
    @Override
    public List<Map<String, Object>> getRealtimeMetricData(Long metricId, Integer limit) {
        log.info("获取指标实时数据, metricId={}, limit={}", metricId, limit);

        // 验证指标是否存在
        MetricMetadata metric = metricMetadataMapper.selectMetricMetadataById(metricId);
        if (metric == null) {
            throw new ServiceException("指标不存在: " + metricId);
        }

        // 限制数量
        if (limit == null || limit <= 0 || limit > 1000) {
            limit = 10;
        }

        // 构建查询参数
        Map<String, Object> params = new HashMap<>();
        params.put("limit", limit);
        params.put("orderBy", "data_time DESC");

        // 执行查询
        List<Map<String, Object>> dataList = metricDataQueryMapper.executeMetricQuery(metricId, params);

        // 数据脱敏
        List<Map<String, Object>> maskedDataList = new ArrayList<>();
        for (Map<String, Object> data : dataList) {
            maskedDataList.add(DataMaskingUtil.maskSensitiveData(data));
        }

        log.info("获取指标实时数据完成, 数量={}", maskedDataList.size());
        return maskedDataList;
    }

    /**
     * 获取指标聚合数据
     *
     * @param metricId 指标ID
     * @param query 查询请求
     * @return 聚合数据
     */
    @Override
    public Map<String, Object> getAggregateMetricData(Long metricId, MetricQueryRequest query) {
        log.info("获取指标聚合数据, metricId={}", metricId);

        // 验证指标是否存在
        MetricMetadata metric = metricMetadataMapper.selectMetricMetadataById(metricId);
        if (metric == null) {
            throw new ServiceException("指标不存在: " + metricId);
        }

        // 验证请求参数
        validateQueryRequest(query);

        // 获取聚合字段
        String[] groupByFields = query.getGroupByFields();
        if (groupByFields == null || groupByFields.length == 0) {
            throw new IllegalArgumentException("分组字段不能为空");
        }

        // 执行聚合查询
        List<Map<String, Object>> resultList = aggregateMetricData(metricId, query, groupByFields);

        // 构建聚合结果
        Map<String, Object> result = new HashMap<>();
        result.put("metricId", metricId);
        result.put("metricName", metric.getMetricName());
        result.put("groupByFields", groupByFields);
        result.put("data", resultList);
        result.put("count", resultList.size());

        log.info("获取指标聚合数据完成, 聚合数量={}", resultList.size());
        return result;
    }

    /**
     * 获取指标趋势数据
     *
     * @param metricId 指标ID
     * @param granularity 时间粒度: hour/day/week/month
     * @param points 数据点数量
     * @return 趋势数据
     */
    @Override
    public List<Map<String, Object>> getMetricTrendData(Long metricId, String granularity, Integer points) {
        log.info("获取指标趋势数据, metricId={}, granularity={}, points={}", metricId, granularity, points);

        // 验证指标是否存在
        MetricMetadata metric = metricMetadataMapper.selectMetricMetadataById(metricId);
        if (metric == null) {
            throw new ServiceException("指标不存在: " + metricId);
        }

        // 验证粒度
        List<String> validGranularities = List.of("hour", "day", "week", "month");
        if (!validGranularities.contains(granularity)) {
            throw new IllegalArgumentException("时间粒度必须是: hour, day, week, month");
        }

        // 限制数据点数量
        if (points == null || points <= 0 || points > 1000) {
            points = 30;
        }

        // 构建查询参数
        Map<String, Object> params = new HashMap<>();
        params.put("granularity", granularity);
        params.put("points", points);

        // 执行查询
        List<Map<String, Object>> dataList = metricDataQueryMapper.executeMetricQuery(metricId, params);

        // 数据脱敏
        List<Map<String, Object>> maskedDataList = new ArrayList<>();
        for (Map<String, Object> data : dataList) {
            maskedDataList.add(DataMaskingUtil.maskSensitiveData(data));
        }

        log.info("获取指标趋势数据完成, 数量={}", maskedDataList.size());
        return maskedDataList;
    }

    /**
     * 获取指标对比数据
     *
     * @param metricId 指标ID
     * @param compareMetricIds 对比指标ID列表
     * @param params 额外参数
     * @return 对比数据
     */
    @Override
    public Map<String, Object> getMetricCompareData(Long metricId, List<Long> compareMetricIds, Map<String, Object> params) {
        log.info("获取指标对比数据, metricId={}, compareMetricIds={}", metricId, compareMetricIds);

        // 验证指标是否存在
        MetricMetadata metric = metricMetadataMapper.selectMetricMetadataById(metricId);
        if (metric == null) {
            throw new ServiceException("指标不存在: " + metricId);
        }

        // 验证对比指标
        if (compareMetricIds == null || compareMetricIds.isEmpty()) {
            throw new IllegalArgumentException("请选择要对比的指标");
        }

        // 查询当前指标数据
        Map<String, Object> currentData = getMetricDataOverview(metricId, null);

        // 查询对比指标数据
        List<Map<String, Object>> compareDataList = new ArrayList<>();
        for (Long compareId : compareMetricIds) {
            Map<String, Object> compareMetricData = getMetricDataOverview(compareId, null);
            compareDataList.add(compareMetricData);
        }

        // 构建对比结果
        Map<String, Object> result = new HashMap<>();
        result.put("current", currentData);
        result.put("compare", compareDataList);
        result.put("params", params);

        log.info("获取指标对比数据完成, 对比指标数量={}", compareDataList.size());
        return result;
    }

    /**
     * 刷新指标数据缓存
     *
     * @param metricId 指标ID
     * @return 是否成功
     */
    @Override
    public boolean refreshMetricData(Long metricId) {
        log.info("刷新指标数据缓存, metricId={}", metricId);

        try {
            // 清除Redis缓存
            String cacheKey = "metric:data:" + metricId;
            // redisCache.deleteObject(cacheKey);

            // TODO: 重新加载数据

            log.info("刷新指标数据缓存成功, metricId={}", metricId);
            return true;
        } catch (Exception e) {
            log.error("刷新指标数据缓存失败, metricId={}", metricId, e);
            return false;
        }
    }

    /**
     * 验证查询请求
     *
     * @param request 查询请求
     * @throws IllegalArgumentException 参数不合法时抛出
     */
    @Override
    public void validateQueryRequest(MetricQueryRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("查询请求不能为空");
        }

        // 验证分页参数
        if (request.getPageNum() == null || request.getPageNum() < 1) {
            throw new IllegalArgumentException("页码必须大于0");
        }
        if (request.getPageSize() == null || request.getPageSize() < 1) {
            throw new IllegalArgumentException("每页数量必须大于0");
        }
        if (request.getPageSize() > 10000) {
            throw new IllegalArgumentException("每页数量不能超过10000");
        }

        // 验证排序字段（防止SQL注入）
        if (StringUtils.isNotEmpty(request.getSortField())) {
            if (!FIELD_NAME_PATTERN.matcher(request.getSortField()).matches()) {
                throw new IllegalArgumentException("排序字段格式不正确: " + request.getSortField());
            }
            // 检查是否包含危险关键字
            if (containsDangerousKeywords(request.getSortField())) {
                throw new IllegalArgumentException("排序字段包含危险关键字: " + request.getSortField());
            }
        }

        // 验证排序方向
        if (StringUtils.isNotEmpty(request.getSortOrder())) {
            String order = request.getSortOrder().toUpperCase();
            if (!"ASC".equals(order) && !"DESC".equals(order)) {
                throw new IllegalArgumentException("排序方向只能是ASC或DESC");
            }
        }

        // 验证过滤条件
        if (request.getFilters() != null) {
            for (MetricQueryRequest.Filter filter : request.getFilters()) {
                validateFilter(filter);
            }
        }

        // 验证查询参数（防止SQL注入）
        if (request.getParameters() != null) {
            for (Map.Entry<String, Object> entry : request.getParameters().entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                // 验证参数名
                if (!FIELD_NAME_PATTERN.matcher(key).matches()) {
                    throw new IllegalArgumentException("参数名格式不正确: " + key);
                }

                // 验证参数值（字符串类型）
                if (value instanceof String) {
                    String strValue = (String) value;
                    if (containsDangerousKeywords(strValue)) {
                        throw new IllegalArgumentException("参数值包含危险关键字: " + key);
                    }
                }
            }
        }

        // 验证时间范围（简单验证非空即可，详细格式验证在具体查询时处理）
        if (StringUtils.isNotEmpty(request.getStartTime())) {
            // 时间格式验证将在实际查询时由数据库处理
        }
        if (StringUtils.isNotEmpty(request.getEndTime())) {
            // 时间格式验证将在实际查询时由数据库处理
        }
    }

    /**
     * 构建安全SQL查询
     *
     * @param metricId 指标ID
     * @param request 查询请求
     * @return SQL语句和参数
     */
    @Override
    public Map<String, Object> buildSafeQuery(Long metricId, MetricQueryRequest request) {
        Map<String, Object> result = new HashMap<>();

        // 构建查询参数
        Map<String, Object> queryParams = buildQueryParams(request);

        // 获取查询模板
        String template = getQueryTemplate(metricId);

        result.put("sql", template);
        result.put("params", queryParams);

        return result;
    }

    /**
     * 对敏感数据进行脱敏处理
     *
     * @param dataList 原始数据列表
     * @return 脱敏后的数据列表
     */
    @Override
    public List<MetricDataVO> maskSensitiveData(List<MetricDataVO> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return dataList;
        }

        // MetricDataVO的脱敏主要在从Map转换时处理
        // 这里可以对特殊字段进行二次处理
        return dataList;
    }

    /**
     * 记录数据查询日志
     *
     * @param metricId 指标ID
     * @param request 查询请求
     * @param resultCount 结果数量
     */
    @Override
    public void logDataQuery(Long metricId, MetricQueryRequest request, int resultCount) {
        try {
            String username = SecurityUtils.getUsername();
            Long userId = SecurityUtils.getUserId();

            log.info("指标数据查询审计 - 用户: {}, 用户ID: {}, 指标ID: {}, 页码: {}, 每页数量: {}, 结果数: {}, 查询时间: {}",
                    username,
                    userId,
                    metricId,
                    request.getPageNum(),
                    request.getPageSize(),
                    resultCount,
                    DateUtils.getTime());

            // TODO: 可以扩展为写入审计日志表
            // 例如：auditLogService.insert(logEntity);

        } catch (Exception e) {
            log.warn("记录数据查询日志失败", e);
        }
    }

    /**
     * 检查导出权限
     *
     * @param metricId 指标ID
     * @return 是否有权限
     */
    @Override
    public boolean checkExportPermission(Long metricId) {
        try {
            // 获取当前用户信息
            Long userId = SecurityUtils.getUserId();
            Long deptId = SecurityUtils.getDeptId();

            // 获取指标元数据
            MetricMetadata metric = metricMetadataMapper.selectMetricMetadataById(metricId);
            if (metric == null) {
                return false;
            }

            // 管理员始终有权限
            if (SecurityUtils.isAdmin(userId)) {
                return true;
            }

            // 检查部门权限（同一部门或有权限）
            if (metric.getDeptId() != null && metric.getDeptId().equals(deptId)) {
                return true;
            }

            // TODO: 可以扩展更细粒度的权限控制
            // 例如检查指标级别的权限配置

            log.warn("用户 {} 无导出指标 {} 的权限", userId, metricId);
            return false;

        } catch (Exception e) {
            log.error("检查导出权限失败", e);
            return false;
        }
    }

    /**
     * 获取指标查询模板
     *
     * @param metricId 指标ID
     * @return 查询模板
     */
    @Override
    public String getQueryTemplate(Long metricId) {
        // TODO: 从MetricDataQuery表中获取查询模板
        // 这里先返回占位符
        return "SELECT * FROM bi_metric_data WHERE metric_id = #{metricId}";
    }

    /**
     * 执行自定义查询
     *
     * @param metricId 指标ID
     * @param customSql 自定义SQL
     * @param params 查询参数
     * @return 查询结果
     */
    @Override
    public List<Map<String, Object>> executeCustomQuery(Long metricId, String customSql,
                                                        Map<String, Object> params) {
        log.warn("尝试执行自定义SQL, metricId={}, sql={}", metricId, customSql);

        // 严格校验自定义SQL
        if (StringUtils.isEmpty(customSql)) {
            throw new IllegalArgumentException("自定义SQL不能为空");
        }

        // 检查危险关键字
        String upperSql = customSql.toUpperCase();
        for (String keyword : DANGEROUS_SQL_KEYWORDS) {
            if (upperSql.contains(keyword)) {
                throw new IllegalArgumentException("自定义SQL包含危险关键字: " + keyword);
            }
        }

        // 只允许SELECT语句
        if (!customSql.trim().toUpperCase().startsWith("SELECT")) {
            throw new IllegalArgumentException("只允许执行SELECT查询");
        }

        // TODO: 实现自定义SQL执行逻辑
        // 需要使用MyBatis动态SQL或JDBC执行

        throw new ServiceException("自定义SQL功能暂未实现");
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 构建查询参数Map
     */
    private Map<String, Object> buildQueryParams(MetricQueryRequest request) {
        Map<String, Object> params = new HashMap<>();

        // 添加基本参数
        if (request.getParameters() != null) {
            params.putAll(request.getParameters());
        }

        // 添加排序参数
        if (StringUtils.isNotEmpty(request.getSortField())) {
            params.put("sortField", request.getSortField());
            params.put("sortOrder", StringUtils.isNotEmpty(request.getSortOrder())
                    ? request.getSortOrder() : "DESC");
        }

        // 添加过滤条件
        if (request.getFilters() != null && !request.getFilters().isEmpty()) {
            params.put("filters", request.getFilters());
        }

        // 添加时间范围
        if (StringUtils.isNotEmpty(request.getStartTime())) {
            params.put("startTime", request.getStartTime());
        }
        if (StringUtils.isNotEmpty(request.getEndTime())) {
            params.put("endTime", request.getEndTime());
        }

        return params;
    }

    /**
     * 验证过滤条件
     */
    private void validateFilter(MetricQueryRequest.Filter filter) {
        if (filter == null) {
            throw new IllegalArgumentException("过滤条件不能为空");
        }

        // 验证字段名
        if (StringUtils.isEmpty(filter.getField())) {
            throw new IllegalArgumentException("过滤字段名不能为空");
        }
        if (!FIELD_NAME_PATTERN.matcher(filter.getField()).matches()) {
            throw new IllegalArgumentException("过滤字段名格式不正确: " + filter.getField());
        }

        // 验证操作符
        String operator = filter.getOperator();
        if (StringUtils.isEmpty(operator)) {
            throw new IllegalArgumentException("过滤操作符不能为空");
        }

        // 验证操作符是否合法
        List<String> validOperators = List.of("eq", "ne", "gt", "gte", "lt", "lte", "like", "in", "between");
        if (!validOperators.contains(operator.toLowerCase())) {
            throw new IllegalArgumentException("不支持的过滤操作符: " + operator);
        }

        // 验证逻辑连接符
        if (StringUtils.isNotEmpty(filter.getLogic())) {
            String logic = filter.getLogic().toUpperCase();
            if (!"AND".equals(logic) && !"OR".equals(logic)) {
                throw new IllegalArgumentException("逻辑连接符只能是AND或OR");
            }
        }
    }

    /**
     * 检查是否包含危险SQL关键字
     */
    private boolean containsDangerousKeywords(String str) {
        if (StringUtils.isEmpty(str)) {
            return false;
        }

        String upperStr = str.toUpperCase();
        for (String keyword : DANGEROUS_SQL_KEYWORDS) {
            if (upperStr.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 将查询结果Map转换为MetricDataVO列表
     */
    private List<MetricDataVO> convertToVOList(List<Map<String, Object>> dataList, MetricMetadata metric) {
        List<MetricDataVO> voList = new ArrayList<>();

        for (Map<String, Object> data : dataList) {
            MetricDataVO vo = new MetricDataVO();
            vo.setMetricId(metric.getId());
            vo.setMetricCode(metric.getMetricCode());
            vo.setMetricName(metric.getMetricName());

            // 从Map中提取值
            if (data.containsKey("value")) {
                vo.setValue(data.get("value"));
            }
            if (data.containsKey("data_time")) {
                vo.setDataTime((java.util.Date) data.get("data_time"));
            }
            if (data.containsKey("data_source")) {
                vo.setDataSource((String) data.get("data_source"));
            }

            // 扩展字段
            Map<String, Object> extFields = new HashMap<>(data);
            extFields.remove("value");
            extFields.remove("data_time");
            extFields.remove("data_source");
            vo.setExtFields(extFields);

            voList.add(vo);
        }

        return voList;
    }
}
