package com.zjrcu.iras.bi.globaloverview.service.KeyBusinessMonitoring;

import com.zjrcu.iras.bi.globaloverview.domain.qo.GlobalOverviewRequestQO;
import com.zjrcu.iras.bi.globaloverview.domain.model.KeyBusinessMonitoring.BondInvestmentStructure;

import java.util.List;

public interface IBondInvestmentStructureService {
     List<BondInvestmentStructure> selectBondInvestmentStructure(GlobalOverviewRequestQO globalOverviewRequestQO);
}
