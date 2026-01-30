package com.zjrcu.iras.bi.globaloverview.service.KeyRiskMonitoring.Impl;

import com.zjrcu.iras.bi.globaloverview.domain.qo.GlobalOverviewRequestQO;
import com.zjrcu.iras.bi.globaloverview.domain.model.KeyRiskMonitoring.NonPerformingLoanTransaction;
import com.zjrcu.iras.bi.globaloverview.mapper.KeyRiskMonitoring.NonPerformingLoanTransactionMapper;
import com.zjrcu.iras.bi.globaloverview.service.KeyRiskMonitoring.INonPerformingLoanTransactionService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NonPerformingLoanTransactionServiceImpl implements INonPerformingLoanTransactionService {

    @Resource
    private NonPerformingLoanTransactionMapper nonPerformingLoanTransactionMapper;
    @Override
    public List<NonPerformingLoanTransaction> selectNonPerformingLoanTransaction(GlobalOverviewRequestQO globalOverviewRequestQO) {
        return nonPerformingLoanTransactionMapper.selectNonPerformingLoanTransaction(globalOverviewRequestQO);
    }
}
