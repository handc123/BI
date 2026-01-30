package com.zjrcu.iras.bi.globaloverview.controller;

import com.zjrcu.iras.bi.globaloverview.domain.qo.GlobalOverviewRequestQO;
import com.zjrcu.iras.bi.globaloverview.service.KeyRiskMonitoring.IKeyDomainLoanRateService;
import com.zjrcu.iras.bi.globaloverview.service.KeyRiskMonitoring.INonPerformingLoanTransactionService;
import com.zjrcu.iras.common.core.controller.BaseController;
import com.zjrcu.iras.common.core.domain.AjaxResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@Tag(name = "关键风险监控接口")
@RestController
@RequestMapping("/globalOverview/keyRiskMonitoring")
public class KeyRiskMonitoringController extends BaseController {
    @Resource
    private INonPerformingLoanTransactionService nonPerformingLoanTransactionService;
    @Resource
    private IKeyDomainLoanRateService keyDomainLoanRateService;

    @Operation(description = "获取新增与处置不良贷款金额")
    /**
     * 获取新增与处置不良贷款金额
     */
    @PostMapping("/nonPerformingLoanTransaction")
    public AjaxResult selectNonPerformingLoanTransaction(@RequestBody GlobalOverviewRequestQO globalOverviewRequestQO){
        return success(nonPerformingLoanTransactionService.selectNonPerformingLoanTransaction(globalOverviewRequestQO));
    }
    /**
     * 获取重点领域贷款发放利率
     */
    @Operation(description = "获取重点领域贷款发放利率")
    @PostMapping("/keyDomainLoanRate")
    public AjaxResult selectKeyDomainLoanRate(@RequestBody GlobalOverviewRequestQO globalOverviewRequestQO){
        return success(keyDomainLoanRateService.selectKeyDomainLoanRate(globalOverviewRequestQO));
    }

}
