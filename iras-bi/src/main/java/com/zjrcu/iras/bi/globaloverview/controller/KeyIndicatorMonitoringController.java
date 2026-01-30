package com.zjrcu.iras.bi.globaloverview.controller;

import com.zjrcu.iras.bi.globaloverview.domain.qo.GlobalOverviewRequestQO;
import com.zjrcu.iras.bi.globaloverview.service.KeyIndicatorMonitoring.IKeyIndicatorService;
import com.zjrcu.iras.common.core.controller.BaseController;
import com.zjrcu.iras.common.core.domain.AjaxResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@Tag(name = "关键指标监控接口")
@RestController
@RequestMapping("/globalOverview/keyIndicatorMonitoring")
public class KeyIndicatorMonitoringController extends BaseController {

    @Resource
    private IKeyIndicatorService keyIndicatorService;
    /**
     * 获取关键指标列表
     */
    @Operation(description = "获取关键指标列表")
    @PostMapping("/keyIndicator")
    public AjaxResult selectKeyIndicator(@RequestBody GlobalOverviewRequestQO globalOverviewRequestQO) {
        return success(keyIndicatorService.selectKeyIndicator(globalOverviewRequestQO));
    }
}
