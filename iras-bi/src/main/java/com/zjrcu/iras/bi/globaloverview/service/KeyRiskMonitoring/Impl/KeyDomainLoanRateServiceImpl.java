package com.zjrcu.iras.bi.globaloverview.service.KeyRiskMonitoring.Impl;

import com.zjrcu.iras.bi.globaloverview.domain.qo.GlobalOverviewRequestQO;
import com.zjrcu.iras.bi.globaloverview.domain.model.KeyRiskMonitoring.LoanInterestRate;
import com.zjrcu.iras.bi.globaloverview.mapper.KeyRiskMonitoring.KeyDomainLoanRateMapper;
import com.zjrcu.iras.bi.globaloverview.service.KeyRiskMonitoring.IKeyDomainLoanRateService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KeyDomainLoanRateServiceImpl implements IKeyDomainLoanRateService {
    @Resource
    private KeyDomainLoanRateMapper keyDomainLoanRateMapper;
    @Override
    public List<LoanInterestRate> selectKeyDomainLoanRate(GlobalOverviewRequestQO globalOverviewRequestQO) {
        return keyDomainLoanRateMapper.selectKeyDomainLoanRate(globalOverviewRequestQO);
    }
}
