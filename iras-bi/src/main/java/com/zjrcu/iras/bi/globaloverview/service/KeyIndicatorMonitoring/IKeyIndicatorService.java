package com.zjrcu.iras.bi.globaloverview.service.KeyIndicatorMonitoring;

import com.zjrcu.iras.bi.globaloverview.domain.qo.GlobalOverviewRequestQO;
import com.zjrcu.iras.bi.globaloverview.domain.model.KeyIndicatorMonitoring.KeyIndicator;

import java.util.List;

public interface IKeyIndicatorService {
    List<KeyIndicator> selectKeyIndicator(GlobalOverviewRequestQO globalOverviewRequestQO);
}
