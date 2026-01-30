package com.zjrcu.iras.bi.globaloverview.mapper.KeyBusinessMonitoring;

import com.zjrcu.iras.bi.globaloverview.domain.qo.GlobalOverviewRequestQO;
import com.zjrcu.iras.bi.globaloverview.domain.model.KeyBusinessMonitoring.InvestmentBusinessStructure;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface InvestmentBusinessStructureMapper {
    List<InvestmentBusinessStructure> selectInvestmentBusinessStructure(@Param("globalOverviewRequestQO") GlobalOverviewRequestQO globalOverviewRequestQO);
}
