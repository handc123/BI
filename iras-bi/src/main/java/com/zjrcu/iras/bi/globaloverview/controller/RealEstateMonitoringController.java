package com.zjrcu.iras.bi.globaloverview.controller;

import com.zjrcu.iras.bi.globaloverview.domain.qo.GlobalOverviewRequestQO;
import com.zjrcu.iras.bi.globaloverview.service.RealEstateMonitoring.IRealEstateLoanService;
import com.zjrcu.iras.common.core.controller.BaseController;
import com.zjrcu.iras.common.core.domain.AjaxResult;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/globalOverview/realEstateMonitoring")
public class RealEstateMonitoringController extends BaseController {
    @Resource
    private IRealEstateLoanService realEstateLoanService;

    /**
     * 获取过去一个月房地产贷款余额
     */
    @Operation(description = "获取过去一个月房地产贷款余额")
    @PostMapping("/realEstateLoanBalance")
    public AjaxResult selectRealEstateLoanBalance(@RequestBody GlobalOverviewRequestQO globalOverviewRequestQO) {
        return success(realEstateLoanService.selectRealEstateLoanBalance(globalOverviewRequestQO));
    }
    /**
     * 获取当天房地产贷款人数占比
     */
    @Operation(description = "获取当天房地产贷款人数占比")
    @PostMapping("/realEstateLoanRatio")
    public AjaxResult selectRealEstateLoanPersonRatio(@RequestBody GlobalOverviewRequestQO globalOverviewRequestQO) {
        return success(realEstateLoanService.selectRealEstateLoanPersonRatio(globalOverviewRequestQO));
    }

}
