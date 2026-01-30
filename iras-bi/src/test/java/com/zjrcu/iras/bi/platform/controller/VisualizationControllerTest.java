package com.zjrcu.iras.bi.platform.controller;

import com.zjrcu.iras.bi.platform.domain.Visualization;
import com.zjrcu.iras.bi.platform.domain.dto.Filter;
import com.zjrcu.iras.bi.platform.domain.dto.QueryResult;
import com.zjrcu.iras.bi.platform.service.IExportService;
import com.zjrcu.iras.bi.platform.service.IVisualizationService;
import com.zjrcu.iras.common.core.domain.AjaxResult;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * VisualizationController单元测试
 *
 * @author iras
 */
class VisualizationControllerTest {

    @Mock
    private IVisualizationService visualizationService;

    @Mock
    private IExportService exportService;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private VisualizationController controller;

    private Visualization testVisualization;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testVisualization = new Visualization();
        testVisualization.setId(1L);
        testVisualization.setName("测试可视化");
        testVisualization.setDatasetId(1L);
        testVisualization.setType("line");
        testVisualization.setConfig("{\"dimensions\":[\"date\"],\"metrics\":[{\"field\":\"amount\",\"aggregation\":\"SUM\"}]}");
    }

    @Test
    void testGetInfo_Success() {
        // 准备测试数据
        when(visualizationService.selectVisualizationById(1L)).thenReturn(testVisualization);

        // 执行测试
        AjaxResult result = controller.getInfo(1L);

        // 验证结果
        assertEquals(200, result.get("code"));
        assertNotNull(result.get("data"));
        verify(visualizationService, times(1)).selectVisualizationById(1L);
    }

    @Test
    void testGetInfo_NotFound() {
        // 准备测试数据
        when(visualizationService.selectVisualizationById(999L)).thenReturn(null);

        // 执行测试
        AjaxResult result = controller.getInfo(999L);

        // 验证结果
        assertEquals(500, result.get("code"));
        assertTrue(result.get("msg").toString().contains("不存在"));
    }

    @Test
    void testAdd_Success() {
        // 准备测试数据
        when(visualizationService.validateVisualizationConfig(any())).thenReturn(null);
        when(visualizationService.insertVisualization(any())).thenReturn(1);

        // 执行测试
        AjaxResult result = controller.add(testVisualization);

        // 验证结果
        assertEquals(200, result.get("code"));
        verify(visualizationService, times(1)).validateVisualizationConfig(any());
        verify(visualizationService, times(1)).insertVisualization(any());
    }

    @Test
    void testAdd_ValidationFailed() {
        // 准备测试数据
        when(visualizationService.validateVisualizationConfig(any()))
            .thenReturn("配置验证失败: 缺少必需字段");

        // 执行测试
        AjaxResult result = controller.add(testVisualization);

        // 验证结果
        assertEquals(500, result.get("code"));
        assertTrue(result.get("msg").toString().contains("配置验证失败"));
        verify(visualizationService, never()).insertVisualization(any());
    }

    @Test
    void testEdit_Success() {
        // 准备测试数据
        when(visualizationService.validateVisualizationConfig(any())).thenReturn(null);
        when(visualizationService.updateVisualization(any())).thenReturn(1);

        // 执行测试
        AjaxResult result = controller.edit(testVisualization);

        // 验证结果
        assertEquals(200, result.get("code"));
        verify(visualizationService, times(1)).updateVisualization(any());
    }

    @Test
    void testRemove_Success() {
        // 准备测试数据
        Long[] ids = {1L, 2L};
        when(visualizationService.deleteVisualizationByIds(ids)).thenReturn(2);

        // 执行测试
        AjaxResult result = controller.remove(ids);

        // 验证结果
        assertEquals(200, result.get("code"));
        verify(visualizationService, times(1)).deleteVisualizationByIds(ids);
    }

    @Test
    void testGetData_Success() {
        // 准备测试数据
        when(visualizationService.selectVisualizationById(1L)).thenReturn(testVisualization);
        
        QueryResult queryResult = new QueryResult();
        queryResult.setSuccess(true);
        
        // 创建符合 List<Map<String, Object>> 类型的测试数据
        Map<String, Object> row1 = new HashMap<>();
        row1.put("date", "2024-01-01");
        row1.put("value", 1000);
        
        Map<String, Object> row2 = new HashMap<>();
        row2.put("date", "2024-01-02");
        row2.put("value", 1500);
        
        queryResult.setData(Arrays.asList(row1, row2));
        when(visualizationService.getVisualizationData(eq(1L), any())).thenReturn(queryResult);

        // 执行测试
        List<Filter> filters = new ArrayList<>();
        AjaxResult result = controller.getData(1L, filters);

        // 验证结果
        assertEquals(200, result.get("code"));
        assertNotNull(result.get("data"));
        verify(visualizationService, times(1)).getVisualizationData(eq(1L), any());
    }

    @Test
    void testGetData_VisualizationNotFound() {
        // 准备测试数据
        when(visualizationService.selectVisualizationById(999L)).thenReturn(null);

        // 执行测试
        AjaxResult result = controller.getData(999L, null);

        // 验证结果
        assertEquals(500, result.get("code"));
        assertTrue(result.get("msg").toString().contains("不存在"));
        verify(visualizationService, never()).getVisualizationData(anyLong(), any());
    }

    @Test
    void testGetData_QueryFailed() {
        // 准备测试数据
        when(visualizationService.selectVisualizationById(1L)).thenReturn(testVisualization);
        
        QueryResult queryResult = new QueryResult();
        queryResult.setSuccess(false);
        queryResult.setMessage("查询执行失败: 连接超时");
        when(visualizationService.getVisualizationData(eq(1L), any())).thenReturn(queryResult);

        // 执行测试
        AjaxResult result = controller.getData(1L, null);

        // 验证结果
        assertEquals(500, result.get("code"));
        assertTrue(result.get("msg").toString().contains("查询执行失败"));
    }

    @Test
    void testExportCSV_Success() {
        // 准备测试数据
        when(visualizationService.selectVisualizationById(1L)).thenReturn(testVisualization);
        doNothing().when(exportService).exportToCSV(eq(1L), any(), eq(response));

        // 执行测试
        controller.exportCSV(1L, null, response);

        // 验证调用
        verify(visualizationService, times(1)).selectVisualizationById(1L);
        verify(exportService, times(1)).exportToCSV(eq(1L), any(), eq(response));
    }

    @Test
    void testExportCSV_VisualizationNotFound() {
        // 准备测试数据
        when(visualizationService.selectVisualizationById(1L)).thenReturn(null);

        // 执行测试
        controller.exportCSV(1L, null, response);

        // 验证不调用导出服务
        verify(exportService, never()).exportToCSV(anyLong(), any(), any());
    }

    @Test
    void testExportExcel_Success() {
        // 准备测试数据
        when(visualizationService.selectVisualizationById(1L)).thenReturn(testVisualization);
        doNothing().when(exportService).exportToExcel(eq(1L), any(), eq(response));

        // 执行测试
        controller.exportExcel(1L, null, response);

        // 验证调用
        verify(visualizationService, times(1)).selectVisualizationById(1L);
        verify(exportService, times(1)).exportToExcel(eq(1L), any(), eq(response));
    }

    @Test
    void testExportExcel_VisualizationNotFound() {
        // 准备测试数据
        when(visualizationService.selectVisualizationById(1L)).thenReturn(null);

        // 执行测试
        controller.exportExcel(1L, null, response);

        // 验证不调用导出服务
        verify(exportService, never()).exportToExcel(anyLong(), any(), any());
    }
}
