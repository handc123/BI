package com.zjrcu.iras.bi.globaloverview.service.RealEstateMonitoring;

import com.zjrcu.iras.bi.globaloverview.domain.qo.GlobalOverviewRequestQO;
import com.zjrcu.iras.bi.globaloverview.domain.model.RealEstateMonitoring.RealEstateLoanPerson;
import com.zjrcu.iras.bi.globaloverview.domain.vo.RealEstateMonitoring.RealEstateLoanVo;

import java.util.List;

public interface IRealEstateLoanService {
    List<RealEstateLoanVo> selectRealEstateLoanBalance(GlobalOverviewRequestQO globalOverviewRequestQO);

    List<RealEstateLoanPerson> selectRealEstateLoanPersonRatio(GlobalOverviewRequestQO globalOverviewRequestQO);
}
