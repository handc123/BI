package com.zjrcu.iras.bi.globaloverview.service.KeyBusinessMonitoring;

import com.zjrcu.iras.bi.globaloverview.domain.qo.GlobalOverviewRequestQO;
import com.zjrcu.iras.bi.globaloverview.domain.model.KeyBusinessMonitoring.InvestmentBusinessStructure;

import java.util.List;

public interface IInvestmentBusinessStructureService {
    List<InvestmentBusinessStructure> selectInvestmentBusinessStructure(GlobalOverviewRequestQO globalOverviewRequestQO);
}
