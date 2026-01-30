package com.zjrcu.iras.bi.platform.service;

import com.zjrcu.iras.bi.platform.domain.dto.Filter;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

/**
 * BI导出服务接口
 *
 * @author iras
 */
public interface IExportService {

    /**
     * 导出可视化数据为CSV
     *
     * @param visualizationId 可视化ID
     * @param filters 筛选条件
     * @param response HTTP响应
     */
    void exportToCSV(Long visualizationId, List<Filter> filters, HttpServletResponse response);

    /**
     * 导出可视化数据为Excel
     *
     * @param visualizationId 可视化ID
     * @param filters 筛选条件
     * @param response HTTP响应
     */
    void exportToExcel(Long visualizationId, List<Filter> filters, HttpServletResponse response);

    /**
     * 导出仪表板为PDF
     *
     * @param dashboardId 仪表板ID
     * @param filters 全局筛选器
     * @param response HTTP响应
     */
    void exportToPDF(Long dashboardId, List<Filter> filters, HttpServletResponse response);

    /**
     * 异步导出仪表板为PDF(用于大数据量场景)
     *
     * @param dashboardId 仪表板ID
     * @param filters 全局筛选器
     * @return 任务ID
     */
    String exportToPDFAsync(Long dashboardId, List<Filter> filters);

    /**
     * 获取异步导出任务状态
     *
     * @param taskId 任务ID
     * @return 任务状态
     */
    ExportTaskStatus getExportTaskStatus(String taskId);
}
