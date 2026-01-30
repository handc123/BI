package com.zjrcu.iras.bi.globaloverview.mapper.KeyBusinessMonitoring;

import com.zjrcu.iras.bi.globaloverview.domain.qo.GlobalOverviewRequestQO;
import com.zjrcu.iras.bi.globaloverview.domain.model.KeyBusinessMonitoring.InterbankAssetLiability;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface InterbankAssetLiabilityMapper {
    List<InterbankAssetLiability> selectInterbankAssetLiability(@Param("globalOverviewRequestQO") GlobalOverviewRequestQO globalOverviewRequestQO);
}
