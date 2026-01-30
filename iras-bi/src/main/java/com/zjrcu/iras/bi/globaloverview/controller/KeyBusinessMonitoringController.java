package com.zjrcu.iras.bi.globaloverview.controller;

import com.zjrcu.iras.bi.globaloverview.domain.qo.GlobalOverviewRequestQO;
import com.zjrcu.iras.bi.globaloverview.service.KeyBusinessMonitoring.IBondInvestmentStructureService;
import com.zjrcu.iras.bi.globaloverview.service.KeyBusinessMonitoring.IDepositService;
import com.zjrcu.iras.bi.globaloverview.service.KeyBusinessMonitoring.IInterbankAssetLiabilityService;
import com.zjrcu.iras.bi.globaloverview.service.KeyBusinessMonitoring.IInvestmentBusinessStructureService;
import com.zjrcu.iras.common.core.controller.BaseController;
import com.zjrcu.iras.common.core.domain.AjaxResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@Tag(name = "关键业务监测接口")
@RestController
@RequestMapping("/globalOverview/keyBusinessMonitoring")
public class KeyBusinessMonitoringController extends BaseController {

    @Resource
    private IDepositService depositService;
    @Resource
    private IInterbankAssetLiabilityService interbankAssetLiabilityService;
    @Resource
    private IInvestmentBusinessStructureService investmentBusinessStructureService;
    @Resource
    private IBondInvestmentStructureService bondInvestmentStructureService;
    /**
     * 获取过去一个月存款利率与金额
     */
    @Operation(description = "获取过去一个月存款利率与金额")
    @PostMapping("/deposit")
    public AjaxResult selectDeposit(@RequestBody GlobalOverviewRequestQO globalOverviewRequestQO){
        return success(depositService.selectDeposit(globalOverviewRequestQO));
    }
    /**
     * 获取同业资产与负债
     */
    @Operation(description = "获取同业资产与负债")
    @PostMapping("/interbankAssetLiability")
    public AjaxResult selectInterbankAssetLiability(@RequestBody GlobalOverviewRequestQO globalOverviewRequestQO){
        return success(interbankAssetLiabilityService.selectInterbankAssetLiability(globalOverviewRequestQO));
    }
    /**
     * 获取投资业务结构
     */
    @Operation(description = "获取投资业务结构")
    @PostMapping("/investmentBusinessStructure")
    public AjaxResult selectInvestmentBusinessStructure(@RequestBody GlobalOverviewRequestQO globalOverviewRequestQO){
        return success(investmentBusinessStructureService.selectInvestmentBusinessStructure(globalOverviewRequestQO));
    }
    /**
     * 获取债券投资结构
     */
    @Operation(description = "获取债券投资结构")
    @PostMapping("/bondInvestmentStructure")
    public AjaxResult selectBondInvestmentStructure(@RequestBody GlobalOverviewRequestQO globalOverviewRequestQO){
        return success(bondInvestmentStructureService.selectBondInvestmentStructure(globalOverviewRequestQO));
    }
}
