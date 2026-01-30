package com.zjrcu.iras.bi.globaloverview.service.KeyBusinessMonitoring;

import com.zjrcu.iras.bi.globaloverview.domain.qo.GlobalOverviewRequestQO;
import com.zjrcu.iras.bi.globaloverview.domain.model.KeyBusinessMonitoring.Deposit;

import java.util.List;

public interface IDepositService {
    List<Deposit> selectDeposit(GlobalOverviewRequestQO globalOverviewRequestQO);
}
