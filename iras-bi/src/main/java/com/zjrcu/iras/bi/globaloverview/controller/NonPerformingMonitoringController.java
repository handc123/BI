package com.zjrcu.iras.bi.globaloverview.controller;

import com.zjrcu.iras.bi.globaloverview.domain.qo.GlobalOverviewRequestQO;
import com.zjrcu.iras.bi.globaloverview.service.NonPerformingMonitoring.INonPerformingLoanService;
import com.zjrcu.iras.common.core.controller.BaseController;
import com.zjrcu.iras.common.core.domain.AjaxResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
@Tag(name = "不良监测接口")
@RestController
@RequestMapping("/globalOverview/nonPerformingMonitoring")
public class NonPerformingMonitoringController extends BaseController {
    @Resource
    private INonPerformingLoanService nonPerformingLoanService;

    /**
     * 获取机构对应的地区
     */
    @Operation(description = "获取机构对应的地区")
    @GetMapping("/region")
    public AjaxResult selectRegionByOrgName(@RequestParam("orgName") String orgName) {
        return success(nonPerformingLoanService.selectRegionByOrgName(orgName));
    }


    /**
     * 获取不良贷款率
     */
    @Operation(description = "获取不良贷款率")
    @PostMapping("/nonPerformingLoanRate")
    public AjaxResult selectNonPerformingLoanRate(@RequestBody GlobalOverviewRequestQO globalOverviewRequestQO) {
        return success(nonPerformingLoanService.selectNonPerformingLoanRate(globalOverviewRequestQO));
    }
    /**
     * 获取过去一个月逾期90天以上占比和关注加不良贷款率
     */
    @Operation(description = "获取过去一个月逾期90天以上占比和关注加不良贷款率")
    @PostMapping("/nonPerformingLoanRate/month")
    public AjaxResult selectNonPerformingLoanRateMonth(@RequestBody GlobalOverviewRequestQO globalOverviewRequestQO) {
        return success(nonPerformingLoanService.selectNonPerformingLoanRateMonth(globalOverviewRequestQO));
    }


}
