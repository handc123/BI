package com.zjrcu.iras.bi.globaloverview.service.CreditRiskMonitoring;

import com.zjrcu.iras.bi.globaloverview.domain.qo.GlobalOverviewRequestQO;
import com.zjrcu.iras.bi.globaloverview.domain.vo.CreditRiskMonitoring.CharacteristicLoanVo;
import com.zjrcu.iras.bi.globaloverview.domain.vo.CreditRiskMonitoring.DailyNewNplAmountOrgNameVo;
import com.zjrcu.iras.bi.globaloverview.domain.vo.CreditRiskMonitoring.DailyNewNplAmountVo;
import com.zjrcu.iras.bi.globaloverview.domain.vo.CreditRiskMonitoring.DisposalAmountTypeVo;
import com.zjrcu.iras.bi.globaloverview.domain.vo.CreditRiskMonitoring.DisposalAmountVo;
import com.zjrcu.iras.bi.globaloverview.domain.vo.CreditRiskMonitoring.NonPerformingBalanceRatioVo;
import com.zjrcu.iras.bi.globaloverview.domain.vo.CreditRiskMonitoring.NonPerformingLoanCunstomerVo;
import com.zjrcu.iras.bi.globaloverview.domain.vo.CreditRiskMonitoring.NonPerformingLoanGuaranteeVo;
import com.zjrcu.iras.bi.globaloverview.domain.vo.CreditRiskMonitoring.NonPerformingLoanIndustryVo;
import com.zjrcu.iras.bi.globaloverview.domain.vo.CreditRiskMonitoring.NonPerformingLoanRegionVo;
import com.zjrcu.iras.bi.globaloverview.domain.vo.CreditRiskMonitoring.OverdueLoanProportionVo;
import com.zjrcu.iras.bi.globaloverview.domain.vo.CreditRiskMonitoring.OverdueLoanSummaryVo;
import com.zjrcu.iras.bi.globaloverview.domain.vo.CreditRiskMonitoring.OverdueLoanVo;

import java.util.List;

public interface ICreditRiskDetailService {


    List<OverdueLoanVo> selectOverdueLoanPeriod(GlobalOverviewRequestQO globalOverviewRequestQO);

    List<OverdueLoanProportionVo> selectOverdueLoanProportion(GlobalOverviewRequestQO globalOverviewRequestQO);

    List<NonPerformingLoanIndustryVo> selectNonPerformingLoanByIndustry(GlobalOverviewRequestQO globalOverviewRequestQO);

    List<NonPerformingLoanRegionVo> selectNonPerformingLoanByRegion(GlobalOverviewRequestQO globalOverviewRequestQO);

    List<NonPerformingLoanGuaranteeVo> selectNonPerformingLoanByGuaranteeType(GlobalOverviewRequestQO globalOverviewRequestQO);

    List<NonPerformingLoanCunstomerVo> selectNonPerformingLoanByCustomerType(GlobalOverviewRequestQO globalOverviewRequestQO);



    List<DisposalAmountTypeVo> selectDisposalAmountByType(GlobalOverviewRequestQO globalOverviewRequestQO);

    List<CharacteristicLoanVo> selectSpecialLoan(GlobalOverviewRequestQO globalOverviewRequestQO);


    List<NonPerformingBalanceRatioVo> selectNonPerformingLoanSummary(GlobalOverviewRequestQO globalOverviewRequestQO);

    List<DailyNewNplAmountVo> selectDailyNewNplAmount(GlobalOverviewRequestQO globalOverviewRequestQO);

    List<DailyNewNplAmountOrgNameVo> selectDailyNewNplAmountOrgName(GlobalOverviewRequestQO globalOverviewRequestQO);

    List<DisposalAmountVo> selectDisposalAmount(GlobalOverviewRequestQO globalOverviewRequestQO);

    List<OverdueLoanSummaryVo> selectOverdueLoanCondition(GlobalOverviewRequestQO globalOverviewRequestQO);
}
