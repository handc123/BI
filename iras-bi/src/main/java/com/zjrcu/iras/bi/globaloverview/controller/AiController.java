package com.zjrcu.iras.bi.globaloverview.controller;

import com.zjrcu.iras.bi.globaloverview.domain.qo.GlobalOverviewRequestQO;
import com.zjrcu.iras.bi.globaloverview.domain.vo.RealEstateFinanceMonitoring.RealEstateNonPerformingLoanVo;
import com.zjrcu.iras.bi.globaloverview.service.RealEstateFinanceMonitoring.IRealEstateLoanDetailService;
import com.zjrcu.iras.bi.globaloverview.service.RealEstateFinanceMonitoring.ai.RealEstateLoanDetailAIService;
import com.zjrcu.iras.common.core.domain.AjaxResult;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@RestController
@RequestMapping("/ai")
public class AiController {
    @Resource
    private IRealEstateLoanDetailService realEstateLoanDetailService;
    @Resource
    private RealEstateLoanDetailAIService aiAnalysisService;


    @PostMapping("/realEstateNonPerformingLoan/analysis")
    public AjaxResult analyze(@RequestBody GlobalOverviewRequestQO qo) {

        List<RealEstateNonPerformingLoanVo> list =
                realEstateLoanDetailService
                        .selectRealEstateNonPerformingLoan(qo);

        String analysis =
                aiAnalysisService.analyzeRealEstateNpl(list);

        return AjaxResult.success(analysis);
    }
}
