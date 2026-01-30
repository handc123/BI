package com.zjrcu.iras.bi.globaloverview.service.KeyBusinessMonitoring;

import com.zjrcu.iras.bi.globaloverview.domain.qo.GlobalOverviewRequestQO;
import com.zjrcu.iras.bi.globaloverview.domain.model.KeyBusinessMonitoring.InterbankAssetLiability;

import java.util.List;

public interface IInterbankAssetLiabilityService {
    List<InterbankAssetLiability> selectInterbankAssetLiability(GlobalOverviewRequestQO globalOverviewRequestQO);
}
