package com.zjrcu.iras.bi.globaloverview.service.KeyRiskMonitoring;

import com.zjrcu.iras.bi.globaloverview.domain.qo.GlobalOverviewRequestQO;
import com.zjrcu.iras.bi.globaloverview.domain.model.KeyRiskMonitoring.LoanInterestRate;

import java.util.List;

public interface IKeyDomainLoanRateService {
    List<LoanInterestRate> selectKeyDomainLoanRate(GlobalOverviewRequestQO globalOverviewRequestQO);
}
