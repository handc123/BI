package com.zjrcu.iras.bi.globaloverview.controller;

import com.zjrcu.iras.bi.globaloverview.domain.qo.GlobalOverviewRequestQO;
import com.zjrcu.iras.bi.globaloverview.service.RealEstateFinanceMonitoring.IRealEstateLoanDetailService;
import com.zjrcu.iras.bi.globaloverview.service.RealEstateFinanceMonitoring.IRealEstateLoanIssuanceService;
import com.zjrcu.iras.common.core.controller.BaseController;
import com.zjrcu.iras.common.core.domain.AjaxResult;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@Tag(name = "房产金融监测接口")
@RestController
@RequestMapping("/globalOverview/realEstateFinanceMonitoring")
public class RealEstateFinanceMonitoringController extends BaseController {
    @Resource
    private IRealEstateLoanDetailService realEstateLoanDetailService;
    @Resource
    private IRealEstateLoanIssuanceService realEstateLoanIssuanceService;
    /**
     * 获取房地产余额及占比
     */
    @PostMapping("/realEstateLoanSummary")
    @Operation(description = "获取房地产余额及占比")
    public AjaxResult selectRealEstateLoanSummary(@RequestBody GlobalOverviewRequestQO globalOverviewRequestQO) {
        return success(realEstateLoanDetailService.selectRealEstateLoanSummary(globalOverviewRequestQO));
    }
    /**
     * 获取以房地产为押品的贷款余额
     */
    @PostMapping("/realEstateCollateralLoan")
    @Operation(description = "获取以房地产为押品的贷款余额")
    public AjaxResult selectRealEstateCollateralLoan(@RequestBody GlobalOverviewRequestQO globalOverviewRequestQO) {
        return success(realEstateLoanDetailService.selectRealEstateCollateralLoan(globalOverviewRequestQO));
    }
    /**
     * 获取房地产当天发放贷款金额及平均利率
     */
    @PostMapping("/realEstateLoanIssuance")
    @Operation(description = "获取房地产当天发放贷款金额及平均利率")
    public AjaxResult selectRealEstateLoanIssuance(@RequestBody GlobalOverviewRequestQO globalOverviewRequestQO) {
        return success(realEstateLoanIssuanceService.selectRealEstateLoanIssuance(globalOverviewRequestQO));
    }
    /**
     * 获取全部房产贷款发放金额及平均利率
     */
    @PostMapping("/allRealEstateLoanIssuance")
    @Operation(description = "获取全部房产贷款发放金额及平均利率")
    public AjaxResult selectAllRealEstateLoanIssuance(@RequestBody GlobalOverviewRequestQO globalOverviewRequestQO) {
    return success(realEstateLoanIssuanceService.selectAllRealEstateLoanIssuance(globalOverviewRequestQO));
    }
    /**
     * 按照分组维度获取房地产当天发放贷款金额
     */
    @PostMapping("/realEstateLoanIssuanceByGroup")
    @Operation(description = "按照分组维度获取房地产当天发放贷款金额")
    public AjaxResult selectRealEstateLoanIssuanceByGroup(@RequestBody GlobalOverviewRequestQO globalOverviewRequestQO){
        return success(realEstateLoanDetailService.selectRealEstateLoanIssuanceByGroup(globalOverviewRequestQO));
    }

    /**
     * 获取个人住房贷款余额及占比
     */
    @PostMapping("/personalHousingLoanSummary")
    @Operation(description = "获取个人住房贷款余额及占比")
    public AjaxResult selectPersonalHousingLoanSummary(@RequestBody GlobalOverviewRequestQO globalOverviewRequestQO) {
        return success(realEstateLoanDetailService.selectPersonRealEstateLoanSummary(globalOverviewRequestQO));
    }
    /**
     * 获取房地产不良贷款余额及占比
     */
    @PostMapping("/realEstateNonPerformingLoan")
    @Operation(description = "获取房地产不良贷款余额及占比")
    public AjaxResult selectRealEstateNonPerformingLoan(@RequestBody GlobalOverviewRequestQO globalOverviewRequestQO) {
        return success(realEstateLoanDetailService.selectRealEstateNonPerformingLoan(globalOverviewRequestQO));
    }

}
