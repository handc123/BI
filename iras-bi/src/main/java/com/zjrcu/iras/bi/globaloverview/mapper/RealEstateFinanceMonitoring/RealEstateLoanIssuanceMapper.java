package com.zjrcu.iras.bi.globaloverview.mapper.RealEstateFinanceMonitoring;

import com.zjrcu.iras.bi.globaloverview.domain.qo.GlobalOverviewRequestQO;
import com.zjrcu.iras.bi.globaloverview.domain.model.RealEstateFinanceMonitoring.RealEstateLoanIssuance;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RealEstateLoanIssuanceMapper {
    List<RealEstateLoanIssuance> selectRealEstateLoanIssuance(@Param("globalOverviewRequestQO") GlobalOverviewRequestQO globalOverviewRequestQO);


    List<RealEstateLoanIssuance> selectAllRealEstateLoanIssuance(@Param("globalOverviewRequestQO") GlobalOverviewRequestQO globalOverviewRequestQO);
}
