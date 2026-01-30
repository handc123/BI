package com.zjrcu.iras.bi.platform.controller;

import com.zjrcu.iras.bi.platform.domain.Dashboard;
import com.zjrcu.iras.bi.platform.domain.dto.Filter;
import com.zjrcu.iras.bi.platform.service.IDashboardService;
import com.zjrcu.iras.bi.platform.service.IExportService;
import com.zjrcu.iras.common.core.domain.AjaxResult;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * DashboardController单元测试
 *
 * @author iras
 */
class DashboardControllerTest {

    @Mock
    private IDashboardService dashboardService;

    @Mock
    private IExportService exportService;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private DashboardController controller;

    private Dashboard testDashboard;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testDashboard = new Dashboard();
        testDashboard.setId(1L);
        testDashboard.setName("测试仪表板");
        testDashboard.setLayoutConfig("{\"components\":[{\"id\":\"comp_1\",\"visualizationId\":1}]}");
        testDashboard.setFilterConfig("{\"filters\":[]}");
        testDashboard.setThemeConfig("{\"primaryColor\":\"#409EFF\"}");
        testDashboard.setStatus("0");
    }

    @Test
    void testGetInfo_Success() {
        // 准备测试数据
        when(dashboardService.selectDashboardById(1L)).thenReturn(testDashboard);

        // 执行测试
        AjaxResult result = controller.getInfo(1L);

        // 验证结果
        assertEquals(200, result.get("code"));
        assertNotNull(result.get("data"));
        verify(dashboardService, times(1)).selectDashboardById(1L);
    }

    @Test
    void testGetInfo_NotFound() {
        // 准备测试数据
        when(dashboardService.selectDashboardById(999L)).thenReturn(null);

        // 执行测试
        AjaxResult result = controller.getInfo(999L);

        // 验证结果
        assertEquals(500, result.get("code"));
        assertTrue(result.get("msg").toString().contains("不存在"));
    }

    @Test
    void testAdd_Success() {
        // 准备测试数据
        when(dashboardService.validateDashboardConfig(any())).thenReturn(null);
        when(dashboardService.insertDashboard(any())).thenReturn(1);

        // 执行测试
        AjaxResult result = controller.add(testDashboard);

        // 验证结果
        assertEquals(200, result.get("code"));
        verify(dashboardService, times(1)).validateDashboardConfig(any());
        verify(dashboardService, times(1)).insertDashboard(any());
    }

    @Test
    void testAdd_ValidationFailed() {
        // 准备测试数据
        when(dashboardService.validateDashboardConfig(any()))
            .thenReturn("配置验证失败: 布局配置无效");

        // 执行测试
        AjaxResult result = controller.add(testDashboard);

        // 验证结果
        assertEquals(500, result.get("code"));
        assertTrue(result.get("msg").toString().contains("配置验证失败"));
        verify(dashboardService, never()).insertDashboard(any());
    }

    @Test
    void testEdit_Success() {
        // 准备测试数据
        when(dashboardService.validateDashboardConfig(any())).thenReturn(null);
        when(dashboardService.updateDashboard(any())).thenReturn(1);

        // 执行测试
        AjaxResult result = controller.edit(testDashboard);

        // 验证结果
        assertEquals(200, result.get("code"));
        verify(dashboardService, times(1)).updateDashboard(any());
    }

    @Test
    void testRemove_Success() {
        // 准备测试数据
        Long[] ids = {1L, 2L};
        when(dashboardService.deleteDashboardByIds(ids)).thenReturn(2);

        // 执行测试
        AjaxResult result = controller.remove(ids);

        // 验证结果
        assertEquals(200, result.get("code"));
        verify(dashboardService, times(1)).deleteDashboardByIds(ids);
    }

    @Test
    void testGetData_Success() {
        // 准备测试数据
        when(dashboardService.selectDashboardById(1L)).thenReturn(testDashboard);
        
        Map<Long, Object> dashboardData = new HashMap<>();
        dashboardData.put(1L, Map.of("data", List.of(List.of("2024-01-01", 1000))));
        dashboardData.put(2L, Map.of("data", List.of(List.of("2024-01-01", 500))));
        when(dashboardService.getDashboardData(eq(1L), any())).thenReturn(dashboardData);

        // 执行测试
        List<Filter> filters = new ArrayList<>();
        AjaxResult result = controller.getData(1L, filters);

        // 验证结果
        assertEquals(200, result.get("code"));
        assertNotNull(result.get("data"));
        verify(dashboardService, times(1)).getDashboardData(eq(1L), any());
    }

    @Test
    void testGetData_DashboardNotFound() {
        // 准备测试数据
        when(dashboardService.selectDashboardById(999L)).thenReturn(null);

        // 执行测试
        AjaxResult result = controller.getData(999L, null);

        // 验证结果
        assertEquals(500, result.get("code"));
        assertTrue(result.get("msg").toString().contains("不存在"));
        verify(dashboardService, never()).getDashboardData(anyLong(), any());
    }

    @Test
    void testExportPDF_Success() {
        // 准备测试数据
        when(dashboardService.selectDashboardById(1L)).thenReturn(testDashboard);
        doNothing().when(exportService).exportToPDF(eq(1L), any(), eq(response));

        // 执行测试
        controller.exportPDF(1L, null, response);

        // 验证结果
        verify(exportService, times(1)).exportToPDF(eq(1L), any(), eq(response));
    }

    @Test
    void testExportPDF_DashboardNotFound() {
        // 准备测试数据
        when(dashboardService.selectDashboardById(999L)).thenReturn(null);

        // 执行测试
        controller.exportPDF(999L, null, response);

        // 验证结果
        verify(exportService, never()).exportToPDF(anyLong(), any(), any());
    }

    @Test
    void testShare_Success() {
        // 准备测试数据
        when(dashboardService.selectDashboardById(1L)).thenReturn(testDashboard);
        when(dashboardService.generateShareLink(1L, "password123"))
            .thenReturn("https://example.com/share/abc123");

        // 执行测试
        AjaxResult result = controller.share(1L, "password123");

        // 验证结果
        assertEquals(200, result.get("code"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data.get("shareLink"));
        assertTrue((Boolean) data.get("hasPassword"));
        verify(dashboardService, times(1)).generateShareLink(1L, "password123");
    }

    @Test
    void testShare_WithoutPassword() {
        // 准备测试数据
        when(dashboardService.selectDashboardById(1L)).thenReturn(testDashboard);
        when(dashboardService.generateShareLink(1L, null))
            .thenReturn("https://example.com/share/xyz789");

        // 执行测试
        AjaxResult result = controller.share(1L, null);

        // 验证结果
        assertEquals(200, result.get("code"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data.get("shareLink"));
        assertFalse((Boolean) data.get("hasPassword"));
    }

    @Test
    void testShare_DashboardNotFound() {
        // 准备测试数据
        when(dashboardService.selectDashboardById(999L)).thenReturn(null);

        // 执行测试
        AjaxResult result = controller.share(999L, null);

        // 验证结果
        assertEquals(500, result.get("code"));
        assertTrue(result.get("msg").toString().contains("不存在"));
        verify(dashboardService, never()).generateShareLink(anyLong(), any());
    }
}
