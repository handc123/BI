package com.zjrcu.iras.bi.globaloverview.controller;

import com.zjrcu.iras.bi.globaloverview.domain.qo.GlobalOverviewRequestQO;
import com.zjrcu.iras.bi.globaloverview.service.FiveMajorArticlesMonitoring.IStrategicLoanService;
import com.zjrcu.iras.common.core.controller.BaseController;
import com.zjrcu.iras.common.core.domain.AjaxResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@Tag(name = "五篇大文章监测接口")
@RestController
@RequestMapping("/globalOverview/fiveMajorArticlesMonitoring")
public class FiveMajorArticlesMonitoringController extends BaseController {
    @Resource
    private IStrategicLoanService strategicLoanService;

    /**
     * 获取过去一个月贷款余额、当天发放贷款金额和利率
     */
    @Operation(description = "获取过去一个月贷款余额、当天发放贷款金额和利率")
    @PostMapping("/strategicLoan")
    public AjaxResult selectStrategicLoan(@RequestBody GlobalOverviewRequestQO globalOverviewRequestQO) {
        return success(strategicLoanService.selectStrategicLoan(globalOverviewRequestQO));
    }
}
