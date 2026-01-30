package com.zjrcu.iras.bi.indicatorquality.service;

import com.zjrcu.iras.bi.indicatorquality.domain.model.OrgIndicatorRank;

import java.util.List;

public interface IOrgIndicatorRankService {
    List<OrgIndicatorRank> selectOrgIndicatorRank(String rankType);

    List<OrgIndicatorRank> selectOrgIndicatorRankPage(String rankType, String dataDate);
}
