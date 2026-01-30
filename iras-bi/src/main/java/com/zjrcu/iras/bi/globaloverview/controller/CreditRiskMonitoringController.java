package com.zjrcu.iras.bi.globaloverview.controller;

import com.zjrcu.iras.bi.globaloverview.domain.qo.GlobalOverviewRequestQO;
import com.zjrcu.iras.bi.globaloverview.service.CreditRiskMonitoring.ICreditRiskDetailService;
import com.zjrcu.iras.common.core.controller.BaseController;
import com.zjrcu.iras.common.core.domain.AjaxResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Tag(name = "信用风险监测接口")
@RestController
@RequestMapping("/globalOverview/creditRiskMonitoring")
public class CreditRiskMonitoringController extends BaseController {
    @Resource
    private ICreditRiskDetailService creditRiskDetailService;

    /**
     * 获取指定日期的不良贷款余额和不良贷款率
     * @return
     */
    @Operation(description = "获取指定日期的不良贷款余额和不良贷款率")
    @PostMapping("/nonPerformingLoanSummary")
    public AjaxResult selectNonPerformingLoanDetail(@RequestBody GlobalOverviewRequestQO globalOverviewRequestQO){
        return success(creditRiskDetailService.selectNonPerformingLoanSummary(globalOverviewRequestQO));
    }
    /**
     * 获取不同逾期天数的不良贷款余额和不良贷款比例
     */
    @Operation(description = "获取不同逾期天数的不良贷款余额和不良贷款比例")
    @PostMapping("/OverdueLoanPeriod")
    private AjaxResult selectOverdueLoanPeriod(@RequestBody GlobalOverviewRequestQO globalOverviewRequestQO){
        return success(creditRiskDetailService.selectOverdueLoanPeriod(globalOverviewRequestQO));
    }
    /**
     * 获取逾期情况
     */
    @Operation(description = "获取逾期情况")
    @PostMapping("/overdueLoanCondition")
    private AjaxResult selectOverdueLoanCondition(@RequestBody GlobalOverviewRequestQO globalOverviewRequestQO){
        return success(creditRiskDetailService.selectOverdueLoanCondition(globalOverviewRequestQO));
    }
    /**
     * 获取逾期贷款占不良贷款占比
     */
    @Operation(description = "获取逾期贷款占不良贷款占比")
    @PostMapping("/overdueLoanProportion")
    private AjaxResult selectOverdueLoanProportion(@RequestBody GlobalOverviewRequestQO globalOverviewRequestQO){
        return success(creditRiskDetailService.selectOverdueLoanProportion(globalOverviewRequestQO));
    }

    /**
     * 分行业获取不良贷款余额
     */
    @Operation(description = "分行业获取不良贷款余额")
    @PostMapping("/nonPerformingLoanByIndustry")
    private AjaxResult selectNonPerformingLoanByIndustry(@RequestBody GlobalOverviewRequestQO globalOverviewRequestQO){
        return success(creditRiskDetailService.selectNonPerformingLoanByIndustry(globalOverviewRequestQO));
    }
    /**
     * 分地区获取不良贷款余额
     */
    @Operation(description = "分地区获取不良贷款余额")
    @PostMapping("/nonPerformingLoanByRegion")
    private AjaxResult selectNonPerformingLoanByRegion(@RequestBody GlobalOverviewRequestQO globalOverviewRequestQO){
        return success(creditRiskDetailService.selectNonPerformingLoanByRegion(globalOverviewRequestQO));
    }
    /**
     * 分担保方式获取不良贷款余额
     */
    @Operation(description = "分担保方式获取不良贷款余额")
    @PostMapping("/nonPerformingLoanByGuaranteeType")
    private AjaxResult selectNonPerformingLoanByGuaranteeType(@RequestBody GlobalOverviewRequestQO globalOverviewRequestQO){
        return success(creditRiskDetailService.selectNonPerformingLoanByGuaranteeType(globalOverviewRequestQO));
    }
    /**
     * 分客户类型获取不良贷款余额
     */
    @Operation(description = "分客户类型获取不良贷款余额")
    @PostMapping("/nonPerformingLoanByCustomerType")
    private AjaxResult selectNonPerformingLoanByCustomerType(@RequestBody GlobalOverviewRequestQO globalOverviewRequestQO){
        return success(creditRiskDetailService.selectNonPerformingLoanByCustomerType(globalOverviewRequestQO));
    }

    /**
     * 获取当日新增不良贷款金额
     */
    @Operation(description = "获取当日新增不良贷款金额")
    @PostMapping("/dailyNewNplAmount")
    private AjaxResult selectDailyNewNplAmount(@RequestBody GlobalOverviewRequestQO globalOverviewRequestQO){
        return success(creditRiskDetailService.selectDailyNewNplAmount(globalOverviewRequestQO));
    }
    /**
     * 按机构获取当日新增不良贷款金额
     */
    @Operation(description = "按机构获取当日新增不良贷款金额")
    @PostMapping("/dailyNewNplAmountOrgName")
    private AjaxResult selectDailyNewNplAmountOrgName(@RequestBody GlobalOverviewRequestQO globalOverviewRequestQO){
        return success(creditRiskDetailService.selectDailyNewNplAmountOrgName(globalOverviewRequestQO));
    }
    /**
     * 获取不良贷款处置金额
     */
    @Operation(description = "获取不良贷款处置金额")
    @PostMapping("/disposalAmount")
    private AjaxResult selectDisposalAmount(@RequestBody GlobalOverviewRequestQO globalOverviewRequestQO){
        return success(creditRiskDetailService.selectDisposalAmount(globalOverviewRequestQO));
    }
    /**
     * 分类型获取当日处置金额
     */
    @Operation(description = "分类型获取处置金额")
    @PostMapping("/DisposalAmountByType")
    private AjaxResult selectDisposalAmountByType(@RequestBody GlobalOverviewRequestQO globalOverviewRequestQO){
        return success(creditRiskDetailService.selectDisposalAmountByType(globalOverviewRequestQO));
    }
    /**
     * 获取特色贷款
     */
    @Operation(description = "获取特色贷款")
    @PostMapping("/specialLoan")
    private AjaxResult selectSpecialLoan(@RequestBody GlobalOverviewRequestQO globalOverviewRequestQO){
        return success(creditRiskDetailService.selectSpecialLoan(globalOverviewRequestQO));
    }

}
