package com.zjrcu.iras.bi.globaloverview.service.FiveMajorArticlesMonitoring;

import com.zjrcu.iras.bi.globaloverview.domain.qo.GlobalOverviewRequestQO;
import com.zjrcu.iras.bi.globaloverview.domain.model.FiveMajorArticlesMonitoring.StrategicLoan;

import java.util.List;

public interface IStrategicLoanService {
    List<StrategicLoan> selectStrategicLoan(GlobalOverviewRequestQO globalOverviewRequestQO);
}
