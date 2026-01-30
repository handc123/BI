package com.zjrcu.iras.bi.globaloverview.service.KeyRiskMonitoring;

import com.zjrcu.iras.bi.globaloverview.domain.qo.GlobalOverviewRequestQO;
import com.zjrcu.iras.bi.globaloverview.domain.model.KeyRiskMonitoring.NonPerformingLoanTransaction;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface INonPerformingLoanTransactionService {
    List<NonPerformingLoanTransaction> selectNonPerformingLoanTransaction(@RequestBody GlobalOverviewRequestQO globalOverviewRequestQO);
}
