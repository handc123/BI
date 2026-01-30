package com.zjrcu.iras.bi.globaloverview.mapper.CreditRiskMonitoring;

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
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CreditRiskDetailMapper {


    List<OverdueLoanVo> selectOverdueLoanPeriod(@Param("qo") GlobalOverviewRequestQO globalOverviewRequestQO);

    List<OverdueLoanProportionVo> selectOverdueLoanProportion(@Param("qo") GlobalOverviewRequestQO globalOverviewRequestQO);

    List<NonPerformingLoanIndustryVo> selectNonPerformingLoanByIndustry(@Param("qo") GlobalOverviewRequestQO globalOverviewRequestQO);

    List<NonPerformingLoanRegionVo> selectNonPerformingLoanByRegion(@Param("qo") GlobalOverviewRequestQO globalOverviewRequestQO);

    List<NonPerformingLoanGuaranteeVo> selectNonPerformingLoanByGuaranteeType(@Param("qo") GlobalOverviewRequestQO globalOverviewRequestQO);

    List<NonPerformingLoanCunstomerVo> selectNonPerformingLoanByCustomerType(@Param("qo") GlobalOverviewRequestQO globalOverviewRequestQO);

    List<DisposalAmountTypeVo> selectDisposalAmountByType(@Param("qo") GlobalOverviewRequestQO globalOverviewRequestQO);

    List<CharacteristicLoanVo> selectSpecialLoan(@Param("qo") GlobalOverviewRequestQO globalOverviewRequestQO);



    List<NonPerformingBalanceRatioVo> selectNonPerformingLoanSummary(@Param("qo") GlobalOverviewRequestQO globalOverviewRequestQO);

    List<DailyNewNplAmountVo> selectDailyNewNplAmount(@Param("qo") GlobalOverviewRequestQO globalOverviewRequestQO);

    List<DailyNewNplAmountOrgNameVo> selectDailyNewNplAmountOrgName(@Param("qo") GlobalOverviewRequestQO globalOverviewRequestQO);

    List<DisposalAmountVo> selectDisposalAmount(@Param("qo") GlobalOverviewRequestQO globalOverviewRequestQO);

    List<OverdueLoanSummaryVo> selectOverdueLoanCondition(@Param("qo") GlobalOverviewRequestQO globalOverviewRequestQO);
}
