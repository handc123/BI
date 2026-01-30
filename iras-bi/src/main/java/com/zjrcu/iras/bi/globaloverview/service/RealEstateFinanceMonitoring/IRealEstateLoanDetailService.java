package com.zjrcu.iras.bi.globaloverview.service.RealEstateFinanceMonitoring;

import com.zjrcu.iras.bi.globaloverview.domain.qo.GlobalOverviewRequestQO;
import com.zjrcu.iras.bi.globaloverview.domain.model.RealEstateFinanceMonitoring.RealEstateLoanDetail;
import com.zjrcu.iras.bi.globaloverview.domain.vo.RealEstateFinanceMonitoring.PersonRealEstateLoanVo;
import com.zjrcu.iras.bi.globaloverview.domain.vo.RealEstateFinanceMonitoring.RealEstateLoanCategoryVO;
import com.zjrcu.iras.bi.globaloverview.domain.vo.RealEstateFinanceMonitoring.RealEstateNonPerformingLoanVo;

import java.util.List;

public interface IRealEstateLoanDetailService {
    List<RealEstateLoanDetail> selectRealEstateLoanSummary(GlobalOverviewRequestQO globalOverviewRequestQO);

    List<RealEstateLoanDetail> selectRealEstateCollateralLoan(GlobalOverviewRequestQO globalOverviewRequestQO);

    List<RealEstateLoanCategoryVO> selectRealEstateLoanIssuanceByGroup(GlobalOverviewRequestQO globalOverviewRequestQO);

    List<PersonRealEstateLoanVo> selectPersonRealEstateLoanSummary(GlobalOverviewRequestQO globalOverviewRequestQO);

    List<RealEstateNonPerformingLoanVo> selectRealEstateNonPerformingLoan(GlobalOverviewRequestQO globalOverviewRequestQO);
}
