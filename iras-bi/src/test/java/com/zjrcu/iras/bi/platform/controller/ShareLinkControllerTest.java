package com.zjrcu.iras.bi.platform.controller;

import com.zjrcu.iras.bi.platform.domain.Dashboard;
import com.zjrcu.iras.bi.platform.domain.dto.Filter;
import com.zjrcu.iras.bi.platform.service.IDashboardService;
import com.zjrcu.iras.bi.platform.service.IShareLinkService;
import com.zjrcu.iras.bi.platform.service.ShareLinkAccessResult;
import com.zjrcu.iras.common.core.domain.AjaxResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ShareLinkController单元测试
 *
 * @author iras
 */
class ShareLinkControllerTest {

    @Mock
    private IShareLinkService shareLinkService;

    @Mock
    private IDashboardService dashboardService;

    @InjectMocks
    private ShareLinkController controller;

    private Dashboard testDashboard;
    private ShareLinkAccessResult successResult;
    private ShareLinkAccessResult failedResult;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testDashboard = new Dashboard();
        testDashboard.setId(1L);
        testDashboard.setName("测试仪表板");
        testDashboard.setLayoutConfig("{}");
        
        successResult = new ShareLinkAccessResult();
        successResult.setAllowed(true);
        successResult.setResourceType("dashboard");
        successResult.setResourceId(1L);
        
        failedResult = new ShareLinkAccessResult();
        failedResult.setAllowed(false);
        failedResult.setErrorMessage("共享链接已过期");
    }

    @Test
    void testValidateAccess_Success() {
        // 准备测试数据
        when(shareLinkService.validateAccess("abc123", null)).thenReturn(successResult);

        // 执行测试
        AjaxResult result = controller.validateAccess("abc123", null);

        // 验证结果
        assertEquals(200, result.get("code"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertTrue((Boolean) data.get("valid"));
        assertEquals("dashboard", data.get("resourceType"));
        assertEquals(1L, data.get("resourceId"));
        
        verify(shareLinkService, times(1)).validateAccess("abc123", null);
    }

    @Test
    void testValidateAccess_Failed() {
        // 准备测试数据
        when(shareLinkService.validateAccess("expired123", null)).thenReturn(failedResult);

        // 执行测试
        AjaxResult result = controller.validateAccess("expired123", null);

        // 验证结果
        assertEquals(500, result.get("code"));
        assertTrue(result.get("msg").toString().contains("已过期"));
    }

    @Test
    void testGetSharedDashboard_Success() {
        // 准备测试数据
        when(shareLinkService.validateAccess("abc123", null)).thenReturn(successResult);
        when(dashboardService.selectDashboardById(1L)).thenReturn(testDashboard);
        doNothing().when(shareLinkService).recordAccess("abc123");

        // 执行测试
        AjaxResult result = controller.getSharedDashboard("abc123", null);

        // 验证结果
        assertEquals(200, result.get("code"));
        Dashboard dashboard = (Dashboard) result.get("data");
        assertNotNull(dashboard);
        assertEquals("测试仪表板", dashboard.getName());
        
        verify(shareLinkService, times(1)).validateAccess("abc123", null);
        verify(dashboardService, times(1)).selectDashboardById(1L);
        verify(shareLinkService, times(1)).recordAccess("abc123");
    }

    @Test
    void testGetSharedDashboard_AccessDenied() {
        // 准备测试数据
        when(shareLinkService.validateAccess("expired123", null)).thenReturn(failedResult);

        // 执行测试
        AjaxResult result = controller.getSharedDashboard("expired123", null);

        // 验证结果
        assertEquals(500, result.get("code"));
        assertTrue(result.get("msg").toString().contains("已过期"));
        
        verify(dashboardService, never()).selectDashboardById(anyLong());
        verify(shareLinkService, never()).recordAccess(anyString());
    }

    @Test
    void testGetSharedDashboard_WrongResourceType() {
        // 准备测试数据
        ShareLinkAccessResult wrongTypeResult = new ShareLinkAccessResult();
        wrongTypeResult.setAllowed(true);
        wrongTypeResult.setResourceType("visualization");
        wrongTypeResult.setResourceId(1L);
        
        when(shareLinkService.validateAccess("wrong123", null)).thenReturn(wrongTypeResult);

        // 执行测试
        AjaxResult result = controller.getSharedDashboard("wrong123", null);

        // 验证结果
        assertEquals(500, result.get("code"));
        assertTrue(result.get("msg").toString().contains("类型不匹配"));
    }

    @Test
    void testGetSharedDashboard_DashboardNotFound() {
        // 准备测试数据
        when(shareLinkService.validateAccess("abc123", null)).thenReturn(successResult);
        when(dashboardService.selectDashboardById(1L)).thenReturn(null);

        // 执行测试
        AjaxResult result = controller.getSharedDashboard("abc123", null);

        // 验证结果
        assertEquals(500, result.get("code"));
        assertTrue(result.get("msg").toString().contains("不存在"));
    }

    @Test
    void testGetSharedDashboardData_Success() {
        // 准备测试数据
        when(shareLinkService.validateAccess("abc123", null)).thenReturn(successResult);
        
        Map<Long, Object> dashboardData = new HashMap<>();
        dashboardData.put(1L, Map.of("rows", List.of()));
        when(dashboardService.getDashboardData(eq(1L), any())).thenReturn(dashboardData);
        
        doNothing().when(shareLinkService).recordAccess("abc123");

        // 执行测试
        AjaxResult result = controller.getSharedDashboardData("abc123", null, null);

        // 验证结果
        assertEquals(200, result.get("code"));
        assertNotNull(result.get("data"));
        
        verify(shareLinkService, times(1)).validateAccess("abc123", null);
        verify(dashboardService, times(1)).getDashboardData(eq(1L), any());
        verify(shareLinkService, times(1)).recordAccess("abc123");
    }

    @Test
    void testGetSharedDashboardData_AccessDenied() {
        // 准备测试数据
        when(shareLinkService.validateAccess("expired123", null)).thenReturn(failedResult);

        // 执行测试
        AjaxResult result = controller.getSharedDashboardData("expired123", null, null);

        // 验证结果
        assertEquals(500, result.get("code"));
        assertTrue(result.get("msg").toString().contains("已过期"));
        
        verify(dashboardService, never()).getDashboardData(anyLong(), any());
    }

    @Test
    void testRevokeShareLink_Success() {
        // 准备测试数据
        when(shareLinkService.revokeShareLink("abc123")).thenReturn(1);

        // 执行测试
        AjaxResult result = controller.revokeShareLink("abc123");

        // 验证结果
        assertEquals(200, result.get("code"));
        verify(shareLinkService, times(1)).revokeShareLink("abc123");
    }

    @Test
    void testRevokeShareLink_NotFound() {
        // 准备测试数据
        when(shareLinkService.revokeShareLink("notfound123")).thenReturn(0);

        // 执行测试
        AjaxResult result = controller.revokeShareLink("notfound123");

        // 验证结果
        assertEquals(500, result.get("code"));
    }
}
