package com.zjrcu.iras.bi.globaloverview.service.NonPerformingMonitoring;

import com.zjrcu.iras.bi.globaloverview.domain.qo.GlobalOverviewRequestQO;
import com.zjrcu.iras.bi.globaloverview.domain.vo.NonPerformingMonitoring.NonPerformingLoanTrendVo;

import java.math.BigDecimal;
import java.util.List;

public interface INonPerformingLoanService {
    BigDecimal selectNonPerformingLoanRate(GlobalOverviewRequestQO globalOverviewRequestQO);

    List<NonPerformingLoanTrendVo> selectNonPerformingLoanRateMonth(GlobalOverviewRequestQO globalOverviewRequestQO);

    List<String> selectRegionByOrgName(String orgName);
}
