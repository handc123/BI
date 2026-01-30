package com.zjrcu.iras.bi.globaloverview.service.RealEstateFinanceMonitoring;

import com.zjrcu.iras.bi.globaloverview.domain.qo.GlobalOverviewRequestQO;
import com.zjrcu.iras.bi.globaloverview.domain.model.RealEstateFinanceMonitoring.RealEstateLoanIssuance;

import java.util.List;

public interface IRealEstateLoanIssuanceService {
    List<RealEstateLoanIssuance> selectRealEstateLoanIssuance(GlobalOverviewRequestQO globalOverviewRequestQO);


    List<RealEstateLoanIssuance> selectAllRealEstateLoanIssuance(GlobalOverviewRequestQO globalOverviewRequestQO);
}
