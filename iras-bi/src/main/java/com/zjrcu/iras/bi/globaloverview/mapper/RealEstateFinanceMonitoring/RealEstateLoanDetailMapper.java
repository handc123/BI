package com.zjrcu.iras.bi.globaloverview.mapper.RealEstateFinanceMonitoring;

import com.zjrcu.iras.bi.globaloverview.domain.qo.GlobalOverviewRequestQO;
import com.zjrcu.iras.bi.globaloverview.domain.model.RealEstateFinanceMonitoring.RealEstateLoanDetail;
import com.zjrcu.iras.bi.globaloverview.domain.vo.RealEstateFinanceMonitoring.PersonRealEstateLoanVo;
import com.zjrcu.iras.bi.globaloverview.domain.vo.RealEstateFinanceMonitoring.RealEstateLoanCategoryVO;
import com.zjrcu.iras.bi.globaloverview.domain.vo.RealEstateFinanceMonitoring.RealEstateNonPerformingLoanVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RealEstateLoanDetailMapper {
    List<RealEstateLoanDetail> selectRealEstateLoanSummary(@Param("globalOverviewRequestQO") GlobalOverviewRequestQO globalOverviewRequestQO);

    List<RealEstateLoanDetail> selectRealEstateCollateralLoan(@Param("globalOverviewRequestQO") GlobalOverviewRequestQO globalOverviewRequestQO);

    List<RealEstateLoanCategoryVO> selectRealEstateLoanIssuanceByGroup(@Param("globalOverviewRequestQO") GlobalOverviewRequestQO globalOverviewRequestQO);

    List<PersonRealEstateLoanVo> selectPersonRealEstateLoanSummary(@Param("globalOverviewRequestQO") GlobalOverviewRequestQO globalOverviewRequestQO);

    List<RealEstateNonPerformingLoanVo> selectRealEstateNonPerformingLoan(@Param("globalOverviewRequestQO") GlobalOverviewRequestQO globalOverviewRequestQO);


}
